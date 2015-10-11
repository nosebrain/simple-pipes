package de.nosebrain.pipes.filter;

import com.rometools.rome.feed.synd.SyndEntry;

public class TitleFilter implements FeedEntryFilter {
  
  private final String titleString;
  
  public TitleFilter(final String titleString) {
    this.titleString = titleString;
  }
  
  @Override
  public boolean filter(final SyndEntry entry) {
    return entry.getTitle().contains(this.titleString);
  }
}
