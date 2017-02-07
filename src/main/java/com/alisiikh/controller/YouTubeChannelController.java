package com.alisiikh.controller;

import com.alisiikh.domain.YouTubeChannelInfo;
import com.alisiikh.service.IYouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author lial
 */
@RestController
@RequestMapping("/youtube/channel")
public class YouTubeChannelController {

	private IYouTubeService youTubeService;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public YouTubeChannelInfo getChannelInfo(@PathVariable(name = "id") String channelId,
			HttpServletResponse response) throws IOException {
		YouTubeChannelInfo channelInfo = youTubeService.getChannelInfo(channelId);

		if (channelInfo == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		} else {
			return channelInfo;
		}
	}

	@Autowired
	public void setYouTubeService(IYouTubeService youTubeService) {
		this.youTubeService = youTubeService;
	}
}
