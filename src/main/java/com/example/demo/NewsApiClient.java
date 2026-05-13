package com.example.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class NewsApiClient {
	private static final String RSS_URL = "https://news.google.com/rss?hl=ja&gl=JP&ceid=JP:ja";
    private static final HttpClient CLIENT = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    public List<NewsItem> fetchNewsItems() throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(RSS_URL)).header("User-Agent", "Mozilla/5.0").build();
        HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String xml = resp.body();

        List<NewsItem> items = new ArrayList<>();
        Matcher m = Pattern.compile("<item>.*?<title>(.*?)</title>.*?<link>(.*?)</link>", Pattern.DOTALL).matcher(xml);
        int count = 0;
        while (m.find() && count < 10) {
            String title = m.group(1).replace("&amp;", "&").replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">");
            items.add(new NewsItem(title, m.group(2)));
            count++;
        }
        return items;
    }

    public static class NewsItem {
        private String title, link;
        public NewsItem(String title, String link) { this.title = title; this.link = link; }
        public String getTitle() { return title; }
        public String getLink() { return link; }
    }
}