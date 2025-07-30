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
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTED(1003,"Email exited",HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_INVALID(1053, "Verification code invalid", HttpStatus.BAD_REQUEST),
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
