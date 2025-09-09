package com.company.turtle.vector;

import java.nio.file.Path;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "turtle.vector.import-to-lucene", havingValue = "true")
public class LuceneImportRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        Path jsonl = Path.of("data","vectors-export.jsonl");
        Path idx = Path.of("data","lucene-index");
        try {
            FilesystemJsonlToLuceneImporter.importToLucene(jsonl, idx);
        } catch (UnsupportedOperationException u) {
            System.out.println("Lucene import requested but not implemented in this build: " + u.getMessage());
        }
    }
}
