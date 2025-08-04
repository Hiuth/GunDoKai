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
    SUB_CATEGORY_NOT_FOUND(1010, "Sub category not found", HttpStatus.BAD_REQUEST),
    SUB_CATEGORY_NOT_EXISTS(1011, "Sub category not found", HttpStatus.BAD_REQUEST),
    SUB_CATEGORY_ALREADY_EXISTS(1012, "Sub category already exists", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXISTS(1013, "Category not found", HttpStatus.BAD_REQUEST),
    PRODUCT_ALREADY_EXISTS(1014, "Product already exists", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXISTS(1015, "Product not found", HttpStatus.BAD_REQUEST),
    PRODUCT_IMG_NOT_EXISTS(1016, "Product image not found", HttpStatus.BAD_REQUEST),
    PRODUCT_DETAIL_NOT_EXISTS(1017, "Product detail not found", HttpStatus.BAD_REQUEST),
    PRODUCT_DETAIL_ALREADY_EXISTS(1018, "Product detail already exists", HttpStatus.BAD_REQUEST),
    INVALID_KEY(8888, "Invalid key", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.SERVICE_UNAVAILABLE),
    USER_NOT_EXISTED(1019, "User not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1020, "You do not have permission", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(1021, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    USER_EXISTED(1022, "User already exists", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTED(1023, "Email already exists", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_INVALID(1053, "Verification code invalid", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(2001, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_INVALID_STATUS(2002, "Invalid order status", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_CANCELLED(2003, "Order has already been cancelled", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_BE_UPDATED(2004, "Order cannot be updated", HttpStatus.BAD_REQUEST),
    ORDER_PAYMENT_FAILED(2005, "Payment failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_ALREADY_PAID(2006, "Order has already been paid", HttpStatus.BAD_REQUEST),
    ORDER_ACCESS_DENIED(2007, "You are not allowed to access this order", HttpStatus.FORBIDDEN),
    ORDER_CREATE_FAILED(2008, "Failed to create order", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_CANNOT_REPAY(2009, "Only orders with failed payment can be repaid", HttpStatus.BAD_REQUEST),
    ORDER_DETAIL_NOT_FOUND(2010, "Order detail not found", HttpStatus.NOT_FOUND),
    ORDER_DETAIL_ALREADY_EXISTS(2011, "Order detail already exists for this order and product", HttpStatus.BAD_REQUEST),
    ORDER_DETAIL_INVALID_QUANTITY(2012, "Invalid quantity for order detail", HttpStatus.BAD_REQUEST),
    ORDER_DETAIL_UPDATE_FAILED(2013, "Failed to update order detail", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_DETAIL_DELETE_FAILED(2014, "Failed to delete order detail", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_DETAIL_PRODUCT_NOT_FOUND(2015, "Product not found for order detail", HttpStatus.NOT_FOUND),
    ORDER_DETAIL_ORDER_NOT_FOUND(2016, "Order not found for order detail", HttpStatus.NOT_FOUND);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}