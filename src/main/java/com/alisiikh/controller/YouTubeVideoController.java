package com.alisiikh.controller;

import com.alisiikh.domain.YouTubeVideoInfo;
import com.alisiikh.service.IYouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author lial
 */
@RestController
@RequestMapping("/youtube/video")
public class YouTubeVideoController {

	private IYouTubeService youTubeService;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public YouTubeVideoInfo getVideoInfo(@PathVariable(name = "id") String videoId) throws IOException {
		return youTubeService.getVideoInfo(videoId);
	}

	@Autowired
	public void setYouTubeService(IYouTubeService youTubeService) {
		this.youTubeService = youTubeService;
	}
}
