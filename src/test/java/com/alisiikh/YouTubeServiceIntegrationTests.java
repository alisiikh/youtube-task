package com.alisiikh;

import com.alisiikh.domain.YouTubeChannelInfo;
import com.alisiikh.domain.YouTubeVideoInfo;
import com.alisiikh.domain.YouTubeVideosSearchInfo;
import com.alisiikh.service.YouTubeService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * @author lial
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class YouTubeServiceIntegrationTests {

	private static final List<String> AVAILABLE_VIDEOS = Arrays.asList("Xm-KjMY_Z_w", "QOR69q1e63Y", "j9nj5dTo54Q",
			"wk3WLaR2V2U", "we_enrM7TSY", "xYnS9PQRXTg", "Zv_mnjYhFAk", "GRPLRONVDWY", "A0goyZ9F4bg", "47xNBNd-LLI");
	private static final List<String> AVAILABLE_CHANNELS = Arrays.asList("UCt7sv-NKh44rHAEb-qCCxvA", "UC7yfnfvEUlXUIfm8rGLwZdA",
			"UCNvzD7Z-g64bPXxGzaQaa4g", "UCPT9_sNLoBLjH1uea7zpVIA", "UCdDhYMT2USoLdh4SZIsu_1g", "UCCBVCTuk6uJrN3iFV_3vurg",
			"UCGfWLin_JpKts2DWtGoXtKA", "UCCE-5_0xSRYkIprHZYaTs9Q", "UC4w_tMnHl6sw5VD93tVymGw", "UCGp4UBwpTNegd_4nCpuBcow");

	@Autowired
	private YouTubeService youTubeService;

	private Executor executor = Executors.newFixedThreadPool(100);

	@Test
	public void testServiceCanHandleMultipleRequestsOnVideos() throws InterruptedException {
		final List<YouTubeVideoInfo> videoInfos = new CopyOnWriteArrayList<>();
		final int requestsCount = 1000;
		final CountDownLatch latch = new CountDownLatch(requestsCount);

		for (int i = 0; i < requestsCount; i++) {
			int randomNum = ThreadLocalRandom.current().nextInt(0, AVAILABLE_VIDEOS.size());
			String videoId = AVAILABLE_VIDEOS.get(randomNum);

			executor.execute(() -> {
				try {
					YouTubeVideoInfo info = youTubeService.getVideoInfo(videoId);

					videoInfos.add(info);
				} finally {
					latch.countDown();
				}
			});
		}

		// wait until all video requests are done
		latch.await();

		assertTrue(videoInfos.size() == requestsCount);

		for (YouTubeVideoInfo videoInfo : videoInfos) {
			assertTrue(StringUtils.isNotBlank(videoInfo.getId()));
			assertTrue(StringUtils.isNotBlank(videoInfo.getUrl()));
			assertTrue(StringUtils.isNotBlank(videoInfo.getTitle()));

			assertTrue(videoInfo.getViews() != 0);
			assertTrue(videoInfo.getPublishedDate() != 0);
			assertTrue(videoInfo.getDuration() != 0);
		}
	}

	@Test
	public void testServiceCanHandleMultipleChannelInfoRequests() throws InterruptedException {
		final List<YouTubeChannelInfo> channelInfos = new CopyOnWriteArrayList<>();
		final int requestsCount = 1000;
		final CountDownLatch latch = new CountDownLatch(requestsCount);

		for (int i = 0; i < requestsCount; i++) {
			int randomNum = ThreadLocalRandom.current().nextInt(0, AVAILABLE_CHANNELS.size());
			String channelId = AVAILABLE_CHANNELS.get(randomNum);

			executor.execute(() -> {
				try {
					YouTubeChannelInfo info = youTubeService.getChannelInfo(channelId);

					channelInfos.add(info);
				} finally {
					latch.countDown();
				}
			});
		}

		// wait until all video requests are done
		latch.await();

		assertTrue(channelInfos.size() == requestsCount);

		for (YouTubeChannelInfo channelInfo : channelInfos) {
			assertTrue(StringUtils.isNotBlank(channelInfo.getId()));
			assertTrue(StringUtils.isNotBlank(channelInfo.getUrl()));
			assertTrue(channelInfo.getSubscribers() != 0);
			assertTrue(channelInfo.getViews() != 0);
			assertTrue(channelInfo.getRegistrationDate() != 0);
		}
	}

	@Test
	public void testChannelMostPopularVideosEndpoint() {
		int videosSize = 50;
		YouTubeVideosSearchInfo searchInfo = youTubeService.getMostPopularVideosOfChannel("UC7yfnfvEUlXUIfm8rGLwZdA", videosSize);

		YouTubeChannelInfo channelInfo = searchInfo.getChannelInfo();
		assertEquals(channelInfo.getRegistrationDate(), 1295474400000L);
		assertTrue(channelInfo.getViews() >= 6239991);
		assertEquals(channelInfo.getId(), "UC7yfnfvEUlXUIfm8rGLwZdA");
		assertEquals(channelInfo.getUrl(), "https://www.youtube.com/channel/UC7yfnfvEUlXUIfm8rGLwZdA");
		assertTrue(channelInfo.getSubscribers() != 0);

		assertTrue(searchInfo.getRequestedVideos() == videosSize);
		assertTrue(searchInfo.getVideos().size() == videosSize);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeSizeParamIsDisallowedForMostPopularVideosSearch() {
		youTubeService.getMostPopularVideosOfChannel("UC7yfnfvEUlXUIfm8rGLwZdA", -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBigSizeIsDisallowedForMostPopularVideosSearch() {
		youTubeService.getMostPopularVideosOfChannel("UC7yfnfvEUlXUIfm8rGLwZdA", 51);
	}
}
