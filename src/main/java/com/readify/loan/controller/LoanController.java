package com.readify.loan.controller;

import com.readify.loan.dto.BorrowBookRequest;
import com.readify.loan.dto.RenewLoanRequest;
import com.readify.entity.Loan;
import com.readify.loan.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/borrow")
    @PreAuthorize("hasAnyRole('MEMBER','LIBRARIAN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Loan> borrowBook(@Valid @RequestBody BorrowBookRequest request, Authentication authentication) {
        return ResponseEntity.ok(loanService.borrowBook(authentication.getName(), request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN','SUPER_ADMIN')")
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MEMBER','LIBRARIAN','ADMIN','SUPER_ADMIN')")
    public List<Loan> getMyLoans(Authentication authentication) {
        return loanService.getCurrentUserLoans(authentication.getName());
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN','SUPER_ADMIN')")
    public List<Loan> getOverdueLoans() {
        return loanService.getOverdueLoans();
    }

    @PostMapping("/{loanId}/return")
    @PreAuthorize("hasAnyRole('MEMBER','LIBRARIAN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Loan> returnBook(@PathVariable Long loanId, Authentication authentication) {
        return ResponseEntity.ok(loanService.returnBook(authentication.getName(), loanId));
    }

    @PostMapping("/{loanId}/renew")
    @PreAuthorize("hasAnyRole('MEMBER','LIBRARIAN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Loan> renewLoan(@PathVariable Long loanId,
                                          @RequestBody(required = false) RenewLoanRequest request,
                                          Authentication authentication) {
        RenewLoanRequest renewRequest = request == null ? new RenewLoanRequest() : request;
        return ResponseEntity.ok(loanService.renewLoan(authentication.getName(), loanId, renewRequest));
    }
}
