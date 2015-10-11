package de.nosebrain.pipes.filter;

import com.rometools.rome.feed.synd.SyndEntry;

public class AuthorFilter implements FeedEntryFilter {
  
  private final String author;
  
  public AuthorFilter(final String author) {
    this.author = author;
  }

  @Override
  public boolean filter(final SyndEntry entry) {
    return entry.getAuthor().contains(this.author);
  }
}
