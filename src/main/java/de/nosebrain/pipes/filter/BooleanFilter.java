package de.nosebrain.pipes.filter;

public interface BooleanFilter extends FeedEntryFilter {
  
  public void appendFilter(final FeedEntryFilter filter);
}
