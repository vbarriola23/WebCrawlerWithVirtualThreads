package com.bar.web.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encapsulates the visited links for a given host. Additionally it parses and applies the rules in robots.txt
 * for the given host to find the disallowed URLs
 * @author 
 *
 */
public class CrawlRoot {

	private Set<String> visitedLinks = ConcurrentHashMap.newKeySet();
	private AtomicInteger numVisits = new AtomicInteger(0);
	private Set<String> disallowedUrls = new HashSet<>();

	public CrawlRoot() {
	}

	/**
	 * Reads robots.txt
	 * @param baseUrl
	 */
	public void initializeDisallowedUrls(String baseUrl) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(baseUrl + "/robots.txt").openStream()))) {
				
			parseRobots(baseUrl, reader);
		} catch (Exception ignore) {// if file is not there means we have no restrictions
		}
	}

	/**
	 * Sets the disallowed urls comming from robots.txt into the Set {@code disallowedUrls}
	 * @param baseUrl
	 * @param reader
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void parseRobots(String baseUrl, BufferedReader reader) throws IOException, URISyntaxException {
		// Read file to find disallowed paths
		String line;
		boolean performParse = false;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line .startsWith("User-agent: *")) {
				performParse = true;
			} else if (performParse == true && line.startsWith("user-agent:")) {
				performParse = false;
			}
			if (performParse == true && line.startsWith("Disallow:")) {

				int initIndex = "Disallow:".length();
				int commentIndex = line.indexOf("#");
				if (commentIndex == -1) {
					commentIndex = line.length() - 1;
				}
				String disallowPath = line.substring(initIndex, commentIndex);
				disallowPath = disallowPath.trim();
				if (disallowPath.startsWith("http") == false)// relative path
					disallowPath = baseUrl + disallowPath;
				// Check that URL is properly formated
				try {
					new URL(disallowPath).toURI();
				} catch (MalformedURLException ex) {

					System.out.println("Malformed URL: " + ex.getMessage());
				}
				disallowedUrls.add(disallowPath);
			}
		}
	}

	public boolean isPathDisallowed(String url) {

		for (String disallowedUrl : disallowedUrls) {
			
			if (url.startsWith(disallowedUrl)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean setVisitedLinks(String url) {

		System.out.println("setVisitedLinks invoked with url: " + url);
		// no need to synchronize this block the number of visits is approximate
		numVisits.incrementAndGet();
		return visitedLinks.add(url);
	}
	
	public Set<String> getDisallowedUrls() {
		
		return disallowedUrls;
	}

	public int getNumberVisits() {

		return numVisits.get();
	}
}
