package org.example.llm.common.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : zybi
 * @date : 2025/5/13 20:16
 */
@Data
@Accessors(chain = true)
public class UserBasicInfo {
    private String roleEnName;          // 角色名称
    private String schoolId;            // 学校id
}
