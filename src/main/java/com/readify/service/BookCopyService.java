package com.readify.service;

import com.readify.entity.Book;
import com.readify.repository.BookRepository;
import com.readify.common.enums.BookCopyStatus;
import com.readify.common.exception.DuplicateResourceException;
import com.readify.common.exception.ResourceNotFoundException;
import com.readify.dto.BookCopyRequest;
import com.readify.entity.BookCopy;
import com.readify.repository.BookCopyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;

    public BookCopyService(BookCopyRepository bookCopyRepository, BookRepository bookRepository) {
        this.bookCopyRepository = bookCopyRepository;
        this.bookRepository = bookRepository;
    }

    public List<BookCopy> getCopiesByBook(Long bookId) {
        return bookCopyRepository.findByBookId(bookId);
    }

    public List<BookCopy> getAvailableCopiesByBook(Long bookId) {
        return bookCopyRepository.findByBookIdAndStatus(bookId, BookCopyStatus.AVAILABLE);
    }

    public BookCopy createCopy(BookCopyRequest request) {
        if (bookCopyRepository.findByCopyCode(request.getCopyCode()).isPresent()) {
            throw new DuplicateResourceException("Book copy", "copy code", request.getCopyCode());
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        BookCopy copy = new BookCopy();
        copy.setBook(book);
        copy.setCopyCode(request.getCopyCode());
        copy.setConditionNote(request.getConditionNote());
        copy.setShelfLocation(request.getShelfLocation());
        copy.setStatus(BookCopyStatus.AVAILABLE);

        return bookCopyRepository.save(copy);
    }

    public BookCopy updateCopyStatus(Long id, BookCopyStatus status) {
        BookCopy copy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book copy", "id", id));
        copy.setStatus(status);
        return bookCopyRepository.save(copy);
    }
}
