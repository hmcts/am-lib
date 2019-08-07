package uk.gov.hmcts.reform.amapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static uk.gov.hmcts.reform.amapi.util.ErrorConstants.INVALID_REQUEST;
import static uk.gov.hmcts.reform.amapi.util.ErrorConstants.MALFORMED_JSON;
import static uk.gov.hmcts.reform.amapi.util.ErrorConstants.RESOURCE_NOT_FOUND;
import static uk.gov.hmcts.reform.amapi.util.ErrorConstants.SERVICE_FAILED;
import static uk.gov.hmcts.reform.amapi.util.ErrorConstants.UNSUPPORTED_MEDIA_TYPES;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class AccessManagementResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    @ResponseBody
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ErrorResponse errorDetails = ErrorResponse.builder()
            .errorDescription(getRootException(ex).getLocalizedMessage())
            .errorMessage(MALFORMED_JSON)
            .status(BAD_REQUEST).errorCode(BAD_REQUEST.value())
            .timeStamp(getTimeStamp())
            .build();

        return new ResponseEntity<>(
            errorDetails, new HttpHeaders(), BAD_REQUEST);
    }

    @Override
    @ResponseBody
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ErrorResponse errorDetails = ErrorResponse.builder()
            .errorDescription(UNSUPPORTED_MEDIA_TYPES)
            .errorMessage(UNSUPPORTED_MEDIA_TYPES)
            .status(UNSUPPORTED_MEDIA_TYPE).errorCode(UNSUPPORTED_MEDIA_TYPE.value())
            .timeStamp(getTimeStamp())
            .build();

        return new ResponseEntity<>(
            errorDetails, new HttpHeaders(), UNSUPPORTED_MEDIA_TYPE);
    }


    @ResponseBody
    @ResponseStatus(NOT_FOUND)
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ErrorResponse errorDetails = ErrorResponse.builder()
            .errorDescription(RESOURCE_NOT_FOUND)
            .errorMessage(RESOURCE_NOT_FOUND)
            .status(NOT_FOUND).errorCode(NOT_FOUND.value())
            .timeStamp(getTimeStamp())
            .build();

        return new ResponseEntity<>(
            errorDetails, new HttpHeaders(), NOT_FOUND);
    }

    @ResponseBody
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Object> handleMissingInputParameterException(InvalidParameterException ex) {

        ErrorResponse errorDetails = ErrorResponse.builder()
            .errorDescription(getRootException(ex).getLocalizedMessage())
            .errorMessage(getRootException(ex).getLocalizedMessage())
            .status(BAD_REQUEST).errorCode(BAD_REQUEST.value())
            .timeStamp(getTimeStamp())
            .build();

        return new ResponseEntity<>(errorDetails, new HttpHeaders(), BAD_REQUEST);
    }


    @ResponseBody
    @ExceptionHandler({PersistenceException.class})
    public ResponseEntity<Object> handleJdbiPersistenceErrors(PersistenceException ex) {

        log.info("JDBI Persistance EXCEPTION::::" + ex.getMessage());
        ErrorResponse errorDetails = ErrorResponse.builder()
            .errorDescription(SERVICE_FAILED)
            .errorMessage(INVALID_REQUEST)
            .status(INTERNAL_SERVER_ERROR).errorCode(INTERNAL_SERVER_ERROR.value())
            .timeStamp(getTimeStamp())
            .build();

        return new ResponseEntity<>(errorDetails, new HttpHeaders(), INTERNAL_SERVER_ERROR);
    }

    @ResponseBody
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, RuntimeException.class})
    public ResponseEntity<Object> handleInternalServerErrors(RuntimeException ex, WebRequest request) {

        ErrorResponse errorDetails = ErrorResponse.builder()
            .errorDescription(getRootException(ex).getLocalizedMessage())
            .errorMessage(getRootException(ex).getLocalizedMessage())
            .status(INTERNAL_SERVER_ERROR).errorCode(INTERNAL_SERVER_ERROR.value())
            .timeStamp(getTimeStamp())
            .build();

        return new ResponseEntity<>(errorDetails, new HttpHeaders(), INTERNAL_SERVER_ERROR);
    }


    private String getTimeStamp() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.ENGLISH).format(new Date());
    }

    private static Throwable getRootException(Throwable exception) {
        Throwable rootException = exception;
        while (rootException.getCause() != null) {
            rootException = rootException.getCause();
        }
        return rootException;
    }

}
