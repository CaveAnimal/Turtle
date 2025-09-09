package com.company.turtle.embedding;

import java.util.List;

public interface EmbeddingService {
    float[] generateEmbedding(String text);
    List<float[]> batchGenerateEmbeddings(List<String> texts);
    int getEmbeddingDimension();
}
