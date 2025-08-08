package org.example.llm.ability.handler;

import lombok.Getter;
import org.example.llm.ability.model.protocol.SourceModelResponse;

import java.util.List;

@Getter
public class CollectThinkingAndContentHandler implements ChatResponseHandler {

    public CollectThinkingAndContentHandler(String question) {
        this.question = question;
    }

    String question;
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
                allThinkingContent.append(reasoningContent);
            }
            String content = choice.getDelta().getContent();
            if (content != null) {
                allContent.append(content);
            }
        }
    }
}
