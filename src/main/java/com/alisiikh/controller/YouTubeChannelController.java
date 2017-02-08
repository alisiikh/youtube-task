package com.alisiikh.controller;

import com.alisiikh.domain.YouTubeChannelInfo;
import com.alisiikh.domain.YouTubeVideosSearchInfo;
import com.alisiikh.service.IYouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author lial
 */
@RestController
@RequestMapping("/youtube/channel")
public class YouTubeChannelController {

	private IYouTubeService youTubeService;

	@RequestMapping(value = "/{channelId}", method = RequestMethod.GET)
	public YouTubeChannelInfo getChannelInfo(@PathVariable("channelId") String channelId,
			HttpServletResponse response) throws IOException {
		YouTubeChannelInfo channelInfo = youTubeService.getChannelInfo(channelId);

		if (channelInfo == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Channel with id = '" + channelId + "' was not found!");
			return null;
		} else {
			return channelInfo;
		}
	}

	@RequestMapping(value = "/{channelId}/videos")
	public YouTubeVideosSearchInfo getVideosInfo(@PathVariable("channelId") String channelId,
			@RequestParam(defaultValue = "10") int size, HttpServletResponse response) throws IOException {
		if (size > 50) {
			size = 50;
		}

		return youTubeService.getChannelVideos(channelId, size);
	}

	@Autowired
	public void setYouTubeService(IYouTubeService youTubeService) {
		this.youTubeService = youTubeService;
	}
}
