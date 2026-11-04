package com.readify.service;

import com.readify.entity.Author;
import com.readify.author.repository.AuthorRepository;
import com.readify.dto.BookRequest;
import com.readify.entity.Book;
import com.readify.book.repository.BookRepository;
import com.readify.entity.Category;
import com.readify.category.repository.CategoryRepository;
import com.readify.common.enums.BookCopyStatus;
import com.readify.common.exception.DuplicateResourceException;
import com.readify.common.exception.ResourceNotFoundException;
import com.readify.inventory.repository.BookCopyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookCopyRepository bookCopyRepository;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       CategoryRepository categoryRepository,
                       BookCopyRepository bookCopyRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
    }

    public List<Book> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrIsbnContainingIgnoreCase(query, query);
    }

    public List<Book> getBooksByCategory(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }

    public List<Book> getBooksByAuthor(Long authorId) {
        return bookRepository.findByAuthorId(authorId);
    }

    public List<Book> getAvailableBooks() {
        return bookCopyRepository.findDistinctBooksByStatus(BookCopyStatus.AVAILABLE);
    }

    public Book createBook(BookRequest request) {
        if (bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new DuplicateResourceException("Book", "ISBN", request.getIsbn());
        }

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", request.getAuthorId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPublishedYear(request.getPublishedYear());
        book.setLanguage(request.getLanguage());
        book.setPublisher(request.getPublisher());
        book.setAuthor(author);
        book.setCategory(category);

        return bookRepository.save(book);
    }

    public Book updateBook(Long id, BookRequest request) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        if (!existing.getIsbn().equals(request.getIsbn()) &&
                bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new DuplicateResourceException("Book", "ISBN", request.getIsbn());
        }

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", request.getAuthorId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        existing.setTitle(request.getTitle());
        existing.setIsbn(request.getIsbn());
        existing.setDescription(request.getDescription());
        existing.setPublishedYear(request.getPublishedYear());
        existing.setLanguage(request.getLanguage());
        existing.setPublisher(request.getPublisher());
        existing.setAuthor(author);
        existing.setCategory(category);

        return bookRepository.save(existing);
    }

    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        bookRepository.delete(book);
    }
}
