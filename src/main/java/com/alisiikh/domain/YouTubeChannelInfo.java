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
public class YouTubeChannelInfo extends YouTubeEntity {
	private long registrationDate;
	private int subscribers;
	private int views;
}
