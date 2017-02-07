package com.alisiikh.service;

import com.alisiikh.domain.YouTubeChannelInfo;
import com.alisiikh.domain.YouTubeVideoInfo;

/**
 * @author lial
 */
public interface IYouTubeService {

	YouTubeVideoInfo getVideoInfo(String videoId);

	YouTubeChannelInfo getChannelInfo(String channelId);
}
