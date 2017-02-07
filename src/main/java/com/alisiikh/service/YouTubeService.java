package com.alisiikh.service;

import com.alisiikh.domain.YouTubeChannelInfo;
import com.alisiikh.domain.YouTubeVideoInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lial
 */
@Service
public class YouTubeService implements IYouTubeService {

	private static final Logger LOG = LoggerFactory.getLogger(YouTubeService.class);

	private static final Pattern DURATION_PATTERN = Pattern.compile("PT(\\d+)M(\\d+)S");
	@Override
	public YouTubeVideoInfo getVideoInfo(String videoId) {
		Validate.notBlank(videoId);

		try {
			Document doc = Jsoup.connect("https://www.youtube.com/watch?v=" + videoId).get();

			return gatherVideoInfo(doc);
		} catch (IOException e) {
			LOG.debug("Failed to find video on youtube website");
			return null;
		}
	}

	@Override
	public YouTubeChannelInfo getChannelInfo(String channelId) {
		// TODO: Implement channel info fetching
		return new YouTubeChannelInfo();
	}

	private YouTubeVideoInfo gatherVideoInfo(Document doc) {
		YouTubeVideoInfo videoInfo = new YouTubeVideoInfo();

		Elements content = doc.select("#content");
		String videoId = content.select("meta[itemprop='videoId']")
				.attr("content");

		// this means video was not found
		if (StringUtils.isBlank(videoId)) {
			return null;
		}

		String url = content.select("link[itemprop='url']")
				.attr("href");
		String views = content.select("meta[itemprop='interactionCount']")
				.attr("content");
		String datePublished = content.select("meta[itemprop='datePublished']")
				.attr("content");
		String title = content.select("meta[itemprop='name']")
				.attr("content");
		String duration = content.select("meta[itemprop='duration']")
				.attr("content");

		videoInfo.setId(videoId);
		videoInfo.setTitle(title);
		videoInfo.setDuration(parseDuration(duration));
		videoInfo.setViews(Integer.valueOf(views));
		videoInfo.setUrl(url);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			videoInfo.setPublishedDate(sdf.parse(datePublished).getTime());
		} catch (ParseException e) {
			// nothing
		}

		return videoInfo;
	}

	private int parseDuration(String durationString) {
		Matcher m = DURATION_PATTERN.matcher(durationString);
		if (m.find()) {
			int minutes = Integer.valueOf(m.group(1));
			int seconds = Integer.valueOf(m.group(2));
			return minutes * 60 + seconds;
		} else {
			return 0;
		}
	}
}
