package com.company.turtle.embedding;

public class TokenizationResult {
    public final int[][] inputIds;
    public final int[][] attentionMask;
    public final int[][] tokenTypeIds;

    public TokenizationResult(int[][] inputIds, int[][] attentionMask, int[][] tokenTypeIds) {
        this.inputIds = inputIds;
        this.attentionMask = attentionMask;
        this.tokenTypeIds = tokenTypeIds;
    }
}
