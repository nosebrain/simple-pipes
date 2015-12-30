package de.nosebrain.pipes.filter;

import java.util.LinkedList;
import java.util.List;

import com.rometools.rome.feed.synd.SyndEntry;

public class AndFilter implements BooleanFilter {
  
  private final List<FeedEntryFilter> filters;
  
  public AndFilter() {
    this(new LinkedList<FeedEntryFilter>());
  }
  
  public AndFilter(final List<FeedEntryFilter> filters) {
    this.filters = filters;
  }

  @Override
  public boolean filter(final SyndEntry entry) {
    for (final FeedEntryFilter feedEntryFilter : this.filters) {
      final boolean singleFilterResult = feedEntryFilter.filter(entry);
      if (!singleFilterResult) {
        return false;
      }
    }
    
    return true;
  }
  
  @Override
  public void appendFilter(final FeedEntryFilter filter) {
    this.filters.add(filter);
  }
}
