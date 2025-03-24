package com.chatbot.util;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Slf4j
public class AnswerDeserializer {
    private static final Pattern BULLET_PATTERN = Pattern.compile("^[•\\-*]\\s+(.+)$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+\\.\\s+(.+)$");

    public static List<String> deserialize(String rawAnswer) {
        if (rawAnswer == null || rawAnswer.trim().isEmpty()) {
            return new ArrayList<>();
        }

        log.debug("Deserializing answer of length: {}", rawAnswer.length());
        List<String> lines = new ArrayList<>();
        String[] rawLines = rawAnswer.split("\n");
        boolean inList = false;
        StringBuilder currentLine = new StringBuilder();

        for (String line : rawLines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder();
                }
                inList = false;
                continue;
            }

            // Check if this is a bullet point or numbered list item
            Matcher bulletMatcher = BULLET_PATTERN.matcher(trimmedLine);
            Matcher numberMatcher = NUMBER_PATTERN.matcher(trimmedLine);

            if (bulletMatcher.find() || numberMatcher.find()) {
                // If we were building a paragraph, add it first
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder();
                }
                // Add the list item
                lines.add(trimmedLine);
                inList = true;
            } else if (inList) {
                // If we're in a list and this line is indented, it's part of the previous list item
                if (line.startsWith("    ") || line.startsWith("\t")) {
                    String lastLine = lines.get(lines.size() - 1);
                    lines.set(lines.size() - 1, lastLine + " " + trimmedLine);
                } else {
                    lines.add(trimmedLine);
                }
            } else {
                // Regular paragraph text
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(trimmedLine);
            }
        }

        // Add any remaining text
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }

        log.debug("Deserialized into {} lines", lines.size());
        return lines;
    }

    public static String serialize(List<String> answerLines) {
        if (answerLines == null || answerLines.isEmpty()) {
            return "";
        }

        return String.join("\n", answerLines);
    }
} 