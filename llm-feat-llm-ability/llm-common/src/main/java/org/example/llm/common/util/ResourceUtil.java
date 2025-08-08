package org.example.llm.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author : zybi
 * @date : 2025/5/19 17:40
 */
public class ResourceUtil {

    public static String readJsonFromResource(String resourcePath) throws IOException {
        try (InputStream inputStream = ResourceUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
