package com.company.turtle.vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Placeholder LuceneVectorStore kept so callers compile; real implementation is blocked by missing
 * Lucene artifacts in this environment. Methods are no-ops or throw to make failures explicit.
 */
@Service
@ConditionalOnProperty(name = "turtle.vector.use-lucene", havingValue = "true")
public class LuceneVectorStore implements VectorStore {

    public LuceneVectorStore(@Value("${turtle.vector.lucene-index:data/lucene-index}") String indexPath,
                             @Value("${turtle.vector.hnsw.m:16}") int m,
                             @Value("${turtle.vector.hnsw.efConstruction:200}") int efConstruction) {
        // Intentionally left blank. Lucene not available in CI environment.
    }

    @Override
    public synchronized void store(String id, float[] vector, Map<String, Object> metadata) {
        throw new UnsupportedOperationException("Lucene not available in this environment");
    }

    @Override
    public List<SearchResult> search(float[] query, int topK) {
        return new ArrayList<>();
    }

    @Override
    public void delete(String id) {
        // no-op
    }
}
