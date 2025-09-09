package com.company.turtle.search;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.company.turtle.embedding.EmbeddingService;
import com.company.turtle.vector.SearchResult;
import com.company.turtle.vector.VectorStore;

@Service
public class SearchService {

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    public SearchService(java.util.List<com.company.turtle.embedding.EmbeddingService> embeddingServices, VectorStore vectorStore) {
        // prefer OnnxEmbeddingService when available
        com.company.turtle.embedding.EmbeddingService chosen = null;
        for (com.company.turtle.embedding.EmbeddingService s : embeddingServices) {
            if (s.getClass().getSimpleName().toLowerCase().contains("onnx")) { chosen = s; break; }
            if (chosen == null) chosen = s;
        }
        this.embeddingService = chosen;
        this.vectorStore = vectorStore;
    }

    public SearchResponse search(SearchRequest req) {
        float[] q = embeddingService.generateEmbedding(req.query);
        List<SearchResult> res = vectorStore.search(q, req.topK);
        SearchResponse out = new SearchResponse();
        out.totalCount = res.size();
        out.results = res.stream().map(r -> r).collect(Collectors.toList());
        return out;
    }
}
