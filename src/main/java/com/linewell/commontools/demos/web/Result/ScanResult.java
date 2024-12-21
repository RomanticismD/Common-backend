package com.linewell.commontools.demos.web.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: ScanResult
 * Package: com.linewell.commontools.demos.web.Result
 * Description:
 *
 * @Author: DWJ
 * @Create: 2024/12/19 - 15:33
 * @Version : v1.0
 */
public  class ScanResult {
    private String message;
    private List<String> chineseNames;
    private Map<String, String> filesWithChinese;
    private List<String> errors;

    public ScanResult() {
        this.chineseNames = new ArrayList<>();
        this.filesWithChinese = new HashMap<>();
        this.errors = new ArrayList<>();
    }

    public void addChineseName(String name) {
        this.chineseNames.add(name);
    }

    public void addFileWithChinese(String filePath, String chineseContent) {
        this.filesWithChinese.put(filePath, chineseContent);
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getChineseNames() {
        return chineseNames;
    }

    public Map<String, String> getFilesWithChinese() {
        return filesWithChinese;
    }

    public List<String> getErrors() {
        return errors;
    }
}