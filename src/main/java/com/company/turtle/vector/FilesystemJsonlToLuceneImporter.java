package com.company.turtle.vector;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Skeleton for importing JSONL into a Lucene index. Kept as a no-op implementation so it
 * can be wired later when Lucene artifacts are available.
 */
public class FilesystemJsonlToLuceneImporter {

    public static void importToLucene(Path jsonl, Path luceneIndex) throws IOException {
        // TODO: implement Lucene index creation and vector field mapping when Lucene is available.
        // For now, throw to make it explicit if someone enables the runner without Lucene.
        throw new UnsupportedOperationException("Lucene import not implemented in this environment");
    }
}
