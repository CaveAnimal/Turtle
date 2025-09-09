package com.company.turtle.embedding;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class WordPieceTokenizerTest {

    @Test
    public void basicTokenize() throws Exception {
        Path tmp = Path.of("target","tokenizer-test");
        Files.createDirectories(tmp.resolve("models/tokenizer"));
        Path vocab = tmp.resolve("models/tokenizer/vocab.txt");
        String content = "[PAD]\n[UNK]\n[CLS]\n[SEP]\nhello\nworld\n##s\n";
        Files.createDirectories(vocab.getParent());
        Files.writeString(vocab, content);

    WordPieceTokenizer t = new WordPieceTokenizer(vocab.toString(), 8);
    List<String> toks = t.tokenize("hello worlds");
        assertNotNull(toks);
        assertTrue(toks.contains("hello"));
        // 'worlds' should be split into 'world' + '##s' (vocab has ##s)
        assertTrue(toks.contains("##s"));
    TokenizationResult tr = t.encode("hello world");
    assertNotNull(tr);
    assertEquals(1, tr.inputIds.length);
    assertEquals(8, tr.inputIds[0].length);
    }
}
