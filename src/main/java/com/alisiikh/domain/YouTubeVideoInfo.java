package com.alisiikh.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author lial
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class YouTubeVideoInfo extends YouTubeEntity {
	private String title;
	private int duration;
	private long publishedDate;
	private int views;
}
