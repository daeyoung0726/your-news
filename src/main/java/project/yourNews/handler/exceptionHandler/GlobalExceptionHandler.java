package project.yourNews.handler.exceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import project.yourNews.handler.exceptionHandler.error.ErrorCode;
import project.yourNews.handler.exceptionHandler.error.ErrorDto;
import project.yourNews.handler.exceptionHandler.exception.CustomException;

import static project.yourNews.handler.exceptionHandler.error.ErrorCode.INTERNAL_SERVER_ERROR;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /* CustomHandler 에러 처리 */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity customExceptionHandler(CustomException ex) {

        ErrorCode errorCode = ex.getErrorCode();
        ErrorDto errorDto = new ErrorDto(errorCode.getStatus(), errorCode.getMessage());

        return new ResponseEntity(errorDto, HttpStatusCode.valueOf(errorDto.getStatus()));
    }

    /* 일반 예외 처리 */
    @ExceptionHandler
    protected ResponseEntity customServerException(Exception ex) {
        ErrorDto error = new ErrorDto(INTERNAL_SERVER_ERROR.getStatus(), INTERNAL_SERVER_ERROR.getMessage());
        return new ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}