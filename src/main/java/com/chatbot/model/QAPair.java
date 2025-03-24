package com.chatbot.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.ai.document.Document;
import com.chatbot.util.AnswerDeserializer;
import java.util.List;

@Data
@Builder
public class QAPair {
    private String question;
    private List<String> answerLines;
    private float[] questionEmbedding;

    public String getFormattedAnswer() {
        return AnswerDeserializer.serialize(answerLines);
    }

    public Document toDocument() {
        Document doc = new Document(
            String.format("Q: %s\nA: %s", question, getFormattedAnswer()),
            java.util.Map.of(
                "question", question,
                "answer", getFormattedAnswer(),
                "answer_lines", String.join("|||", answerLines),
                "type", "qa_pair"
            )
        );
        if (questionEmbedding != null) {
            doc.setEmbedding(questionEmbedding);
        }
        return doc;
    }

    public static QAPair fromDocument(Document document) {
        String content = document.getContent();
        String[] parts = content.split("\n", 2); // Split only at first newline
        String question = parts[0].substring(3).trim(); // Remove "Q: "
        String answerPart = parts[1].substring(3).trim(); // Remove "A: "
        
        // Get answer lines from metadata if available, otherwise deserialize the answer part
        List<String> answerLines;
        if (document.getMetadata().containsKey("answer_lines")) {
            answerLines = java.util.Arrays.asList(
                document.getMetadata().get("answer_lines").toString().split("\\|\\|\\|")
            );
        } else {
            answerLines = AnswerDeserializer.deserialize(answerPart);
        }
        
        return QAPair.builder()
            .question(question)
            .answerLines(answerLines)
            .questionEmbedding(document.getEmbedding())
            .build();
    }

    public static QAPair fromRawQA(String question, String answer) {
        return QAPair.builder()
            .question(question)
            .answerLines(AnswerDeserializer.deserialize(answer))
            .build();
    }

    public static QAPair fromRawQAWithLines(String question, List<String> answerLines) {
        return QAPair.builder()
            .question(question)
            .answerLines(answerLines)
            .build();
    }
} 