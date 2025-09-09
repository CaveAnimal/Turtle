package com.company.turtle.vector;

import java.util.List;
import java.util.Map;

public interface VectorStore {
    void store(String id, float[] vector, Map<String, Object> metadata);
    List<SearchResult> search(float[] query, int topK);
    void delete(String id);
}

