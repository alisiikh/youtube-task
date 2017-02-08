package com.alisiikh.service;

import com.alisiikh.domain.YouTubeChannelInfo;
import com.alisiikh.domain.YouTubeVideoInfo;
import com.alisiikh.domain.YouTubeVideosSearchInfo;

/**
 * @author lial
 */
public interface IYouTubeService {

	YouTubeVideoInfo getVideoInfo(String videoId);

	YouTubeChannelInfo getChannelInfo(String channelId);

	YouTubeVideosSearchInfo getChannelVideos(String channelId, int size);
}
