package org.example.gundokai.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    EMAIL_EXISTED(1001, "Email already exists", HttpStatus.BAD_REQUEST),
    FILE_EMPTY(1002, "File is empty", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(1003, "File too large", HttpStatus.PAYLOAD_TOO_LARGE),
    INVALID_FILE_TYPE(1004, "Invalid file type", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(1005, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_FILE_FAILED(1006, "Delete file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_ALREADY_EXISTS(1007, "Category already exists", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(1008, "Category not found", HttpStatus.NOT_FOUND),
    LIST_EMPTY(1009, "List is empty", HttpStatus.BAD_REQUEST),
    SUB_CATEGORY_NOT_FOUND(10010, "Sub category not found", HttpStatus.BAD_REQUEST),
    SUB_CATEGORY_NOT_EXISTS(10011, "Sub category not found", HttpStatus.BAD_REQUEST),
    SUB_CATEGORY_ALREADY_EXISTS(10012, "Sub category already exists", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXISTS(10013, "Category not found", HttpStatus.BAD_REQUEST),
    INVALID_KEY(8888, "Invalid key", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.SERVICE_UNAVAILABLE),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;

    }
