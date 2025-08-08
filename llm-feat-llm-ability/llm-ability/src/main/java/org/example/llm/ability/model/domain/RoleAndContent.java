package org.example.llm.ability.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RoleAndContent {
    private String role;
    private String content;
}
