package com.readify.repository;

import com.readify.entity.Book;
import com.readify.common.enums.BookCopyStatus;
import com.readify.entity.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    List<BookCopy> findByBookId(Long bookId);
    List<BookCopy> findByBookIdAndStatus(Long bookId, BookCopyStatus status);
    Optional<BookCopy> findByCopyCode(String copyCode);

    @Query("select distinct c.book from BookCopy c where c.status = :status")
    List<Book> findDistinctBooksByStatus(BookCopyStatus status);
}