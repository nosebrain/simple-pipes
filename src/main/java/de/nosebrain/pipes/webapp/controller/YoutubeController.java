package de.nosebrain.pipes.webapp.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.rometools.modules.itunes.FeedInformation;
import com.rometools.modules.itunes.FeedInformationImpl;
import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.types.MediaGroup;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import de.nosebrain.util.IOUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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
  private static final Namespace MEDIA_NAMESPACE = Namespace.getNamespace("media", "http://search.yahoo.com/mrss/");

  @Value("${youtube.apikey}")
  private String youtubeApiKey;

  @RequestMapping("/users/{username}")
  public void userFeed(@PathVariable("username") final String username, @RequestParam(value = "title", required = false) final String title, @RequestParam("serviceUrl") final String serviceUrl, final HttpServletResponse response) throws IOException, IllegalArgumentException, FeedException {
    final String feedUrl = YOUTUBE_FEED_ADDRESS + "?user=" + username;
    final URL feedImage = getFeedImageUrl(this.youtubeApiKey, username, null);
    buildFeed(title, serviceUrl, response, feedUrl, feedImage);
  }
  
  @RequestMapping("/channels/{channelId}")
  public void channelFeed(@PathVariable("channelId") final String channelId, @RequestParam(value = "title", required = false) final String title, @RequestParam("serviceUrl") final String serviceUrl, final HttpServletResponse response) throws IOException, IllegalArgumentException, FeedException {
    final String feedUrl = YOUTUBE_FEED_ADDRESS + "?channel_id=" + channelId;
    final URL feedImage = getFeedImageUrl(this.youtubeApiKey, null, channelId);
    buildFeed(title, serviceUrl, response, feedUrl, feedImage);
  }

  private static URL getFeedImageUrl(String youtubeApiKey, String user, String channelId) {
    String urlToCall = "https://www.googleapis.com/youtube/v3/channels?part=snippet&key=" + youtubeApiKey;
    if (user != null) {
      urlToCall += "&forUsername=" + user;
    } else {
      urlToCall += "&id=" + channelId;
    }

    try {
      final String loadedJson = IOUtils.readUrl(new URL(urlToCall));
      final JSONObject rootObject = new JSONObject(loadedJson);
      return new URL(rootObject.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high").getString("url"));
    } catch (final Exception e) {
      return null;
    }
  }

  private static void buildFeed(final String title, final String serviceUrl, final HttpServletResponse response, final String feedUrl, final URL feedImage) throws MalformedURLException, FeedException, IOException {
    final URL url = new URL(feedUrl);
    final SyndFeedInput input = new SyndFeedInput();
    final SyndFeed feed = input.build(new XmlReader(url));

    final SyndFeed newFeed = new SyndFeedImpl();
    newFeed.setFeedType("rss_2.0");
    if (title != null) {
      newFeed.setTitle(title);
    } else {
      newFeed.setTitle(feed.getTitle());
    }

    // copy meta infos
    newFeed.setLanguage("en-US");
    newFeed.setDescription("Youtube Feed");
    newFeed.setLink(feed.getLink());

    // copy each entry into the new feed and set the enclosure url
    final List<SyndEntry> entries = new ArrayList();
    for (final SyndEntry entry : feed.getEntries()) {
      final SyndEntry newEntry = new SyndEntryImpl();
      newEntry.setTitle(entry.getTitle());
      newEntry.setLink(entry.getLink());
      newEntry.setPublishedDate(entry.getPublishedDate());

      final List<Element> foreignMarkup = entry.getForeignMarkup();
      final Element youtubeIdElement = getForeignMarkup("videoId", YOUTUBE_NAMESPACE, foreignMarkup);
      final Module mediaModule = entry.getModule(MEDIA_NAMESPACE.getURI());
      if (mediaModule instanceof MediaEntryModule) {
        final MediaEntryModule mediaEntryModule = (MediaEntryModule) mediaModule;
        final MediaGroup[] mediaGroups = mediaEntryModule.getMediaGroups();
        for (final MediaGroup mediaGroup : mediaGroups) {
          final String description = mediaGroup.getMetadata().getDescription();
          if (description != null) {
            final SyndContent descriptionElement = new SyndContentImpl();
            descriptionElement.setType("text/plain");;
            descriptionElement.setValue(description);
            newEntry.setDescription(descriptionElement);
          }
        }
      }

      final String youtubeId = youtubeIdElement.getValue();
      final SyndEnclosureImpl linkToVideo = new SyndEnclosureImpl();
      linkToVideo.setUrl(serviceUrl + youtubeId);
      linkToVideo.setType("video/mp4");
      newEntry.getEnclosures().add(linkToVideo);

      entries.add(newEntry);
    }

    newFeed.setEntries(entries);

    // set image
    if (feedImage != null) {
      final FeedInformation feedInfo = new FeedInformationImpl();
      newFeed.getModules().add(feedInfo);
      feedInfo.setImage(feedImage);
    }

    final SyndFeedOutput output = new SyndFeedOutput();
    response.setCharacterEncoding("UTF-8");
    output.output(newFeed, response.getWriter());
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
