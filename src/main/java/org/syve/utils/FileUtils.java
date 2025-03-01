package org.syve.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);
    private static final Map<String, String> mimeTypeToExtensionMap = createMimeTypeMap();
    private static final Tika tika = new Tika();

    private static Map<String, String> createMimeTypeMap() {
        Map<String, String> map = new HashMap<>();
        map.put("image/jpeg", ".jpg");
        map.put("image/png", ".png");
        map.put("application/pdf", ".pdf");
        map.put("text/plain", ".txt");
        map.put("application/msword", ".doc");
        map.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
        return map;
    }

    public static String getExtensionFromMimeType(String mimeType) {
        return mimeTypeToExtensionMap.getOrDefault(mimeType, "");
    }

    public static Optional<String> detectMimeType(File file) {
        try {
            return Optional.of(tika.detect(file));
        } catch (IOException e) {
            LOG.error("Error determining MIME type of file [{}]: {}", file.getName(), e.getMessage());
            return Optional.empty();
        }
    }
}
