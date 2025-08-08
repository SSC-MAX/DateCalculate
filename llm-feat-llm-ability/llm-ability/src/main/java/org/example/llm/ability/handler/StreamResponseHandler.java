package org.example.llm.ability.handler;

import org.example.llm.ability.model.protocol.SourceModelResponse;
import org.example.llm.common.util.Checks;

import java.util.List;

/**
 * @author : zybi
 * @date : 2025/5/19 20:22
 */
public class StreamResponseHandler {

    private boolean startedReasoningContent = false;
    private boolean startedText = false;

    public void output(SourceModelResponse response) {
        if (response == null) {
            return;
        }

        List<SourceModelResponse.Choice> choices = response.getChoices();
        if (Checks.isNull( choices )) {
            return;
        }

        for (SourceModelResponse.Choice choice : choices) {
            SourceModelResponse.ChoiceDelta delta = choice.getDelta();
            if (delta == null) {
                continue;
            }
            String reasoningContent = delta.getReasoningContent();
            String content = delta.getContent();
            if (Checks.noNull( reasoningContent )) {
                if (!startedReasoningContent) {
                    System.out.println("\n-----------思考过程-----------");
                    startedReasoningContent = true;
                }
                System.out.print(reasoningContent);
            }

            if (Checks.noNull( content )) {
                if (!startedText) {
                    System.out.println("\n-----------文本内容-----------");
                    startedText = true;
                }
                System.out.print(content);
            }
        }
    }
}
