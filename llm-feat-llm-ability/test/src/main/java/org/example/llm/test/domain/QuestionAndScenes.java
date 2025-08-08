package org.example.llm.test.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class QuestionAndScenes {

    private String question;
    private String intention;
    private List<String> scenes;

}
