package com.alisiikh.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lial
 */

@EqualsAndHashCode
public class YouTubeVideoInfo extends YouTubeEntity {
	@Getter
	@Setter
	private String title;
	@Getter
	@Setter
	private int duration;
	@Getter
	@Setter
	private long publishedDate;
	@Getter
	@Setter
	private int views;
}
