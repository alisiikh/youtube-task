package com.alisiikh.controller;

import com.alisiikh.domain.YouTubeVideoInfo;
import com.alisiikh.service.IYouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author lial
 */
@RestController
@RequestMapping(value = "/youtube/video")
public class YouTubeVideoController {

	private IYouTubeService youTubeService;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public YouTubeVideoInfo getVideoInfo(@PathVariable(name = "id") String videoId,
			HttpServletResponse response) throws IOException {
		YouTubeVideoInfo videoInfo = youTubeService.getVideoInfo(videoId);

		if (videoInfo == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		} else {
			return videoInfo;
		}
	}

	@Autowired
	public void setYouTubeService(IYouTubeService youTubeService) {
		this.youTubeService = youTubeService;
	}
}
