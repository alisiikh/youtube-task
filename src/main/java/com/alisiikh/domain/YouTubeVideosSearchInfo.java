package com.alisiikh.domain;

import lombok.Data;

import java.util.List;


/**
 * @author lial
 */
@Data
public class YouTubeVideosSearchInfo {
	private YouTubeChannelInfo channelInfo;
	private int requestedVideos;
	private List<YouTubeVideoInfo> videos;
}
