package com.company.turtle.indexing;

import java.nio.file.Path;
import java.util.List;

public interface CodeIndexer {
    void indexDirectory(Path sourceDirectory);
    List<CodeChunk> indexFile(Path javaFile);
}
