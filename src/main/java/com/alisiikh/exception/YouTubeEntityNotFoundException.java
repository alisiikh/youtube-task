package com.alisiikh.exception;

/**
 * @author lial
 */
public class YouTubeEntityNotFoundException extends YouTubeException {

	public YouTubeEntityNotFoundException(String message) {
		super(message);
	}

	public YouTubeEntityNotFoundException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
