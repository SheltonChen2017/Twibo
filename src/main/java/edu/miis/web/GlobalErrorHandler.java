package edu.miis.web;

import edu.miis.service.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalErrorHandler {
    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    ModelAndView notFound() {
        ModelAndView view = new ModelAndView("error");
        view.setStatus(HttpStatus.NOT_FOUND);
        view.addObject("status", HttpStatus.NOT_FOUND.value());
        return view;
    }
}
