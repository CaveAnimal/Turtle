package com.company.turtle.embedding;

public class EmbeddingDemo {

    public static void main(String[] args) throws Exception {
        String modelPath = "models/all-MiniLM-L6-v2.onnx";
        WordPieceTokenizer tok = new WordPieceTokenizer("models/tokenizer/vocab.txt", 64);
        String text = (args.length>0)? String.join(" ", args) : "Hello world";
    TokenizationResult tr = tok.encode(text);

        OnnxEmbeddingService svc = new OnnxEmbeddingService(384, modelPath);
        if (!svc.isOrtReady()) {
            System.out.println("ONNX runtime not ready; exiting demo");
            return;
        }

    // Use the token id inference helper (converts ids to the expected ONNX inputs)
    float[] emb = svc.inferFromTokenIdsWithMask(tr.inputIds, tr.attentionMask);
        System.out.println("Embedding length=" + emb.length);
    }
}
