package com.readify.repository;

import com.readify.common.enums.LoanStatus;
import com.readify.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    long countByMemberIdAndStatusIn(Long memberId, Collection<LoanStatus> statuses);
    List<Loan> findByMemberEmailOrderByIssueDateDesc(String email);
    List<Loan> findByStatusInAndDueDateBefore(Collection<LoanStatus> statuses, Instant dueDate);
}
