package com.company.turtle.embedding;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal WordPiece tokenizer implementation that expects a vocab.txt file (one token per line)
 * placed under models/tokenizer/vocab.txt. This implementation is intentionally small and
 * does not cover every edge case of the HuggingFace tokenizer; it's sufficient for local
 * experimentation when the proper vocab is provided.
 */
public class WordPieceTokenizer {

    private final Map<String,Integer> vocab = new HashMap<>();
    private final int maxLen;
    private final String unkToken = "[UNK]";
    private final String clsToken = "[CLS]";
    private final String sepToken = "[SEP]";
    private final String padToken = "[PAD]";

    public WordPieceTokenizer(String vocabTxtPath, int maxLen) throws IOException {
        this.maxLen = maxLen;
        Path p = Path.of(vocabTxtPath);
        try (BufferedReader r = Files.newBufferedReader(p)) {
            String line; int idx=0;
            while ((line = r.readLine())!=null) {
                String tok = line.strip();
                if (tok.isEmpty()) continue;
                vocab.put(tok, idx++);
            }
        }
    }

    public List<String> tokenize(String text) {
        List<String> out = new ArrayList<>();
        // Simple whitespace + punctuation split
        String[] words = text.trim().toLowerCase().split("\\s+");
        for (String w : words) {
            if (vocab.containsKey(w)) { out.add(w); continue; }
            // WordPiece greedy longest-match
            int len = w.length();
            boolean matched = false;
            for (int start=0; start<len; ) {
                int end = len;
                String cur = null;
                while (end>start) {
                    String substr = (start==0)? w.substring(start,end) : "##" + w.substring(start,end);
                    if (vocab.containsKey(substr)) { cur = substr; break; }
                    end--;
                }
                if (cur==null) { out.add(unkToken); matched=true; break; }
                out.add(cur);
                start = (start==0)? end : end;
                matched = true;
            }
            if (!matched) out.add(unkToken);
        }
        return out;
    }

    public int[] convertTokensToIds(List<String> tokens) {
        int[] ids = new int[tokens.size()];
        for (int i=0;i<tokens.size();i++) ids[i] = vocab.getOrDefault(tokens.get(i), vocab.getOrDefault(unkToken, 100));
        return ids;
    }

    public TokenizationResult encode(String text) {
        List<String> toks = new ArrayList<>();
        if (vocab.containsKey(clsToken)) toks.add(clsToken);
        toks.addAll(tokenize(text));
        if (vocab.containsKey(sepToken)) toks.add(sepToken);
        if (toks.size() > maxLen) toks = toks.subList(0, maxLen);
        int[] ids = convertTokensToIds(toks);
        int[][] out = new int[1][maxLen];
        int[][] mask = new int[1][maxLen];
        int[][] tokenType = new int[1][maxLen];
        for (int i=0;i<maxLen;i++) {
            if (i<ids.length) { out[0][i]=ids[i]; mask[0][i]=1; tokenType[0][i]=0; }
            else { out[0][i]=vocab.getOrDefault(padToken, 0); mask[0][i]=0; tokenType[0][i]=0; }
        }
        return new TokenizationResult(out, mask, tokenType);
    }

    public int[][] attentionMaskFromIds(int[][] ids) {
        int[][] mask = new int[ids.length][ids[0].length];
        for (int i=0;i<ids.length;i++) for (int j=0;j<ids[0].length;j++) mask[i][j] = (ids[i][j]==vocab.getOrDefault(padToken,0))?0:1;
        return mask;
    }
}
