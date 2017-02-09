package com.alisiikh.service;

import com.alisiikh.domain.YouTubeChannelInfo;
import com.alisiikh.domain.YouTubeVideoInfo;
import com.alisiikh.domain.YouTubeVideosSearchInfo;
import com.alisiikh.exception.YouTubeDataFetchException;
import com.alisiikh.exception.YouTubeEntityNotFoundException;
import org.apache.commons.lang3.Validate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.alisiikh.util.UrlUtils.encode;


/**
 * @author lial
 */
@Service
public class YouTubeService implements IYouTubeService {

	private static final Logger LOG = LoggerFactory.getLogger(YouTubeService.class);

	private static final Pattern DURATION_PATTERN = Pattern.compile("PT(\\d+)M(\\d+)S");
	private static final String YOUTUBE_WEBSITE_ADDRESS = "https://www.youtube.com";
	private static final DateTimeFormatter PUBLISHED_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
	private static final DateTimeFormatter REGISTERED_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy",
			Locale.US);

	private RestTemplate restTemplate;
	private ThreadPoolTaskExecutor taskExecutor;

	@Override
	public YouTubeVideoInfo getVideoInfo(String videoId) {
		Validate.notBlank(videoId, "Video id is required!");

		try {
			Optional<JSONArray> jsonArray = readDataFromYoutube("/watch?spf=navigate&v=" + encode(videoId));
			if (!jsonArray.isPresent()) {
				LOG.warn("Failed to get data from YouTube");
				return null;
			}

			JSONObject nestedObj = (JSONObject) jsonArray.get().get(3);
			String contentHtml = (String) ((JSONObject) nestedObj.get("body"))
					.get("watch7-container");

			Document doc = Jsoup.parseBodyFragment(contentHtml);

			return findVideoInfo(doc);
		} catch (IOException e) {
			LOG.warn("Exception occurred during fetching video info", e);
			throw new YouTubeDataFetchException();
		}
	}

	private <T> Optional<T> readDataFromYoutube(String relativeUrl) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Accept-Language", "en-US");

		HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);
		ResponseEntity<String> jsonData;

		String requestUrl = YOUTUBE_WEBSITE_ADDRESS + relativeUrl;
		try {
			jsonData = restTemplate.exchange(requestUrl, HttpMethod.GET, httpEntity, String.class);

		} catch (HttpClientErrorException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				LOG.debug("Requested entity couldn't be found");
				throw new YouTubeEntityNotFoundException("Failed to find anything against the following url: "
						+ requestUrl);
			} else {
				LOG.warn("Unknown error occurred during communicating with YouTube", e);
				throw new YouTubeDataFetchException(e);
			}
		}

		JSONParser jsonParser = new JSONParser();

		try {
			@SuppressWarnings("unchecked")
			T json = (T) jsonParser.parse(jsonData.getBody());

			return Optional.of(json);
		} catch (org.json.simple.parser.ParseException e) {
			LOG.warn("Failed to parse JSON data passed by YouTube.", e);

			return Optional.empty();
		}
	}

	@Override
	public YouTubeChannelInfo getChannelInfo(String channelId) {
		Validate.notBlank(channelId, "Channel id is required!");

		try {
			Optional<JSONObject> jsonObj = readDataFromYoutube("/channel/" + encode(channelId) + "/about?spf=navigate");
			if (!jsonObj.isPresent()) {
				throw new YouTubeDataFetchException();
			}

			String contentHtml = (String) ((JSONObject) jsonObj.get().get("body")).get("content");

			Document doc = Jsoup.parseBodyFragment(contentHtml);

			YouTubeChannelInfo channelInfo = findChannelInfo(doc);
			channelInfo.setId(channelId);
			channelInfo.setUrl(YOUTUBE_WEBSITE_ADDRESS + "/channel/" + encode(channelId));

			return channelInfo;
		} catch (IOException e) {
			throw new YouTubeDataFetchException(e);
		}
	}

	@Override
	public YouTubeVideosSearchInfo getMostPopularVideosOfChannel(String channelId, int size) {
			Validate.isTrue(size > 0 && size <= 50);

			YouTubeVideosSearchInfo videosSearchInfo = new YouTubeVideosSearchInfo();

			CountDownLatch latch = new CountDownLatch(2);

			taskExecutor.submit(() -> {
				try {
					videosSearchInfo.setChannelInfo(getChannelInfo(channelId));
				} finally {
					latch.countDown();
				}
			});
			taskExecutor.submit(() -> {
				try {
					Optional<JSONObject> jsonObject = readDataFromYoutube("/channel/" + channelId
							+ "/videos?sort=p&flow=grid&view=0&spf=navigate");
					if (!jsonObject.isPresent()) {
						throw new YouTubeDataFetchException("Failed to read data from YouTube");
					}
					String contentHtml = (String) ((JSONObject) jsonObject.get().get("body")).get("content");

					Document doc = Jsoup.parseBodyFragment(contentHtml);

					videosSearchInfo.setVideos(gatherChannelVideosInfo(doc, size));
				} catch (IOException e) {
					LOG.warn("Exception occurred during fetching channel videos");
				} finally {
					latch.countDown();
				}
			});
			videosSearchInfo.setRequestedVideos(size);

			try {
				latch.await(20, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOG.debug("Failed to finish channel's most popular videos search");
				throw new YouTubeDataFetchException(e);
			}

			return videosSearchInfo;
	}

	private List<YouTubeVideoInfo> gatherChannelVideosInfo(Document doc, int size) {
		Elements videoBlocks = doc.select("#channels-browse-content-grid .channels-content-item");

		int currentSize = videoBlocks.size();

		if (currentSize < size) {
			String loadMoreVideosHref = doc.select("#browse-items-primary .load-more-button")
					.attr("data-uix-load-more-href");

			while (currentSize < size) {
				try {
					Optional<JSONObject> jsonObj = readDataFromYoutube(loadMoreVideosHref);
					if (!jsonObj.isPresent()) {
						throw new YouTubeDataFetchException();
					}

					String contentHtml = (String) jsonObj.get().get("content_html");
					String moreVideosButtonHtml = (String) jsonObj.get().get("load_more_widget_html");

					loadMoreVideosHref = Jsoup.parseBodyFragment(moreVideosButtonHtml).body().select(".load-more-button")
							.attr("data-uix-load-more-href");

					Element extraVideosBody = Jsoup.parseBodyFragment(contentHtml).body();
					Elements extraVideoBlocks = extraVideosBody.select(".channels-content-item");

					videoBlocks.addAll(extraVideoBlocks);
					currentSize += extraVideoBlocks.size();
				} catch (IOException e) {
					// nothing
				}
			}
		}

		return videoBlocks.subList(0, size).parallelStream().map((block) -> {
			String videoId = block.select("> .yt-lockup-video")
					.attr("data-context-item-id");

			return getVideoInfo(videoId);
		}).collect(Collectors.toList());
	}

	private YouTubeChannelInfo findChannelInfo(Document doc) {
		YouTubeChannelInfo channelInfo = new YouTubeChannelInfo();

		Elements channelAttrs = doc.select("#browse-items-primary .about-metadata-stats .about-stats");
		String subscribers = channelAttrs.select(".about-stat:nth-of-type(1) b").text()
				.replaceAll(",", "")
				.trim();
		String totalViews = channelAttrs.select(".about-stat:nth-of-type(2) b").text()
				.replaceAll(",", "")
				.trim();
		String dateRegistered = channelAttrs.select(".about-stat:nth-of-type(3)").text()
				.replaceFirst("Joined", "")
				.trim();

		channelInfo.setSubscribers(Integer.valueOf(subscribers));
		channelInfo.setViews(Integer.valueOf(totalViews));

		try {
			LocalDate registeredDate = LocalDate.parse(dateRegistered, REGISTERED_DATE_FORMAT);
			long registeredDateMillis = registeredDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;

			channelInfo.setRegistrationDate(registeredDateMillis);
		} catch (DateTimeParseException e) {
			LOG.debug("Failed to parse registration date string: " + dateRegistered);
		}

		return channelInfo;
	}

	private YouTubeVideoInfo findVideoInfo(Document doc) {
		YouTubeVideoInfo videoInfo = new YouTubeVideoInfo();

		String videoId = doc.select("meta[itemprop='videoId']")
				.attr("content");

		String url = doc.select("link[itemprop='url']")
				.attr("href");
		String views = doc.select("meta[itemprop='interactionCount']")
				.attr("content");
		String datePublished = doc.select("meta[itemprop='datePublished']")
				.attr("content");
		String title = doc.select("meta[itemprop='name']")
				.attr("content");
		String duration = doc.select("meta[itemprop='duration']")
				.attr("content");

		videoInfo.setId(videoId);
		videoInfo.setTitle(title);
		videoInfo.setDuration(convertDurationIntoSeconds(duration));
		videoInfo.setViews(Integer.valueOf(views));
		videoInfo.setUrl(url);

		try {
			LocalDate publishedDate = LocalDate.parse(datePublished, PUBLISHED_DATE_FORMAT);
			long publishedDateMillis = publishedDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;

			videoInfo.setPublishedDate(publishedDateMillis);
		} catch (DateTimeParseException e) {
			LOG.debug("Failed to parse published date of the video");
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
