package com.alisiikh.service;

import com.alisiikh.domain.YouTubeChannelInfo;
import com.alisiikh.domain.YouTubeVideoInfo;
import com.alisiikh.domain.YouTubeVideosSearchInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author lial
 */
@Service
public class YouTubeService implements IYouTubeService {

	private static final Logger LOG = LoggerFactory.getLogger(YouTubeService.class);

	private static final Pattern DURATION_PATTERN = Pattern.compile("PT(\\d+)M(\\d+)S");

	private RestTemplate restTemplate;
	private ThreadPoolTaskExecutor taskExecutor;

	@Override
	public YouTubeVideoInfo getVideoInfo(String videoId) {
		// TODO: Possibly use GET https://www.youtube.com/user/{channelId}/about?spf=navigate
		Validate.notBlank(videoId, "Video id is required!");

		try {
			Document doc = fetchDocument("https://www.youtube.com/watch?v=" + videoId);

			return gatherVideoInfo(doc);
//			Map<String, String> responseMap = readDataFromYoutube("https://www.youtube.com/watch?spf=navigate&v=" + videoId);
//			TODO: finish
//			return null;
		} catch (IOException e) {
			LOG.debug("Exception occurred during fetching video info");
			return null;
		}
	}

	private Map<String, String> readDataFromYoutube(String url) throws IOException {
		ResponseEntity<String> jsonData = restTemplate.getForEntity(url, String.class);

		@SuppressWarnings("unchecked")
		Map<String, String> responseMap = (Map<String, String>) new ObjectMapper().readValue(jsonData.getBody(), HashMap.class);
		return responseMap;
	}

	@Override
	public YouTubeChannelInfo getChannelInfo(String channelId) {
		// TODO: Possibly use GET https://www.youtube.com/user/{channelId}/about?spf=navigate
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
		// TODO: Possibly use GET https://www.youtube.com/user/fxigr1/videos?spf=navigate
		// and if more more than 30 videos required,
		// use https://www.youtube.com/browse_ajax?action_continuation=1&continuation=4qmFsgI8EhhVQ3JfZndGLW4tMl9vbFRZZC1tM24zMmcaIEVnWjJhV1JsYjNNZ0FEQUJPQUZnQVdvQWVnRXl1QUVB
		// that could be found in $('#content #browse-items-primary .load-more-button').data('uix-load-more-href');

		try {
			Document doc = fetchDocument("https://www.youtube.com/channel/" + channelId + "/videos?view=0&flow=grid&sort=p");
			YouTubeVideosSearchInfo videosSearchInfo = new YouTubeVideosSearchInfo();
			Future<?> channelInfoTask = taskExecutor.submit(() -> videosSearchInfo.setChannelInfo(getChannelInfo(channelId)));
			Future<?> channelVideosInfoTask = taskExecutor.submit(() -> videosSearchInfo.setVideos(gatherChannelVideosInfo(doc, size)));
			videosSearchInfo.setRequestedVideos(size);

			try {
				channelInfoTask.get();
				channelVideosInfoTask.get();
			} catch (InterruptedException | ExecutionException e) {
				// nothing
			}

			return videosSearchInfo;
		} catch (IOException e) {
			LOG.debug("Exception occurred during fetching channel videos");
			return null;
		}
	}

	private List<YouTubeVideoInfo> gatherChannelVideosInfo(Document doc, int size) {
		Elements videoBlocks = doc.select("#channels-browse-content-grid .channels-content-item");

		if (videoBlocks.size() < size) {
			String relativeHref = doc.select("#browse-items-primary .load-more-button").attr("data-uix-load-more-href");
			ResponseEntity<String> response = restTemplate.getForEntity("https://www.youtube.com" + relativeHref, String.class);

			try {
				@SuppressWarnings("unchecked")
				Map<String, String> responseMap = (Map<String, String>) new ObjectMapper().readValue(response.getBody(), HashMap.class);
				Document extraVideosDoc = Jsoup.parse(responseMap.get("content_html"));
				Elements extraVideoBlocks = extraVideosDoc.select(".channels-content-item");

				videoBlocks.addAll(extraVideoBlocks);
			} catch (IOException e) {
				// nothing
			}
		}

		return gatherVideoInfos(videoBlocks.subList(0, size));
	}

	private List<YouTubeVideoInfo> gatherVideoInfos(List<Element> videoBlocks) {
		return videoBlocks.parallelStream().map((block) -> {
			String videoId = block.select("> .yt-lockup-video").attr("data-context-item-id");

			return getVideoInfo(videoId);
		}).filter(Objects::nonNull).collect(Collectors.toList());
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
			// TODO: Figure out why this happens even for existing videos
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

	@Autowired
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Autowired
	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
}
