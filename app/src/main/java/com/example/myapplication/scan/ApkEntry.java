package com.example.myapplication.scan;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.jar.JarEntry;

public class ApkEntry extends JarEntry {
    byte[] data;
    public ApkEntry(String clz) {
        super(clz.replace('.', '/') + ".class");

        byte[] bytes = clz.getBytes(StandardCharsets.UTF_8);
        this.data = new byte[bytes.length + 4];
        System.arraycopy(MAGIC, 0, this.data, 0, 4);
        System.arraycopy(bytes, 0, this.data, 4, bytes.length);
    }

    private static final byte[] MAGIC = "APK/".getBytes(StandardCharsets.US_ASCII);

    /**
     *
     * @param inputStream
     * @return 如果是安卓类返回null，调用read读取类名；否则返回原始输入流
     */
    public static InputStream judge(InputStream inputStream) throws IOException {
        byte[] magic = new byte[4];
        int readed = inputStream.read(magic);
        if (readed == 4) {
            if (Arrays.equals(MAGIC, magic)) {
                return null;
            }
        }
        return new SequenceInputStream(new ByteArrayInputStream(magic, 0, readed), inputStream);
    }

    public static String read(InputStream inputStream) throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).readLine();
    }
}
