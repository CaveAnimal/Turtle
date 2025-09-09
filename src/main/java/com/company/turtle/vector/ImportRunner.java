package com.company.turtle.vector;

import java.nio.file.Path;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "turtle.vector.import-jsonl", havingValue = "true")
public class ImportRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Path jsonl = Path.of("data","vectors-export.jsonl");
        Path out = Path.of("data","vectors");
        FilesystemJsonlImporter.importTo(jsonl, out);
        System.out.println("Imported vectors from " + jsonl.toAbsolutePath());
    }
}
