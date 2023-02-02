# WebCrawlerWithVirtualThreads

This is a case study of writing a web crawler using virtual threads. It is a proof of concept to showcase how more efficent and staightforward is the implementation of a high throughput I/O application using virtual threads.

A web crawler scans the internet for URL references. It connects to each reference, parses the page looking for new URL references, and continues this process indefietly.

For this particular implementation, a breath first seach was used and it ran each search in a unique and distinct virtual thread. Virtual threads have a very small memory footprint, so there is no risk of running out of memory.
