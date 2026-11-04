package com.readify.book.repository;

import com.readify.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
    List<Book> findByCategoryId(Long categoryId);
    List<Book> findByAuthorId(Long authorId);
    List<Book> findByTitleContainingIgnoreCaseOrIsbnContainingIgnoreCase(String title, String isbn);
}
