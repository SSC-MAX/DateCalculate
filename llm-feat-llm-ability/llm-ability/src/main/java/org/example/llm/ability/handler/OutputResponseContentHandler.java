package org.example.llm.ability.handler;

import org.example.llm.ability.model.protocol.SourceModelResponse;

import java.util.List;

public class OutputResponseContentHandler implements ChatResponseHandler {
    @Override
    public void handle(SourceModelResponse response) {
        if (response == null || response.getChoices() == null) {
            return;
        }

        List<SourceModelResponse.Choice> choices = response.getChoices();
        for (SourceModelResponse.Choice choice : choices) {
            System.out.print(choice.getMessage().getContent());
        }
        System.out.println();
    }
}
