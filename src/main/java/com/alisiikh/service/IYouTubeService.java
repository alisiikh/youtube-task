package com.alisiikh.service;

import com.alisiikh.domain.YouTubeVideoInfo;

/**
 * @author lial
 */
public interface YouTubeService {
	
	YouTubeVideoInfo getVideoInfo(String videoId);
}
