package de.nosebrain.pipes.filter;

import com.rometools.rome.feed.synd.SyndEntry;

public interface FeedEntryFilter {
  
  public boolean filter(final SyndEntry entry);
}
