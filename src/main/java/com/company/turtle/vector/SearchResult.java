package com.company.turtle.vector;

import java.util.Map;

public class SearchResult {
    public final String id;
    public final double score;
    public final Map<String,Object> metadata;

    public SearchResult(String id, double score, Map<String,Object> metadata) {
        this.id = id; this.score = score; this.metadata = metadata;
    }
}
