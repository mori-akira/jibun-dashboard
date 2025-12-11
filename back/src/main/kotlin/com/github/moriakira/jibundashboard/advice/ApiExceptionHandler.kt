package com.github.moriakira.jibundashboard.advice

import com.github.moriakira.jibundashboard.generated.model.ErrorDetail
import com.github.moriakira.jibundashboard.generated.model.ErrorInfo
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
    ): ResponseEntity<ErrorInfo> {
        log.debug("MethodArgumentNotValidException", ex)

        val details = ex.bindingResult.allErrors.map { error ->
            val fieldName = (error as? FieldError)?.field
            ErrorDetail(
                errorCode = ErrorDetail.ErrorCode.VALIDATION_ERROR,
                errorLevel = ErrorDetail.ErrorLevel.WARN,
                errorMessage = error.defaultMessage ?: "Validation failed",
                errorItem = fieldName?.let { listOf(it) },
            )
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorInfo(errors = details))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
    ): ResponseEntity<ErrorInfo> {
        log.debug("ConstraintViolationException", ex)

        val details = ex.constraintViolations.map { violation ->
            ErrorDetail(
                errorCode = ErrorDetail.ErrorCode.VALIDATION_ERROR,
                errorLevel = ErrorDetail.ErrorLevel.WARN,
                errorMessage = violation.message,
                errorItem = listOf(violation.propertyPath.toString()),
            )
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorInfo(errors = details))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
    ): ResponseEntity<ErrorInfo> {
        log.debug("MissingServletRequestParameterException", ex)

        val detail = ErrorDetail(
            errorCode = ErrorDetail.ErrorCode.MISSING_PARAMETER,
            errorLevel = ErrorDetail.ErrorLevel.WARN,
            errorMessage = "Required request parameter '${ex.parameterName}' is missing.",
            errorItem = listOf(ex.parameterName),
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorInfo(errors = listOf(detail)))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
    ): ResponseEntity<ErrorInfo> {
        log.debug("MethodArgumentTypeMismatchException", ex)

        val paramName = ex.name
        val requiredType = ex.requiredType?.simpleName ?: "unknown"
        val detail = ErrorDetail(
            errorCode = ErrorDetail.ErrorCode.TYPE_MISMATCH,
            errorLevel = ErrorDetail.ErrorLevel.WARN,
            errorMessage = "Parameter '$paramName' must be of type $requiredType.",
            errorItem = listOf(paramName),
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorInfo(errors = listOf(detail)))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
    ): ResponseEntity<ErrorInfo> {
        log.debug("HttpMessageNotReadableException", ex)

        val detail = ErrorDetail(
            errorCode = ErrorDetail.ErrorCode.INVALID_REQUEST_BODY,
            errorLevel = ErrorDetail.ErrorLevel.WARN,
            errorMessage = "Request body is invalid or unreadable.",
            errorItem = null,
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorInfo(errors = listOf(detail)))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
    ): ResponseEntity<ErrorInfo> {
        log.debug("IllegalArgumentException", ex)

        val detail = ErrorDetail(
            errorCode = ErrorDetail.ErrorCode.INVALID_ARGUMENT,
            errorLevel = ErrorDetail.ErrorLevel.WARN,
            errorMessage = ex.message ?: "Invalid argument.",
            errorItem = null,
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorInfo(errors = listOf(detail)))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnhandledException(
        ex: Exception,
    ): ResponseEntity<ErrorInfo> {
        log.error("Unhandled exception", ex)

        val detail = ErrorDetail(
            errorCode = ErrorDetail.ErrorCode.UNEXPECTED_ERROR,
            errorLevel = ErrorDetail.ErrorLevel.ERROR,
            errorMessage = "Unexpected error occurred.",
            errorItem = null,
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorInfo(errors = listOf(detail)))
    }
}
