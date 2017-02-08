package com.alisiikh.exception;

/**
 * @author lial
 */
public abstract class YouTubeException extends RuntimeException {

	public YouTubeException(String message) {
		super(message);
	}

	public YouTubeException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
