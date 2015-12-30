package de.nosebrain.pipes.webapp.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;

import de.nosebrain.pipes.filter.FeedEntryFilter;

@Controller
@RequestMapping("/feeds")
public class GeneralController {
  
  @RequestMapping("/{url}")
  public void filterFeedByCategory(@PathVariable("url") final URL url, final FeedEntryFilter filter, final HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
    final SyndFeedInput input = new SyndFeedInput();
    final SyndFeed feed = input.build(new XmlReader(url));
    
    final Iterator<SyndEntry> iterator = feed.getEntries().iterator();
    
    while (iterator.hasNext()) {
      final SyndEntry entry = iterator.next();
      
      final boolean remove = !filter.filter(entry);
      if (remove) {
        iterator.remove();
      }
    }
    
    final SyndFeedOutput output = new SyndFeedOutput();
    response.setCharacterEncoding("UTF-8");
    output.output(feed, response.getWriter());
  }
}