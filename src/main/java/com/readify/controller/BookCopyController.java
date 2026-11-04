package com.readify.controller;

import com.readify.dto.BookCopyRequest;
import com.readify.dto.BookCopyStatusUpdateRequest;
import com.readify.entity.BookCopy;
import com.readify.service.BookCopyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BookCopyController {

    private final BookCopyService bookCopyService;

    public BookCopyController(BookCopyService bookCopyService) {
        this.bookCopyService = bookCopyService;
    }

    @GetMapping("/api/v1/books/{bookId}/copies")
    public List<BookCopy> getBookCopies(@PathVariable Long bookId) {
        return bookCopyService.getCopiesByBook(bookId);
    }

    @GetMapping("/api/v1/books/{bookId}/copies/available")
    public List<BookCopy> getAvailableCopies(@PathVariable Long bookId) {
        return bookCopyService.getAvailableCopiesByBook(bookId);
    }

    @PostMapping("/api/v1/copies")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<BookCopy> createCopy(@Valid @RequestBody BookCopyRequest request) {
        return ResponseEntity.ok(bookCopyService.createCopy(request));
    }

    @PatchMapping("/api/v1/copies/{id}/status")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<BookCopy> updateCopyStatus(@PathVariable Long id,
                                                     @Valid @RequestBody BookCopyStatusUpdateRequest request) {
        return ResponseEntity.ok(bookCopyService.updateCopyStatus(id, request.getStatus()));
    }
}
