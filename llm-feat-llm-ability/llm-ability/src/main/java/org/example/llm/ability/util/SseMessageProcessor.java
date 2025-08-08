package org.example.llm.ability.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SseMessageProcessor {
    // 用于匹配并移除 SSE 前缀的正则表达式模式
    private static final Pattern SSE_PREFIX_PATTERN = Pattern.compile(
            "^(data|event|id|retry|comment):\\s*(.*)$",
            Pattern.MULTILINE
    );

    // 用于匹配并移除注释行的正则表达式模式
    private static final Pattern COMMENT_LINE_PATTERN = Pattern.compile(
            "^:\\s*.*$",
            Pattern.MULTILINE
    );

    /**
     * 去除 SSE 消息中的固定前缀
     * @param sseMessage 原始 SSE 消息
     * @return 处理后的消息内容
     */
    public static String removeSsePrefixes(String sseMessage) {
        if (sseMessage == null) {
            return null;
        }

        // 先移除注释行
        String cleanedMessage = COMMENT_LINE_PATTERN.matcher(sseMessage).replaceAll("");

        // 再处理并移除各字段前缀
        Matcher matcher = SSE_PREFIX_PATTERN.matcher(cleanedMessage);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            // 获取第2个捕获组，即冒号后面的内容
            String value = matcher.group(2);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static void main(String[] args) {
        String sseData = "data: This is a data message\nevent: customEvent\nid: 12345\nretry: 5000\n: This is a comment line\ndata: Another data line";

        String cleanedData = removeSsePrefixes(sseData);
        System.out.println(cleanedData);
    }
}