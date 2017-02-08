package com.alisiikh.controller.advice;

import com.alisiikh.exception.YouTubeDataFetchException;
import com.alisiikh.exception.YouTubeEntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author lial
 */
@ControllerAdvice
public class GlobalControllerExceptionHandler {

	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Couldn't find YouTube entity information")
	@ExceptionHandler(YouTubeEntityNotFoundException.class)
	public void handleYouTubeEntityNotFound() {
		// nothing
	}

	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Request to YouTube failed with errors, probably because of bad input")
	@ExceptionHandler(YouTubeDataFetchException.class)
	public void handleProblemsContactingYouTube() {
		// nothing
	}
}
