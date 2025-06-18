package com.sk.revisit.helper;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class FileHelper {

    public static long getFolderSize(String folderPath) throws IOException {
        Path folder = Paths.get(folderPath);
        AtomicLong size = new AtomicLong(0);
        try (Stream<Path> walk = Files.walk(folder)) {
            walk.parallel()
                    .filter(Files::isRegularFile)
                    .forEach(path -> size.addAndGet(path.toFile().length()));
        }
        return size.get();
    }

    public static void searchRecursive(@NonNull File dir, String extension, List<String> files) {
        File[] fileList = dir.listFiles();
        if (fileList == null) {
            return;
        }
        for (File file : fileList) {
            if (file.isDirectory()) {
                searchRecursive(file, extension, files);
            } else if (file.getName().endsWith(extension)) {
                files.add(file.getAbsolutePath());
            }
        }
    }

    private static void searchHtmlParallel(File dir, List<String> files) throws Exception {
        Path folder = Paths.get(dir.getAbsolutePath());
        try (Stream<Path> walk = Files.walk(folder)) {
            walk.parallel()
                    .filter(Files::isRegularFile)
                    .filter(FileHelper::isHTML)
                    .forEach(path -> files.add(path.toString()));
        }
    }

    static boolean isHTML(Path path) {
        return path.endsWith(".html") || path.endsWith(".htm");
    }
}