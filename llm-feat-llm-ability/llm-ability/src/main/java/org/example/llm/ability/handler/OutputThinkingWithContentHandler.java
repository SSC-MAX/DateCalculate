package org.example.llm.ability.handler;

import org.example.llm.ability.model.protocol.SourceModelResponse;
import org.example.llm.common.util.Checks;

import java.util.List;

public class OutputThinkingWithContentHandler implements ChatResponseHandler {

    boolean thinkingStart = false;
    boolean contentStart = false;

    StringBuilder allThinkingContent = new StringBuilder();
    StringBuilder allContent = new StringBuilder();

    @Override
    public void handle(SourceModelResponse response) {
        if (response == null || response.getChoices() == null) {
            return;
        }

        List<SourceModelResponse.Choice> choices = response.getChoices();
        for (SourceModelResponse.Choice choice : choices) {
            String reasoningContent = choice.getDelta().getReasoningContent();
            if (reasoningContent != null) {
                if (!thinkingStart) {
                    thinkingStart = true;
                    System.out.println("---------------思考过程----------------");
                }
                System.out.print(reasoningContent);
                allThinkingContent.append(reasoningContent);
            }
            String content = choice.getDelta().getContent();
            if (content != null) {
                if (!contentStart) {
                    contentStart = true;
                    System.out.println("\n\n---------------正文部分----------------");
                }
                System.out.print(content);
                allContent.append(content);
            }
        }
    }
}
