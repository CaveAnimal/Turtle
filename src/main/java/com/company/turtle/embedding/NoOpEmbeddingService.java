package com.company.turtle.embedding;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class NoOpEmbeddingService implements EmbeddingService {

    private static final int DIM = 384;

    @Override
    public float[] generateEmbedding(String text) {
        return new float[DIM];
    }

    @Override
    public List<float[]> batchGenerateEmbeddings(List<String> texts) {
        List<float[]> out = new ArrayList<>(texts.size());
        for (int i = 0; i < texts.size(); i++) {
            out.add(new float[DIM]);
        }
        return out;
    }

    @Override
    public int getEmbeddingDimension() {
        return DIM;
    }
}

