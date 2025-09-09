package com.company.turtle.embedding;

import java.nio.file.Path;

public class EmbeddingModelInspector {

    public static void inspect(Path modelPath) {
        try {
            Class<?> envClass = Class.forName("com.microsoft.onnxruntime.OrtEnvironment");
            java.lang.reflect.Method getEnv = envClass.getMethod("getEnvironment");
            Object env = getEnv.invoke(null);
            java.lang.reflect.Method create = env.getClass().getMethod("createSession", String.class);
            Object session = create.invoke(env, modelPath.toString());
            java.lang.reflect.Method getInputNames = session.getClass().getMethod("getInputNames");
            Object names = getInputNames.invoke(session);
            System.out.println("Model inputs: " + names);
            java.lang.reflect.Method getOutputNames = session.getClass().getMethod("getOutputNames");
            Object onames = getOutputNames.invoke(session);
            System.out.println("Model outputs: " + onames);
            java.lang.reflect.Method close = session.getClass().getMethod("close"); close.invoke(session);
        } catch (Throwable t) {
            System.out.println("Could not inspect model via ONNX runtime: " + t.toString());
        }
    }
}
