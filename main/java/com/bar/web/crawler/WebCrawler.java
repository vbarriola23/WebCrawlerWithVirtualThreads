package com.bar.web.crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Starts crawling using the seed url links provided in {@code String[] seeds}.
 * Keeps recods of all the visited host to avoid revisiting them in
 * {@code Map<String, CrawlRoot> visitedHostUrlLinks}} Through command line
 * arguments the user can control the number of visits performed and/or the list
 * of searchWords that can be used to match the candidate urls to store in
 * fetchedUrls.
 * 
 * @author
 *
 */
public class WebCrawler {

	private static String[] seeds = { "https://root/domain/page" };
	private static Map<String, CrawlRoot> visitedHostUrlLinks = new ConcurrentHashMap<>();
	private static int MAX_NUM_VISITS = 1000;
	private static int numOfVisits = 0;
	// Once this is initialized is only read only
	private static Set<String> searchWords = new HashSet<>();
	// Set of URLs that have been fetched by the crawler
	private static Set<String> fetchedUrls = new HashSet<>();

	/**
	 * The first argument specifies the max number of visited links, it is preceded
	 * by max_num=*, for example max_num=459999 The following arguments are search
	 * words that must be present on the URL to be included in the results The next
	 * parameters
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		processArguments(args);

		for (String url : seeds) {

			URL urlLink;
			try {

				urlLink = new URL(url);
				String urlBase = urlLink.getProtocol() + "://" + urlLink.getAuthority();
				CrawlRoot cr = new CrawlRoot();
				System.out.println("Put new host to visitedHostUrlLinks: " + urlBase);
				visitedHostUrlLinks.put(urlBase, cr);
				cr.setVisitedLinks(url);
				CrawlJob cj = new CrawlJob(url, urlBase, cr);
				Thread.ofVirtual().start(cj);
			} catch (MalformedURLException e) {

				System.err.println("For '" + url + "': " + e.getMessage());
			}
		}
		while (true) {

			Thread.sleep(2000);
			for (Map.Entry<String, CrawlRoot> entry : visitedHostUrlLinks.entrySet()) {

				numOfVisits = numOfVisits + entry.getValue().getNumberVisits();
			}
			if (numOfVisits > MAX_NUM_VISITS) {
				// the program generates virtual threads. All are daemon threads so when this
				// thread goes....
				break;
			}
		}
		try (FileWriter fw = new FileWriter("fetchedUrl.txt", true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			for (String fetchedUrl : fetchedUrls) {

				out.println(fetchedUrl);
			}
		} catch (IOException e) {

			System.out.println("Failed saving file: " + e.getMessage());
		}
	}

	/**
	 * Process the parameters passed
	 * 
	 * @param args
	 */
	private static void processArguments(String[] args) {

		if (args.length > 0) {

			String maxNum = args[0].toLowerCase();
			if (maxNum.startsWith("max_num=")) {

				try {
					MAX_NUM_VISITS = Integer.parseInt(maxNum.substring("max_num=".length()));
				} catch (NumberFormatException ignore) {
				}
				if (args.length > 1) {
					processSearchItems(Arrays.copyOfRange(args, 1, args.length - 1));
				}
			} else {

				processSearchItems(args);
			}
		}

	}

	/**
	 * This is just to copy the search words
	 * 
	 * @param words
	 */
	private static void processSearchItems(String[] words) {

		for (String word : words) {

			searchWords.add(word);
		}

	}

	/**
	 * Returns {@code CrawlRoot} for the corresponding {@code hostUrl} or null if
	 * not found
	 * 
	 * @param hostUrl
	 * @return
	 */
	public static CrawlRoot getVisitedHostUrlLinks(String hostUrl) {

		return visitedHostUrlLinks.get(hostUrl);
	}

	/**
	 * Sets the hostUrl in the map with the CrawlRoot. Due to the concurrent nature
	 * of the application this can happen more than once but it is harmless it just
	 * means that we might visit a given site more than once
	 * 
	 * @param hostUrl
	 * @param cr
	 */
	public static void setVisitedHostUrlLinks(String hostUrl, CrawlRoot cr) {

		visitedHostUrlLinks.put(hostUrl, cr);
	}

	public static Set<String> getSearchWords() {

		return searchWords;
	}

	public static void appendFetchedUrl(String url) {

		fetchedUrls.add(url);
	}
}
