package com.company.turtle.indexing;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CodeChunkerTest {

    @Autowired
    CodeChunker chunker;

    @Test
    void chunkSampleFile() {
        Path p = Paths.get("src/sample-code/Sample.java");
        List<CodeChunk> chunks = chunker.chunkFile(p);
        assertFalse(chunks.isEmpty());
    }
}
