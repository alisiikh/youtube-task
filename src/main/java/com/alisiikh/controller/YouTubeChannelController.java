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
	public YouTubeChannelInfo getChannelInfo(@PathVariable("channelId") String channelId) throws IOException {
		return youTubeService.getChannelInfo(channelId);
	}

	@RequestMapping(value = "/{channelId}/videos", method = RequestMethod.GET)
	public YouTubeVideosSearchInfo getVideosInfo(@PathVariable("channelId") String channelId,
			@RequestParam(defaultValue = "10") int size, HttpServletResponse response) throws IOException {
		if (size <= 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Size parameter should be in bounds of (0,50]");
			return null;
		}

		return youTubeService.getMostPopularVideosOfChannel(channelId, size > 50 ? 50 : size);
	}

	@Autowired
	public void setYouTubeService(IYouTubeService youTubeService) {
		this.youTubeService = youTubeService;
	}
}
