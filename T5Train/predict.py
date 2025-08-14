from transformers import T5Tokenizer, T5ForConditionalGeneration


# 模型保存路径（训练时 TrainingArguments.output_dir）
model_path = 'models/models_v1/t5_time_extractor'

input_text = '后天下午上课'

# 加载 tokenizer 和 模型
tokenizer = T5Tokenizer.from_pretrained(model_path)
model = T5ForConditionalGeneration.from_pretrained(model_path)
print(f'===T5加载完成===')

# 编码成模型可接受的张量
input_ids = tokenizer(
    input_text,
    return_tensors="pt",  # 返回 PyTorch tensor
    max_length=64,
    truncation=True
).input_ids

# 生成结果（可以调整 max_length, num_beams 等参数）
outputs = model.generate(
    input_ids,
    max_length=64,
    num_beams=4,  # beam search，可提高生成质量
    early_stopping=True
)

# 解码成可读的字符串
predicted_text = tokenizer.decode(outputs[0], skip_special_tokens=True)

print(predicted_text)
