import os

services = [
    {
        "path": "catalog-service/catalog-presentation/src/main/java/com/furnisight/catalog/presentation/web/exception",
        "package": "com.furnisight.catalog.presentation.web.exception",
        "has_domain_ex": True,
        "domain_ex_import": "com.furnisight.catalog.domain.exceptions.DomainException;\nimport com.furnisight.catalog.domain.exceptions.*;"
    },
    {
        "path": "order-service/order-adapter/src/main/java/com/furnisight/order/adapter/in/web/exception",
        "package": "com.furnisight.order.adapter.in.web.exception",
        "has_domain_ex": True,
        "domain_ex_import": "com.furnisight.order.domain.exceptions.DomainException;\nimport com.furnisight.order.domain.exceptions.*;"
    },
    {
        "path": "user-service/user-infrastructure/src/main/java/com/furnisight/user/infrastructure/exception",
        "package": "com.furnisight.user.infrastructure.exception",
        "has_domain_ex": True,
        "domain_ex_import": "com.furnisight.user.domain.exceptions.DomainException;"
    },
    {
        "path": "cart-service/src/main/java/com/furnisight/cart/exception",
        "package": "com.furnisight.cart.exception",
        "has_domain_ex": False,
        "domain_ex_import": ""
    },
    {
        "path": "media-service/media-api/src/main/java/com/furnisight/media/api/exception",
        "package": "com.furnisight.media.api.exception",
        "has_domain_ex": False,
        "domain_ex_import": ""
    },
    {
        "path": "notification-service/notification-adapter/src/main/java/com/furnisight/notification/adapter/in/web/exception",
        "package": "com.furnisight.notification.adapter.in.web.exception",
        "has_domain_ex": False,
        "domain_ex_import": ""
    }
]

api_error_template = """package {package};

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiError {{

    private LocalDateTime timestamp;
    private int status;
    private String code;
    private String message;
    private String path;
}}
"""

handler_template = """package {package};

{domain_ex_import}
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {{
{domain_ex_methods}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {{
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: message={{}}, path={{}}", message, request.getRequestURI());

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request.getRequestURI());
    }}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {{
        log.warn("Illegal argument: message={{}}, path={{}}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request.getRequestURI());
    }}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {{
        log.error("Unexpected exception occurred at path={{}}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred", request.getRequestURI());
    }}

    private String formatFieldError(FieldError error) {{
        return error.getField() + ": " + error.getDefaultMessage();
    }}

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String code, String message, String path) {{
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .code(code)
                .message(message)
                .path(path)
                .build();
        return ResponseEntity.status(status).body(apiError);
    }}
}}
"""

domain_method_template = """
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomainException(
            DomainException ex,
            HttpServletRequest request
    ) {{
        String code = "BAD_REQUEST";
        if (ex.getErrorCode() != null) {{
            code = ex.getErrorCode().name();
        }}
        log.warn("Domain exception occurred: code={{}}, message={{}}, path={{}}", code, ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, code, ex.getMessage(), request.getRequestURI());
    }}
"""

catalog_order_extra = """
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex, HttpServletRequest request) {{
        String code = ex.getErrorCode() != null ? ex.getErrorCode().name() : "NOT_FOUND";
        return buildResponse(HttpStatus.NOT_FOUND, code, ex.getMessage(), request.getRequestURI());
    }}

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAlreadyExistsException(AlreadyExistsException ex, HttpServletRequest request) {{
        String code = ex.getErrorCode() != null ? ex.getErrorCode().name() : "CONFLICT";
        return buildResponse(HttpStatus.CONFLICT, code, ex.getMessage(), request.getRequestURI());
    }}

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleDomainValidationException(ValidationException ex, HttpServletRequest request) {{
        String code = ex.getErrorCode() != null ? ex.getErrorCode().name() : "BAD_REQUEST";
        return buildResponse(HttpStatus.BAD_REQUEST, code, ex.getMessage(), request.getRequestURI());
    }}

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiError> handleInvalidOperationException(InvalidOperationException ex, HttpServletRequest request) {{
        String code = ex.getErrorCode() != null ? ex.getErrorCode().name() : "BAD_REQUEST";
        return buildResponse(HttpStatus.BAD_REQUEST, code, ex.getMessage(), request.getRequestURI());
    }}

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {{
        String code = ex.getErrorCode() != null ? ex.getErrorCode().name() : "FORBIDDEN";
        return buildResponse(HttpStatus.FORBIDDEN, code, ex.getMessage(), request.getRequestURI());
    }}
"""

for svc in services:
    os.makedirs(svc["path"], exist_ok=True)
    
    # write ApiError
    api_error_code = api_error_template.format(package=svc["package"])
    with open(os.path.join(svc["path"], "ApiError.java"), "w") as f:
        f.write(api_error_code)
        
    # write GlobalExceptionHandler
    domain_ex_methods = ""
    if svc["has_domain_ex"]:
        domain_ex_methods = domain_method_template
        if "catalog" in svc["package"] or "order" in svc["package"]:
            domain_ex_methods += catalog_order_extra
            
    handler_code = handler_template.format(
        package=svc["package"],
        domain_ex_import=svc["domain_ex_import"],
        domain_ex_methods=domain_ex_methods
    )
    with open(os.path.join(svc["path"], "GlobalExceptionHandler.java"), "w") as f:
        f.write(handler_code)

print("Generated exception handlers successfully.")
