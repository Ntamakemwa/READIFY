package com.readify.inventory.repository;

import com.readify.book.entity.Book;
import com.readify.common.enums.BookCopyStatus;
import com.readify.inventory.entity.BookCopy;
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
