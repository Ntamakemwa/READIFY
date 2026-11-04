package com.readify.dto;

import com.readify.common.enums.BookCopyStatus;
import jakarta.validation.constraints.NotNull;

public class BookCopyStatusUpdateRequest {

    @NotNull
    private BookCopyStatus status;

    public BookCopyStatus getStatus() {
        return status;
    }

    public void setStatus(BookCopyStatus status) {
        this.status = status;
    }
}
