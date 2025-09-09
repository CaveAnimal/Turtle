package com.company.turtle.vector;

import java.nio.file.Path;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Runs a one-shot filesystem->jsonl migration when property turtle.vector.migrate-to-jsonl=true is set.
 */
@Component
@ConditionalOnProperty(name = "turtle.vector.migrate-to-jsonl", havingValue = "true")
public class MigrationRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Path src = Path.of("data","vectors");
        Path out = Path.of("data","vectors-export.jsonl");
        FilesystemToJsonlExporter.export(src, out);
        System.out.println("Exported vectors to " + out.toAbsolutePath());
    }
}
