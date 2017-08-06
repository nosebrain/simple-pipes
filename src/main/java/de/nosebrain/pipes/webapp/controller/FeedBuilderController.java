package de.nosebrain.pipes.webapp.controller;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import de.nosebrain.util.IOUtils;
import de.nosebrain.util.URLUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

@Controller
@RequestMapping("/buildFeed")
public class FeedBuilderController {

  @RequestMapping("/{url}")
  public void filterFeedByCategory(@PathVariable("url") final URL source,
        @RequestParam(value = "feed.title") final String feedTitle,
        @RequestParam(value = "selectors.content") final String contentSelector,
        @RequestParam(value = "selectors.items.title") final String itemTitleSelector,
        @RequestParam(value = "selectors.items.link") final String itemLinkSelector,
        @RequestParam(value = "selectors.items.date") final String itemDateSelector,
        @RequestParam(value = "selectors.items.description", required = false) final String itemDescriptionSelector,
        @RequestParam(value = "formats.items.date") final String dateFormatString,
        final HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException, ParseException {
    final Document doc = Jsoup.parse(source, 5000);
    final Elements items = doc.select(contentSelector);
    final SyndFeed feed = buildFeed(source, feedTitle);
    final SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    for (final Element element : items) {
      final SyndEntry entry = new SyndEntryImpl();
      
      final Elements titleSelect = element.select(itemTitleSelector);
      final String itemTitle = titleSelect.text();
      entry.setTitle(itemTitle);
      
      final Elements dateSelect = element.select(itemDateSelector);
      final String itemDate = dateSelect.text();
      final Date publishedDate = dateFormat.parse(itemDate);
      entry.setPublishedDate(publishedDate);
      
      final Elements linkSelect = element.select(itemLinkSelector);
      final String itemUrl = linkSelect.first().absUrl("href");
      entry.setLink(itemUrl);
      
      if (itemDescriptionSelector != null) {
        final Elements descriptionSelect = element.select(itemDescriptionSelector);
        final String itemDescription = descriptionSelect.text();
        entry.setDescription(buildTextDescription(itemDescription));
      }
      feed.getEntries().add(entry);
    }
    
    final SyndFeedOutput output = new SyndFeedOutput();
    response.setCharacterEncoding("UTF-8");
    output.output(feed, response.getWriter());
  }

  private static SyndContentImpl buildTextDescription(String itemDescription) {
    final SyndContentImpl description = new SyndContentImpl();
    description.setValue(itemDescription);
    return description;
  }

  private static SyndFeed buildFeed(@PathVariable("url") URL source, @RequestParam(value = "feed.title") String feedTitle) {
    final SyndFeed feed = new SyndFeedImpl();
    feed.setFeedType("atom_0.3");
    feed.setTitle(feedTitle);
    feed.setUri(source.toString());
    return feed;
  }

  @RequestMapping("/json/{url}")
  public void buildFeedByJson(@PathVariable("url") final URL source,
                              @RequestParam(value = "feed.title") final String feedTitle,
                              @RequestParam(value = "selectors.content") final String contentSelector,
                              @RequestParam(value = "selectors.items.title") final String itemTitleSelector,
                              @RequestParam(value = "selectors.items.link") final String itemLinkSelector,
                              @RequestParam(value = "selectors.items.date") final String itemDateSelector,
                              @RequestParam(value = "selectors.items.description", required = false) final String itemDescriptionSelector,
                              @RequestParam(value = "formats.items.date") final String dateFormatString, final HttpServletResponse response) throws IOException, FeedException, ParseException {
    final SyndFeed feed = buildFeed(source, feedTitle);

    final SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);

    final String loadedJson = IOUtils.readUrl(source);
    final JSONObject rootObject = new JSONObject(loadedJson);

    final JSONObject content = runSelector(rootObject, contentSelector, JSONObject.class);

    for (final Iterator<String> it = content.keys(); it.hasNext();) {
      final String contentKey = it.next();

      final JSONObject item = content.getJSONObject(contentKey);
      final SyndEntry entry = new SyndEntryImpl();

      final String title = runSelector(item, itemTitleSelector, String.class);
      entry.setTitle(title);

      final String link = URLUtils.normLink(source, runSelector(item, itemLinkSelector, String.class));
      entry.setLink(link);

      final String itemDate = runSelector(item, itemDateSelector, String.class);
      final Date publishedDate = dateFormat.parse(itemDate);
      entry.setPublishedDate(publishedDate);

      if (itemDescriptionSelector != null) {
        final String description = runSelector(item, itemDescriptionSelector, String.class);
        entry.setDescription(buildTextDescription(description));
      }

      feed.getEntries().add(entry);
    }

    final SyndFeedOutput output = new SyndFeedOutput();
    response.setCharacterEncoding("UTF-8");
    output.output(feed, response.getWriter());
  }

  private static <T> T runSelector(final JSONObject object, final String selector, final Class<T> targetClass) {
    final List<String> selectors = new LinkedList<>(Arrays.asList(selector.split(" ")));

    // get last
    final String lastSelector = selectors.get(selectors.size() - 1);
    selectors.remove(selectors.size()  - 1);

    // get up to last (exclusive)
    final JSONObject element = runSelector(object, selectors);

    // use
    return (T) element.get(lastSelector);
  }

  private static JSONObject runSelector(final JSONObject object, final List<String> selectors) {
    JSONObject selected = object;
    for (final String selector : selectors) {
      selected = selected.getJSONObject(selector);
    }

    return selected;
  }
}
