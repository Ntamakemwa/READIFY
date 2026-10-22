package com.readify.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookCopyRequest {

    @NotNull
    private Long bookId;

    @NotBlank
    private String copyCode;

    @NotBlank
    private String conditionNote;

    @NotBlank
    private String shelfLocation;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getCopyCode() {
        return copyCode;
    }

    public void setCopyCode(String copyCode) {
        this.copyCode = copyCode;
    }

    public String getConditionNote() {
        return conditionNote;
    }

    public void setConditionNote(String conditionNote) {
        this.conditionNote = conditionNote;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }
}
