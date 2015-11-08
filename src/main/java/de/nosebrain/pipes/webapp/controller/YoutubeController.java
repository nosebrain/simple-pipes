package de.nosebrain.pipes.webapp.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rometools.rome.feed.synd.SyndEnclosureImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;

@Controller
@RequestMapping("/youtube")
public class YoutubeController {

  private static final String YOUTUBE_FEED_ADDRESS = "https://www.youtube.com/feeds/videos.xml";
  private static final Namespace YOUTUBE_NAMESPACE = Namespace.getNamespace("yt", "http://www.youtube.com/xml/schemas/2015");

  @RequestMapping("/users/{username}")
  public void userFeed(@PathVariable("username") final String username, @RequestParam(value = "title", required = false) final String title, @RequestParam("serviceUrl") final String serviceUrl, final HttpServletResponse response) throws IOException, IllegalArgumentException, FeedException {
    final String feedUrl = YOUTUBE_FEED_ADDRESS + "?user=" + username;
    modifyFeed(title, serviceUrl, response, feedUrl);
  }
  
  @RequestMapping("/channels/{channelId}")
  public void channelFeed(@PathVariable("channelId") final String channelId, @RequestParam(value = "title", required = false) final String title, @RequestParam("serviceUrl") final String serviceUrl, final HttpServletResponse response) throws IOException, IllegalArgumentException, FeedException {
    final String feedUrl = YOUTUBE_FEED_ADDRESS + "?channel_id=" + channelId;
    modifyFeed(title, serviceUrl, response, feedUrl);
  }
  
  private static void modifyFeed(final String title, final String serviceUrl, final HttpServletResponse response, final String feedUrl) throws MalformedURLException, FeedException, IOException {
    final URL url = new URL(feedUrl);
    final SyndFeedInput input = new SyndFeedInput();
    final SyndFeed feed = input.build(new XmlReader(url));
    if (title != null) {
      feed.setTitle(title);
    }
    
    for (final SyndEntry entry : feed.getEntries()) {
      final List<Element> foreignMarkup = entry.getForeignMarkup();
      final Element youtubeIdElement = getForeignMarkup("videoId", YOUTUBE_NAMESPACE, foreignMarkup);
      final String youtubeId = youtubeIdElement.getValue();
      final SyndEnclosureImpl linkToVideo = new SyndEnclosureImpl();
      linkToVideo.setUrl(serviceUrl + youtubeId);
      linkToVideo.setType("video/mp4");
      entry.getEnclosures().add(linkToVideo);
    }
    
    final SyndFeedOutput output = new SyndFeedOutput();
    response.setCharacterEncoding("UTF-8");
    output.output(feed, response.getWriter());
  }
  
  private static Element getForeignMarkup(final String key, final Namespace namespace, final List<Element> foreignMarkup) {
    for (final Element element : foreignMarkup) {
      if (element.getName().equals(key) && element.getNamespace().equals(namespace)) {
        return element;
      }
    }
    return null;
  }
}