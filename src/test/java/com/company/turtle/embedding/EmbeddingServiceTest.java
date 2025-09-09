package com.company.turtle.embedding;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmbeddingServiceTest {

    @Autowired
    EmbeddingService embeddingService;

    @Test
    void generatesEmbedding() {
        float[] v = embeddingService.generateEmbedding("hello world");
        assertNotNull(v);
        assertEquals(384, v.length);
    }

    @Test
    void batchEmbeddings() {
        var list = embeddingService.batchGenerateEmbeddings(Arrays.asList("a","b","c"));
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(384, list.get(0).length);
    }
}
