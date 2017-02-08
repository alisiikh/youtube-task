package com.alisiikh.exception;


/**
 * @author lial
 */
public class YouTubeDataFetchException extends YouTubeException {

	private static final String DEFAULT_ERROR_MESSAGE = "Failed to fetch data from YouTube";

	public YouTubeDataFetchException() {
		super(DEFAULT_ERROR_MESSAGE);
	}

	public YouTubeDataFetchException(String message) {
		super(message);
	}

	public YouTubeDataFetchException(Throwable throwable) {
		super(DEFAULT_ERROR_MESSAGE, throwable);
	}

	public YouTubeDataFetchException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
