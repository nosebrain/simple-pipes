package de.nosebrain.pipes.filter;

import java.util.List;
import java.util.Set;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;

public class CategoryFilter implements FeedEntryFilter {
  
  private final Set<String> categories;
  
  public CategoryFilter(final Set<String> categories) {
    this.categories = categories;
  }
  
  @Override
  public boolean filter(final SyndEntry entry) {
    final List<SyndCategory> entryCategories = entry.getCategories();
    for (final SyndCategory syndCategory : entryCategories) {
      if (this.categories.contains(syndCategory.getName())) {
        return true;
      }
    }
    return false;
  }
}
