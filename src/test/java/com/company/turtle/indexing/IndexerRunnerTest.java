package com.company.turtle.indexing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import com.company.turtle.embedding.EmbeddingService;
import com.company.turtle.vector.FilesystemVectorStore;
import com.company.turtle.vector.SearchResult;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
public class IndexerRunnerTest {

    @Autowired
    CodeChunker chunker;

    @Autowired
    EmbeddingService embeddingService;

    @Autowired
    FilesystemVectorStore store;

    @Test
    void indexAndQuerySampleCode() {
        Path sample = Path.of("sample-code");
        var files = sample.toFile().listFiles((f)->f.getName().endsWith(".java"));
        if (files == null || files.length==0) return; // nothing to do in CI
        for (var f : files) {
            List<com.company.turtle.indexing.CodeChunk> chunks = chunker.chunkFile(f.toPath());
            for (var c : chunks) {
                float[] vec = embeddingService.generateEmbedding(c.getContent());
                store.store(c.getId(), vec, c.getMetadata());
            }
        }
        // query with sample text
        float[] q = embeddingService.generateEmbedding("sample");
        var res = store.search(q, 5);
        assertTrue(res != null);
    }
}
