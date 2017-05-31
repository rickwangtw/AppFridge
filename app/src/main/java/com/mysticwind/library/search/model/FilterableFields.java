package com.mysticwind.library.search.model;

import java.util.List;

public interface FilterableFields {
    List<String> getSearchableStringsOrderedByPriority();
}
