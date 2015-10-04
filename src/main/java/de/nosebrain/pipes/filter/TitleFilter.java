package de.nosebrain.pipes.filter;

import com.rometools.rome.feed.synd.SyndEntry;

public class TitleFilter implements FeedEntryFilter {
  
  private final String titleString;
  
  public TitleFilter(final String titleString) {
    super();
    this.titleString = titleString;
  }
  
  @Override
  public boolean filter(final SyndEntry entry) {
    if (entry.getTitle().contains(this.titleString)) {
      return true;
    }
    return false;
  }
}
