package com.company.turtle.embedding;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OnnxEmbeddingService implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(OnnxEmbeddingService.class);

    private final int dim;
    private final String modelPath;
    private final boolean onnxAvailable;
    private final boolean modelExists;
    private final NoOpEmbeddingService fallback = new NoOpEmbeddingService();
    private Object ortEnvironment;
    private Object ortSession;
    private boolean ortReady = false;
    // optional tokenizer support
    private WordPieceTokenizer tokenizer = null;
    private final String tokenizerVocabPath;
    private final int tokenizerMaxLen = 128;

    public OnnxEmbeddingService(@Value("${turtle.embedding.dimension:384}") int dim,
                               @Value("${turtle.embedding.model-path:models/all-MiniLM-L6-v2.onnx}") String modelPath) {
        this.dim = dim; this.modelPath = modelPath;
        this.tokenizerVocabPath = Path.of(modelPath).getParent()==null ? "models/tokenizer/vocab.txt" : Path.of(modelPath).getParent().resolve("tokenizer").resolve("vocab.txt").toString();
        boolean has = false;
        try { Class.forName("com.microsoft.onnxruntime.OrtEnvironment"); has = true; } catch (Throwable t) { /* not available */ }
        this.onnxAvailable = has;
        this.modelExists = Path.of(modelPath).toFile().exists();

        // Try to initialize ONNX runtime via reflection if available and model exists
        if (onnxAvailable && modelExists) {
            try {
                Class<?> envClass = Class.forName("com.microsoft.onnxruntime.OrtEnvironment");
                java.lang.reflect.Method getEnv = envClass.getMethod("getEnvironment");
                ortEnvironment = getEnv.invoke(null);

                // Attempt to create a session (SessionOptions may be needed)
                try {
                    Class<?> sessionOptionsClass = Class.forName("com.microsoft.onnxruntime.OrtSession$SessionOptions");
                    Object sessionOptions = sessionOptionsClass.getConstructor().newInstance();
                    java.lang.reflect.Method createSession = ortEnvironment.getClass().getMethod("createSession", String.class, sessionOptionsClass);
                    ortSession = createSession.invoke(ortEnvironment, modelPath, sessionOptions);
                } catch (ClassNotFoundException cnfe) {
                    // Fallback: try createSession(String)
                    try {
                        java.lang.reflect.Method createSession = ortEnvironment.getClass().getMethod("createSession", String.class);
                        ortSession = createSession.invoke(ortEnvironment, modelPath);
                    } catch (Throwable t) {
                        log.info("ONNX runtime present but could not create session via reflection: {}", t.toString());
                    }
                }

                ortReady = (ortEnvironment != null && ortSession != null);
                log.info("ONNX reflection init: available={}, modelExists={}, ready={}", onnxAvailable, modelExists, ortReady);
            } catch (Throwable t) {
                log.info("ONNX reflection init failed: {}", t.toString());
                ortReady = false;
            }
        }
        // attempt to instantiate tokenizer if vocab available
        try {
            Path tv = Path.of(this.tokenizerVocabPath);
            if (!tv.toFile().exists()) tv = Path.of("models","tokenizer","vocab.txt");
            if (tv.toFile().exists()) tokenizer = new WordPieceTokenizer(tv.toString(), tokenizerMaxLen);
        } catch (Throwable t) {
            log.debug("Tokenizer not initialized: {}", t.toString());
        }
    }

    @Override
    public float[] generateEmbedding(String text) {
        if (ortReady) {
            try {
                if (tokenizer != null) {
                    TokenizationResult tr = tokenizer.encode(text);
                    int[][] ids = tr.inputIds;
                    int[][] mask = tr.attentionMask;
                    try {
                        float[] out = inferFromTokenIdsWithMask(ids, mask);
                        if (out != null && out.length == dim) return out;
                    } catch (Throwable t) {
                        log.info("ONNX inference failed during generateEmbedding, falling back: {}", t.toString());
                    }
                } else {
                    log.debug("ONNX ready but no tokenizer available; falling back to NoOp");
                }
            } catch (Throwable t) {
                log.info("ONNX generateEmbedding error, falling back: {}", t.toString());
            }
        }
        return fallback.generateEmbedding(text);
    }

    /**
     * Run inference on a pre-constructed float input tensor (shape: [batch, ...]) and
     * return the first output flattened to a float[] if possible.
     * This method uses reflection against the ONNX Runtime Java API and will throw if
     * the runtime is not available or the model input/output shapes are incompatible.
     */
    public float[] inferFromFloatTensor(float[][] input) throws Exception {
        if (!ortReady) throw new IllegalStateException("ONNX runtime/session not initialized");

        // find input name
        String inputName = null;
        try {
            java.lang.reflect.Method getInputNames = ortSession.getClass().getMethod("getInputNames");
            Object namesObj = getInputNames.invoke(ortSession);
            if (namesObj instanceof java.util.Set) {
                java.util.Set<?> s = (java.util.Set<?>) namesObj;
                if (!s.isEmpty()) inputName = String.valueOf(s.iterator().next());
            }
        } catch (Throwable t) {
            // ignore
        }
        if (inputName == null) inputName = "input";

        Object tensor = null;
        Object result = null;
        try {
            Class<?> onnxTensorClass = Class.forName("com.microsoft.onnxruntime.OnnxTensor");
            // prefer createTensor(OrtEnvironment, long[][]) for int64 inputs if available
            java.lang.reflect.Method createTensor = null;
            try {
                createTensor = onnxTensorClass.getMethod("createTensor", Class.forName("com.microsoft.onnxruntime.OrtEnvironment"), long[][].class);
                // convert float[][] to long[][] by casting
                long[][] longInput = new long[input.length][input[0].length];
                for (int i=0;i<input.length;i++) for (int j=0;j<input[0].length;j++) longInput[i][j] = (long) input[i][j];
                tensor = createTensor.invoke(null, ortEnvironment, (Object) longInput);
            } catch (NoSuchMethodException ns) {
                // fallback to generic createTensor(OrtEnvironment, Object)
                createTensor = onnxTensorClass.getMethod("createTensor", Class.forName("com.microsoft.onnxruntime.OrtEnvironment"), Object.class);
                tensor = createTensor.invoke(null, ortEnvironment, input);
            }

            // Build inputs map. If model uses named inputs like input_ids and attention_mask, try to map them.
            java.util.Map<String, Object> inputs = new java.util.HashMap<>();
            java.lang.reflect.Method getInputNames = ortSession.getClass().getMethod("getInputNames");
            Object namesObj = getInputNames.invoke(ortSession);
            java.util.List<String> names = new java.util.ArrayList<>();
            if (namesObj instanceof java.util.Set) {
                for (Object o : (java.util.Set<?>)namesObj) names.add(String.valueOf(o));
            }
            if (names.contains("input_ids")) {
                inputs.put("input_ids", tensor);
                // try to also create attention_mask from tensor (1s and 0s where padding id found)
                // simplistic: assume ids>0 are not padding
                // create an attention mask tensor as long[][] mirror of ids
                long[][] mask = new long[input.length][input[0].length];
                for (int i=0;i<input.length;i++) for (int j=0;j<input[0].length;j++) mask[i][j] = (input[i][j]!=0)?1L:0L;
                // create tensor for mask
                Object maskTensor = null;
                try {
                    java.lang.reflect.Method createMask = onnxTensorClass.getMethod("createTensor", Class.forName("com.microsoft.onnxruntime.OrtEnvironment"), long[][].class);
                    maskTensor = createMask.invoke(null, ortEnvironment, (Object) mask);
                    inputs.put("attention_mask", maskTensor);
                } catch (Throwable t) {
                    // ignore mask creation failures
                }
            } else {
                inputs.put(inputName, tensor);
            }
            java.lang.reflect.Method runMethod = ortSession.getClass().getMethod("run", java.util.Map.class);
            result = runMethod.invoke(ortSession, inputs);

            // result is OrtSession.Result; attempt to get first output: result.get(0).getValue()
            Object out0 = null;
            try {
                java.lang.reflect.Method get0 = result.getClass().getMethod("get", int.class);
                out0 = get0.invoke(result, 0);
            } catch (NoSuchMethodException ns) {
                // try iterator
                try {
                    java.lang.reflect.Method iterator = result.getClass().getMethod("iterator");
                    Object it = iterator.invoke(result);
                    java.lang.reflect.Method hasNext = it.getClass().getMethod("hasNext");
                    java.lang.reflect.Method next = it.getClass().getMethod("next");
                    if ((Boolean) hasNext.invoke(it)) out0 = next.invoke(it);
                } catch (Throwable t) {
                    // give up
                }
            }

            if (out0 == null) throw new IllegalStateException("Could not read ONNX output");

            // call getValue() on output
            java.lang.reflect.Method getValue = out0.getClass().getMethod("getValue");
            Object val = getValue.invoke(out0);

            // Attempt to convert val to float[]
            if (val instanceof float[]) return (float[]) val;
            if (val instanceof float[][]) {
                float[][] a = (float[][]) val;
                if (a.length > 0) return a[0];
            }
            if (val instanceof double[]) {
                double[] d = (double[]) val; float[] f = new float[d.length]; for (int i=0;i<d.length;i++) f[i]=(float)d[i]; return f;
            }
            if (val instanceof double[][]) {
                double[][] d = (double[][]) val; if (d.length>0){ double[] r=d[0]; float[] f=new float[r.length]; for(int i=0;i<r.length;i++) f[i]=(float)r[i]; return f; }
            }

            // Fallback: try toString parse (not ideal)
            throw new IllegalStateException("Unsupported ONNX output type: " + val.getClass());
        } finally {
            // close resources if possible
            try { if (result != null) { java.lang.reflect.Method close = result.getClass().getMethod("close"); close.invoke(result); } } catch (Throwable t) {}
            try { if (tensor != null) { java.lang.reflect.Method close = tensor.getClass().getMethod("close"); close.invoke(tensor); } } catch (Throwable t) {}
        }
    }

    public boolean isOrtReady() { return ortReady; }

    /**
     * Convenience wrapper that accepts token ids (int[][]) and forwards to inferFromFloatTensor
     */
    public float[] inferFromTokenIds(int[][] tokenIds) throws Exception {
        float[][] input = new float[tokenIds.length][tokenIds[0].length];
        for (int i=0;i<tokenIds.length;i++) for (int j=0;j<tokenIds[0].length;j++) input[i][j] = tokenIds[i][j];
        return inferFromFloatTensor(input);
    }

    /**
     * Accepts token ids and attention masks (int[][]) and maps them to model inputs
     */
    public float[] inferFromTokenIdsWithMask(int[][] tokenIds, int[][] attentionMask) throws Exception {
        if (!ortReady) throw new IllegalStateException("ONNX runtime/session not initialized");
        // convert int[][] to long[][] for int64 inputs
        long[][] ids = new long[tokenIds.length][tokenIds[0].length];
        long[][] mask = new long[attentionMask.length][attentionMask[0].length];
        for (int i=0;i<tokenIds.length;i++) for (int j=0;j<tokenIds[0].length;j++) ids[i][j] = tokenIds[i][j];
        for (int i=0;i<attentionMask.length;i++) for (int j=0;j<attentionMask[0].length;j++) mask[i][j] = attentionMask[i][j];

        // create tensors reflectively and run similarly to inferFromFloatTensor but mapping names
        Class<?> onnxTensorClass = Class.forName("com.microsoft.onnxruntime.OnnxTensor");
        java.lang.reflect.Method createLongTensor = null;
        Object idsTensor = null;
        Object maskTensor = null;
        try {
            try {
                createLongTensor = onnxTensorClass.getMethod("createTensor", Class.forName("com.microsoft.onnxruntime.OrtEnvironment"), long[][].class);
                idsTensor = createLongTensor.invoke(null, ortEnvironment, (Object) ids);
                maskTensor = createLongTensor.invoke(null, ortEnvironment, (Object) mask);
            } catch (NoSuchMethodException ns) {
                // fallback: create generic tensor
                java.lang.reflect.Method createTensor = onnxTensorClass.getMethod("createTensor", Class.forName("com.microsoft.onnxruntime.OrtEnvironment"), Object.class);
                idsTensor = createTensor.invoke(null, ortEnvironment, ids);
                maskTensor = createTensor.invoke(null, ortEnvironment, mask);
            }

            java.util.Map<String,Object> inputs = new java.util.HashMap<>();
            // prefer named inputs
            java.lang.reflect.Method getInputNames = ortSession.getClass().getMethod("getInputNames");
            Object namesObj = getInputNames.invoke(ortSession);
            java.util.List<String> names = new java.util.ArrayList<>();
            if (namesObj instanceof java.util.Set) for (Object o: (java.util.Set<?>)namesObj) names.add(String.valueOf(o));
            if (names.contains("input_ids")) inputs.put("input_ids", idsTensor); else inputs.put("input", idsTensor);
            if (names.contains("attention_mask")) inputs.put("attention_mask", maskTensor);

            java.lang.reflect.Method runMethod = ortSession.getClass().getMethod("run", java.util.Map.class);
            Object result = runMethod.invoke(ortSession, inputs);
            Object out0 = null;
            try { java.lang.reflect.Method get0 = result.getClass().getMethod("get", int.class); out0 = get0.invoke(result, 0); } catch (NoSuchMethodException ns) {
                java.lang.reflect.Method iterator = result.getClass().getMethod("iterator"); Object it = iterator.invoke(result);
                java.lang.reflect.Method hasNext = it.getClass().getMethod("hasNext"); java.lang.reflect.Method next = it.getClass().getMethod("next");
                if ((Boolean) hasNext.invoke(it)) out0 = next.invoke(it);
            }
            if (out0==null) throw new IllegalStateException("ONNX output unavailable");
            java.lang.reflect.Method getValue = out0.getClass().getMethod("getValue"); Object val = getValue.invoke(out0);
            if (val instanceof float[]) return (float[]) val;
            if (val instanceof float[][]) { float[][] a=(float[][])val; if (a.length>0) return a[0]; }
            if (val instanceof double[]) { double[] d=(double[])val; float[] f=new float[d.length]; for (int i=0;i<d.length;i++) f[i]=(float)d[i]; return f; }
            throw new IllegalStateException("Unsupported ONNX output type: " + (val==null?"null":val.getClass()));
        } finally {
            try { if (idsTensor!=null) { java.lang.reflect.Method close = idsTensor.getClass().getMethod("close"); close.invoke(idsTensor);} } catch (Throwable t) {}
            try { if (maskTensor!=null) { java.lang.reflect.Method close = maskTensor.getClass().getMethod("close"); close.invoke(maskTensor);} } catch (Throwable t) {}
        }
    }

    @Override
    public List<float[]> batchGenerateEmbeddings(List<String> texts) {
        List<float[]> out = new ArrayList<>();
        for (String t : texts) out.add(generateEmbedding(t));
        return out;
    }

    @Override
    public int getEmbeddingDimension() { return dim; }
}
