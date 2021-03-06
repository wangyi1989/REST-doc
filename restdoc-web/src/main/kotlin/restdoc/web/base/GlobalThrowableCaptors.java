package restdoc.web.base;

import com.google.common.base.Joiner;
import com.google.common.base.VerifyException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import restdoc.web.base.auth.AuthException;
import restdoc.web.core.ServiceException;
import restdoc.web.core.Status;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.stream.Collectors;

import static restdoc.web.core.StandardKt.failure;


/**
 * @author maxuefeng
 * @since 2019/11/18
 */
@ControllerAdvice
public class GlobalThrowableCaptors {

    @ExceptionHandler(value = ServiceException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Object handlerServiceException(ServiceException e) {
        e.printStackTrace();
        return failure(Status.BAD_REQUEST, Objects.requireNonNull(e.getMessage()));
    }

    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Object handlerMethodArgBindException(BindException e, HttpServletResponse response) {
        BindingResult result = e.getBindingResult();

        String errMsg = Joiner.on(";").join(
                result.getAllErrors().stream()
                        .filter(or -> or instanceof FieldError)
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.toList())
        );
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        return failure(Status.BAD_REQUEST, errMsg);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Object handlerConstraintViolationException(ConstraintViolationException e, HttpServletResponse response) {
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return failure(Status.BAD_REQUEST, e.getMessage());
    }


    @ExceptionHandler(value = VerifyException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Object handlerVerifyException(VerifyException e, HttpServletResponse response) {
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return failure(Status.BAD_REQUEST, e.getMessage());
    }


    @ExceptionHandler(value = AuthException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Object handlerAuthException(AuthException e, HttpServletResponse response) {
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return failure(Status.FORBIDDEN, "无权限");
    }

    @ExceptionHandler(value = NoHandlerFoundException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Object handlerNotFoundException(NoHandlerFoundException e, HttpServletResponse response) {
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return failure(Status.BAD_REQUEST, e.getMessage());
    }


    @ExceptionHandler(value = {Throwable.class})
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Object handlerGlobalException(HttpServletRequest request, Throwable e, HttpServletResponse response) {
        e.printStackTrace();
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return failure(Status.BAD_REQUEST, e.getMessage());
    }
}
