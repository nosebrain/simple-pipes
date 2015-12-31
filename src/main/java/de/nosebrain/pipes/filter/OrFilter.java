package de.nosebrain.pipes.filter;

import java.util.LinkedList;
import java.util.List;

import com.rometools.rome.feed.synd.SyndEntry;

public class OrFilter implements BooleanFilter {
  
  private final List<FeedEntryFilter> filters;
  
  public OrFilter() {
    this(new LinkedList<FeedEntryFilter>());
  }
  
  public OrFilter(final List<FeedEntryFilter> filters) {
    this.filters = filters;
  }

  @Override
  public boolean filter(final SyndEntry entry) {
    for (final FeedEntryFilter feedEntryFilter : this.filters) {
      final boolean singleFilterResult = feedEntryFilter.filter(entry);
      if (singleFilterResult) {
        return true;
      }
    }
    
    return false;
  }
  
  @Override
  public void appendFilter(final FeedEntryFilter filter) {
    this.filters.add(filter);
  }
}
