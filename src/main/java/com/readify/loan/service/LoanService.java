package com.readify.loan.service;

import com.readify.auth.entity.User;
import com.readify.auth.entity.UserRole;
import com.readify.auth.repository.UserRepository;
import com.readify.common.enums.BookCopyStatus;
import com.readify.common.enums.LoanStatus;
import com.readify.common.enums.UserStatus;
import com.readify.common.exception.BusinessRuleException;
import com.readify.common.exception.ResourceNotFoundException;
import com.readify.common.exception.UnauthorizedAccessException;
import com.readify.inventory.entity.BookCopy;
import com.readify.inventory.repository.BookCopyRepository;
import com.readify.loan.dto.BorrowBookRequest;
import com.readify.loan.dto.RenewLoanRequest;
import com.readify.loan.entity.Loan;
import com.readify.loan.repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
public class LoanService {

    private static final int DEFAULT_LOAN_DAYS = 14;
    private static final int DEFAULT_RENEW_DAYS = 7;
    private static final int MAX_ACTIVE_LOANS = 3;
    private static final int MAX_RENEWALS = 2;

    private final LoanRepository loanRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;

    public LoanService(LoanRepository loanRepository,
                       BookCopyRepository bookCopyRepository,
                       UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.userRepository = userRepository;
    }

    public Loan borrowBook(String currentEmail, BorrowBookRequest request) {
        User actor = getUserByEmail(currentEmail);
        User member = resolveMember(actor, request.getMemberId());

        if (!member.isEnabled() || member.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessRuleException.LoanException("Member account is not active.");
        }

        long activeLoans = loanRepository.countByMemberIdAndStatusIn(member.getId(), Set.of(LoanStatus.ACTIVE, LoanStatus.RENEWED));
        if (activeLoans >= MAX_ACTIVE_LOANS) {
            throw new BusinessRuleException.LoanException("Member has reached the loan limit.");
        }

        BookCopy copy = bookCopyRepository.findById(request.getBookCopyId())
                .orElseThrow(() -> new ResourceNotFoundException("Book copy", "id", request.getBookCopyId()));

        if (copy.getStatus() != BookCopyStatus.AVAILABLE) {
            throw new BusinessRuleException.LoanException("Book copy is not available.");
        }

        Instant now = Instant.now();
        int loanDays = sanitizeDays(request.getLoanDays(), DEFAULT_LOAN_DAYS);

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBookCopy(copy);
        loan.setIssueDate(now);
        loan.setDueDate(now.plus(loanDays, ChronoUnit.DAYS));
        loan.setStatus(LoanStatus.ACTIVE);

        copy.setStatus(BookCopyStatus.BORROWED);
        bookCopyRepository.save(copy);
        return loanRepository.save(loan);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> getCurrentUserLoans(String email) {
        return loanRepository.findByMemberEmailOrderByIssueDateDesc(email);
    }

    public List<Loan> getOverdueLoans() {
        return loanRepository.findByStatusInAndDueDateBefore(Set.of(LoanStatus.ACTIVE, LoanStatus.RENEWED), Instant.now());
    }

    public Loan returnBook(String currentEmail, Long loanId) {
        User actor = getUserByEmail(currentEmail);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

        if (!canManageLoan(actor, loan)) {
            throw new UnauthorizedAccessException("You cannot return this loan.");
        }

        if (loan.getReturnDate() != null || loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleException.LoanException("Loan is already returned.");
        }

        loan.setReturnDate(Instant.now());
        loan.setStatus(LoanStatus.RETURNED);

        BookCopy copy = loan.getBookCopy();
        copy.setStatus(BookCopyStatus.AVAILABLE);
        bookCopyRepository.save(copy);

        return loanRepository.save(loan);
    }

    public Loan renewLoan(String currentEmail, Long loanId, RenewLoanRequest request) {
        User actor = getUserByEmail(currentEmail);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

        if (!canManageLoan(actor, loan)) {
            throw new UnauthorizedAccessException("You cannot renew this loan.");
        }

        if (loan.getReturnDate() != null || loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleException.LoanException("Returned loans cannot be renewed.");
        }

        if (loan.getDueDate().isBefore(Instant.now())) {
            loan.setStatus(LoanStatus.OVERDUE);
            loanRepository.save(loan);
            throw new BusinessRuleException.LoanException("Overdue loans cannot be renewed.");
        }

        if (loan.getRenewalCount() >= MAX_RENEWALS) {
            throw new BusinessRuleException.LoanException("Loan renewal limit reached.");
        }

        int extraDays = sanitizeDays(request.getExtraDays(), DEFAULT_RENEW_DAYS);
        loan.setDueDate(loan.getDueDate().plus(extraDays, ChronoUnit.DAYS));
        loan.setRenewalCount(loan.getRenewalCount() + 1);
        loan.setStatus(LoanStatus.RENEWED);
        return loanRepository.save(loan);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private User resolveMember(User actor, Long memberId) {
        if (memberId == null) {
            return actor;
        }

        if (!hasAnyRole(actor, UserRole.ROLE_LIBRARIAN, UserRole.ROLE_ADMIN, UserRole.ROLE_SUPER_ADMIN)) {
            throw new UnauthorizedAccessException("Only staff can issue books for another member.");
        }

        return userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));
    }

    private boolean canManageLoan(User actor, Loan loan) {
        return loan.getMember().getId().equals(actor.getId())
                || hasAnyRole(actor, UserRole.ROLE_LIBRARIAN, UserRole.ROLE_ADMIN, UserRole.ROLE_SUPER_ADMIN);
    }

    private boolean hasAnyRole(User user, UserRole... roles) {
        for (UserRole role : roles) {
            if (user.getRole() == role) {
                return true;
            }
        }
        return false;
    }

    private int sanitizeDays(Integer requestedDays, int fallback) {
        if (requestedDays == null || requestedDays < 1 || requestedDays > 30) {
            return fallback;
        }
        return requestedDays;
    }
}
