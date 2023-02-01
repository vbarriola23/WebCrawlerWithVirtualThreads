package com.bar.web.crawler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

public class CrawlRootTest {

	@Test
	public void robotsTest() throws FileNotFoundException, IOException, URISyntaxException {
		
		CrawlRoot cr = new CrawlRoot();
		try (FileReader fr = new FileReader(new File(this.getClass().getResource("/robots.txt").getFile()));
				BufferedReader br = new BufferedReader(fr);) {
			
			cr.parseRobots("https://example.com", br);
			
			assertEquals("Disalowed list", 24, cr.getDisallowedUrls().size());
			assertTrue("Disallowed URL", cr.isPathDisallowed("https://example.com/links/source"));
			assertTrue("Disallowed URL", cr.isPathDisallowed("https://example.com/links/source/foo"));
			assertFalse("Allowed URL" , cr.isPathDisallowed("https://example.com/foo"));
			assertFalse("Allowed URL" , cr.isPathDisallowed("https://example.com/foo/myFrouis.txt"));
		}
	}
}
