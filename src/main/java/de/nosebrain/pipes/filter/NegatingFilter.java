package de.nosebrain.pipes.filter;

import com.rometools.rome.feed.synd.SyndEntry;

public class NegatingFilter implements FeedEntryFilter {
  
  private final FeedEntryFilter filter;
  
  public NegatingFilter(final FeedEntryFilter filter) {
    this.filter = filter;
  }

  @Override
  public boolean filter(final SyndEntry entry) {
    return !this.filter.filter(entry);
  }
}