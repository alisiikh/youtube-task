package com.alisiikh.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lial
 */
@EqualsAndHashCode
public abstract class YouTubeEntity {
	@Getter
	@Setter
	private String id;
	@Getter
	@Setter
	private String url;
}
