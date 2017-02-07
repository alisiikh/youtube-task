package com.alisiikh.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lial
 */

@EqualsAndHashCode
public class YouTubeChannelInfo extends YouTubeEntity {
	@Getter
	@Setter
	private long registrationDate;
	@Getter
	@Setter
	private int subscribers;
	@Getter
	@Setter
	private int views;
}
