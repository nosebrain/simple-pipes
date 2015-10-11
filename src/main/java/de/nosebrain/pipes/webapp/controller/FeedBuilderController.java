package de.nosebrain.pipes.webapp.controller;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

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
        @RequestParam("feed.title") final String feedTitle,
        @RequestParam("selectors.content") final String contentSelector,
        @RequestParam("selectors.items.title") final String itemTitleSelector,
        @RequestParam("selectors.items.link") final String itemLinkSelector,
        @RequestParam("selectors.items.date") final String itemDateSelector,
        @RequestParam(value = "selectors.items.description", required = false) final String itemDescriptionSelector,
        @RequestParam("formats.items.date") final String dateFormatString,
        final HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException, ParseException {
    final Document doc = Jsoup.parse(source, 5000);
    final Elements items = doc.select(contentSelector);
    final SyndFeed feed = new SyndFeedImpl();
    feed.setFeedType("atom_0.3");
    feed.setTitle(feedTitle);
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
        final SyndContentImpl description = new SyndContentImpl();
        description.setValue(itemDescription);
        entry.setDescription(description);
      }
      feed.getEntries().add(entry);
    }
    
    final SyndFeedOutput output = new SyndFeedOutput();
    response.setCharacterEncoding("UTF-8");
    output.output(feed, response.getWriter());
  }
}
