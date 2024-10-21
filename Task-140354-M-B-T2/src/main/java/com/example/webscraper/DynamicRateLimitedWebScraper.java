package com.example.webscraper;

import com.google.common.util.concurrent.RateLimiter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DynamicRateLimitedWebScraper {
    // Initial rate limit (in requests per second)
    private static final double INITIAL_RATE_LIMIT = 10.0;
    // Maximum rate limit (in requests per second)
    private static final double MAX_RATE_LIMIT = 20.0;
    // Minimum rate limit (in requests per second)
    private static final double MIN_RATE_LIMIT = 1.0;
    // Rate limit adjustment factor
    private static final double RATE_LIMIT_ADJUSTMENT_FACTOR = 0.1;
    // Maximum response time threshold (in milliseconds)
    private static final long MAX_RESPONSE_TIME_THRESHOLD = 5000;
    // Minimum response time threshold (in milliseconds)
    private static final long MIN_RESPONSE_TIME_THRESHOLD = 200;

    private final RateLimiter rateLimiter;

    public DynamicRateLimitedWebScraper() {
        this.rateLimiter = RateLimiter.create(INITIAL_RATE_LIMIT);
    }

    public Document scrapeUrl(String url) throws IOException {
        // Acquire a permit before making the request
        rateLimiter.acquire();

        long startTime = System.currentTimeMillis();
        Document document = Jsoup.connect(url).get();
        long responseTime = System.currentTimeMillis() - startTime;

        // Adjust the rate limit based on the response time
        adjustRateLimit(responseTime);

        return document;
    }

    private void adjustRateLimit(long responseTime) {
        double currentRateLimit = rateLimiter.getRate();

        if (responseTime > MAX_RESPONSE_TIME_THRESHOLD) {
            // Slow down if the response time is too slow
            double newRateLimit = Math.max(MIN_RATE_LIMIT, currentRateLimit - (currentRateLimit * RATE_LIMIT_ADJUSTMENT_FACTOR));
            rateLimiter.setRate(newRateLimit);
        } else if (responseTime < MIN_RESPONSE_TIME_THRESHOLD) {
            // Speed up if the response time is too fast
            double newRateLimit = Math.min(MAX_RATE_LIMIT, currentRateLimit + (currentRateLimit * RATE_LIMIT_ADJUSTMENT_FACTOR));
            rateLimiter.setRate(newRateLimit);
        }
    }

    public static void main(String[] args) throws IOException {
        DynamicRateLimitedWebScraper scraper = new DynamicRateLimitedWebScraper();
        String url = "https://example.com"; // Replace this with the URL you want to scrape

        while (true) {
            try {
                Document document = scraper.scrapeUrl(url);
                System.out.println("Scraped content: " + document.title());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

