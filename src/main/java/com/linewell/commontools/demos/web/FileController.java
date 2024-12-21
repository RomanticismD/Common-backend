package com.linewell.commontools.demos.web;

import com.linewell.commontools.demos.web.Result.ScanResult;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");

    @GetMapping("/scan")
    public ResponseEntity<ScanResult> scanFiles(
            @RequestParam(name = "directory",required = true) String directoryPath,
            @RequestParam(name = "excludeDirectories", required = false) List<String> excludeDirectories,
            @RequestParam(name = "excludeExtensions", required = false) List<String> excludeExtensions) {
        System.out.println("Processing .................");
        File folder = new File(directoryPath);

        ScanResult result = new ScanResult();

        if (folder.exists() && folder.isDirectory()) {
            traverseFolder(folder, result, excludeDirectories, excludeExtensions);
        } else {
            result.setMessage("Invalid directory path: " + directoryPath);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        System.out.println("Done!");
        result.setMessage("Processing completed successfully.");
        return ResponseEntity.ok(result);
    }

    // Recursively traverse the folder and its subfolders
    private void traverseFolder(File folder, ScanResult result, List<String> excludeDirectories, List<String> excludeExtensions) {
        File[] files = folder.listFiles();
        if (files != null) {
            if (excludeDirectories != null && excludeDirectories.contains(folder.getAbsolutePath())) {
                return;
            }
            checkFileNameForChinese(folder, result);
            for (File file : files) {
                if (file.isDirectory()) {
                    traverseFolder(file, result, excludeDirectories, excludeExtensions);
                } else {
                    if (shouldExcludeFile(file, excludeExtensions)) {
                        continue;
                    }
                    checkFileNameForChinese(file, result);
                    processTextFile(file, result);
                }
            }
        }
    }

    // Check if a file should be excluded based on its extension
    private boolean shouldExcludeFile(File file, List<String> excludeExtensions) {
        if (excludeExtensions != null && !excludeExtensions.isEmpty()) {
            String fileName = file.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                String fileExtension = fileName.substring(dotIndex + 1);
                return excludeExtensions.contains(fileExtension);
            }
        }
        return false;
    }

    // Check if a file or folder name contains Chinese characters and add it to the result
    private void checkFileNameForChinese(File file, ScanResult result) {
        Matcher matcher = CHINESE_PATTERN.matcher(file.getName());
        if (matcher.find()) {
            result.addChineseName(file.getAbsolutePath());
        }
    }

    // Process text files and find Chinese characters
    private void processTextFile(File file, ScanResult result) {
        Charset detectedCharset = detectFileEncoding(file);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), detectedCharset))) {
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line);
            }

            Matcher matcher = CHINESE_PATTERN.matcher(fileContent.toString());
            StringBuilder chineseChars = new StringBuilder();

            while (matcher.find()) {
                if (chineseChars.length() > 0) {
                    chineseChars.append(", ");
                }
                chineseChars.append(matcher.group());
            }
            if (chineseChars.length() > 0) {
                result.addFileWithChinese(file.getAbsolutePath(), chineseChars.toString());
            }
        } catch (IOException e) {
            result.addError("Error reading file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    // Detect file encoding using UniversalDetector
    private Charset detectFileEncoding(File file) {
        try (InputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            UniversalDetector detector = new UniversalDetector(null);

            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0 && !detector.isDone()) {
                detector.handleData(buffer, 0, bytesRead);
            }
            detector.dataEnd();

            String encoding = detector.getDetectedCharset();
            detector.reset();

            if (encoding != null) {
                return Charset.forName(encoding);
            }
        } catch (IOException e) {
            System.out.println("Error detecting encoding for file: " + file.getAbsolutePath());
        }

        return StandardCharsets.UTF_8; // Default to UTF-8
    }
}
