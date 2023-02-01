package com.bar.web.crawler;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * It's run by a virtual thread. It connects to the provided URL, parses all the
 * hrefs of the anchors of the html file, searches in the link for matches in
 * {@code WebCrawler#getSearchWords()} and if allowed it starts a new virtual
 * thread to continue the process. It does a breath first search to capture all
 * the Urls and stores them in {@code WebCrawler#fetchedUrls}, when the process
 * finishes it stores them in a file
 * 
 * @author
 *
 */
public class CrawlJob implements Runnable {

	private String url;
	private String baseUrl;
	private CrawlRoot crawlRoot;

	/**
	 * New {@code CrawlJob} to possibly spawn a new virtual thread if conditions
	 * apply
	 * 
	 * @param url
	 * @param baseUrl
	 * @param crawlRoot
	 */
	public CrawlJob(String url, String baseUrl, CrawlRoot crawlRoot) {

		this.url = url;
		this.baseUrl = baseUrl;
		this.crawlRoot = crawlRoot;
	}

	/**
	 * Runnable Interface, run invoked by the spawning of a new virtual thread
	 */
	@Override
	public void run() {

		try {
			Document document = Jsoup.connect(url).get();
			// We connected to a valid URL
			WebCrawler.appendFetchedUrl(url);
			Elements links = document.select("a[href]");
			for (Element element : links) {

				String newUrl = element.attr("href");
				if (findSearchWords(element) == false) {

					continue;
				}
				if (newUrl.startsWith("http") == true) {
					// The newUrl is an absolute link
					URL urlLink = new URL(newUrl);
					String newBaseUrl = urlLink.getProtocol() + "://" + urlLink.getAuthority();
					if (newBaseUrl.equalsIgnoreCase(baseUrl)) {
						// the base of newUrl is already visited
						spawnNewVirtualThread(newUrl, crawlRoot);
					} else {
						// the base of newUrl has not been visited
						CrawlRoot cr = WebCrawler.getVisitedHostUrlLinks(newBaseUrl);
						if (cr == null) {
							// the newBaseUrl has NOT been visited by the crawler
							cr = new CrawlRoot();
							cr.initializeDisallowedUrls(newBaseUrl);
							WebCrawler.setVisitedHostUrlLinks(newBaseUrl, cr);
							spawnNewVirtualThread(newUrl, cr);
						} else {// cr is not null
							// the newBaseUrl has been visited by the crawler
							spawnNewVirtualThread(newUrl, cr);
						}
					}
				} else if (newUrl.startsWith("/")) {
					// newUrl is a relative path, presumably to the baseUrl
					newUrl = baseUrl + newUrl;
					spawnNewVirtualThread(newUrl, crawlRoot);
				} else {
					// At this point we don't process this
					System.out.println("Ignoring url: " + newUrl);
				}
			}
		} catch (IOException e) {
			System.err.println("For '" + url + "': " + e.getMessage());
		}
	}

	/**
	 * Check if the given {@code findSearchWords(element)} contains the word
	 * specified in the set of SearchWords specified by WebCrawler
	 * 
	 * @param element
	 * @return true if found, if there are no SearchWords it also returns true
	 */
	private boolean findSearchWords(Element element) {

		Set<String> searchWords = WebCrawler.getSearchWords();
		if (searchWords.size() == 0)
			return true;
		boolean found = false;
		for (String word : searchWords) {
			// simple implementation for demonstration purposes only
			if (element.text().contains(word) == true) {
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * Spawn a new virtual thread if it's allowed by Robots.txt and it has not been
	 * already visited
	 * 
	 * @param newUrl
	 * @param cr
	 */
	private void spawnNewVirtualThread(String newUrl, CrawlRoot cr) {
		if (!cr.isPathDisallowed(newUrl) && cr.setVisitedLinks(newUrl)) {
			// path is allowed by Robots.txt and newUrl has not been visited
			CrawlJob cj = new CrawlJob(newUrl, baseUrl, crawlRoot);
			Thread.ofVirtual().start(cj);
		} else {
			// Ignore the url has already been visited
		}
	}
}
