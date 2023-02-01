package com.bar.web.crawler;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

public class WebCrawlerTest {

	@Before
	public void setup() {

	}

	@Test
	public void jsoupParsingTest() throws IOException {

		URL fileUrl = this.getClass().getResource("/html.txt");
		File file = new File(fileUrl.getFile());
		Document document = Jsoup.parse(file);
		Elements links = document.select("a[href]");
		List<String> absUrls = new ArrayList<>();
		List<String> baseUrls = new ArrayList<>();
		List<String> relUrls = new ArrayList<>();
		for (Element element : links) {

			//absUrls.add(element.attr("abs:href"));
			String url = element.attr("href");
			if (url.startsWith("http") == true) {
				URL urlLink = new URL(url);
				String urlRoot = urlLink.getProtocol() + "://" + urlLink.getAuthority();
				baseUrls.add(urlRoot);
				absUrls.add(url);
			} else if (url.startsWith("/")) {
				relUrls.add(element.attr("href"));
			}
		}
		assertEquals("absolute urls", 8, absUrls.size());
		assertEquals("base urls", 8, absUrls.size());
		assertEquals("relative urls", 111, relUrls.size());
	}
}
