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
