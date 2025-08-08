import hanlp

# 加载SRL模型（支持中文语义角色标注）
srl = hanlp.load('SRL_BERT_base_Chinese')  # 基于BERT的中文SRL模型

# 调课句子示例
sentence = "将今天下午第二节课调整到后天上午第三节"

# 执行SRL分析（输入为分词后的列表，HanLP可自动分词，也可手动指定）
# 注意：SRL输入需为分词后的token列表，这里用HanLP的分词工具预处理
tokenizer = hanlp.load('PKU_NAME_MERGED_SIX_MONTHS_CONVSEG')  # 分词模型
tokens = tokenizer(sentence)  # 分词结果：['将', '今天下午', '第二节课', '调整', '到', '后天上午', '第三节']

# 执行SRL分析
srl_result = srl(tokens)
print(srl_result)