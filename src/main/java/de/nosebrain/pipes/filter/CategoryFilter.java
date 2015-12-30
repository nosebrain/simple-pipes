package de.nosebrain.pipes.filter;

import java.util.List;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;

public class CategoryFilter implements FeedEntryFilter {
  
  private final String category;
  
  public CategoryFilter(final String category) {
    this.category = category;
  }
  
  @Override
  public boolean filter(final SyndEntry entry) {
    final List<SyndCategory> entryCategories = entry.getCategories();
    for (final SyndCategory syndCategory : entryCategories) {
      if (this.category.equals(syndCategory.getName())) {
        return true;
      }
    }
    return false;
  }
}
