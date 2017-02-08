package com.alisiikh.service;

import com.alisiikh.domain.YouTubeChannelInfo;
import com.alisiikh.domain.YouTubeVideoInfo;
import com.alisiikh.domain.YouTubeVideosSearchInfo;
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
import java.util.Locale;
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
		Validate.notBlank(videoId, "Video id is required!");

		try {
			Document doc = fetchDocument("https://www.youtube.com/watch?v=" + videoId);

			return gatherVideoInfo(doc);
		} catch (IOException e) {
			LOG.debug("Exception occurred during fetching video info");
			return null;
		}
	}

	@Override
	public YouTubeChannelInfo getChannelInfo(String channelId) {
		Validate.notBlank(channelId, "Channel id is required!");

		try {
			Document doc = fetchDocument("https://www.youtube.com/channel/" + channelId + "/about");

			return gatherChannelInfo(doc);
		} catch (IOException e) {
			LOG.debug("Exception occurred during fetching channel info");
			return null;
		}
	}

	@Override
	public YouTubeVideosSearchInfo getChannelVideos(String channelId, int size) {
		return new YouTubeVideosSearchInfo();
	}

	private YouTubeChannelInfo gatherChannelInfo(Document doc) {
		YouTubeChannelInfo channelInfo = new YouTubeChannelInfo();
		String channelId = doc.select("meta[itemprop='channelId']").attr("content");
		String url = doc.select("link[itemprop='url']").attr("href");

		Elements channelAttrs = doc.select("#browse-items-primary .about-metadata-stats .about-stats");
		String subscribers = channelAttrs.select(".about-stat:nth-of-type(1) b").text()
				.replaceAll(",", "")
				.trim();
		String totalViews = channelAttrs.select(".about-stat:nth-of-type(2) b").text()
				.replaceAll(",", "")
				.trim();
		String registrationDate = channelAttrs.select(".about-stat:nth-of-type(3)").text()
				.replaceFirst("Joined", "")
				.trim();

		channelInfo.setId(channelId);
		channelInfo.setUrl(url);
		channelInfo.setSubscribers(Integer.valueOf(subscribers));
		channelInfo.setViews(Integer.valueOf(totalViews));

		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
		try {
			channelInfo.setRegistrationDate(sdf.parse(registrationDate).getTime());
		} catch (ParseException e) {
			// nothing
		}

		return channelInfo;
	}

	private Document fetchDocument(String url) throws IOException {
		return Jsoup.connect(url)
				.header("Accept-Language", "en-US")
				.get();
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
		videoInfo.setDuration(convertDurationIntoSeconds(duration));
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

	private int convertDurationIntoSeconds(String durationString) {
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
