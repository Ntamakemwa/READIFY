package com.readify.service;

import com.readify.dto.AuthorRequest;
import com.readify.entity.Author;
import com.readify.repository.AuthorRepository;
import com.readify.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public Author getAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", id));
    }

    public Author createAuthor(AuthorRequest request) {
        Author author = new Author();
        author.setFullName(request.getFullName());
        author.setBiography(request.getBiography());
        return authorRepository.save(author);
    }

    public Author updateAuthor(Long id, AuthorRequest request) {
        Author existing = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", id));
        existing.setFullName(request.getFullName());
        existing.setBiography(request.getBiography());
        return authorRepository.save(existing);
    }

    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", id));
        authorRepository.delete(author);
    }
}
