from transformers import (
    T5Tokenizer,
    T5ForConditionalGeneration,
    DataCollatorForSeq2Seq,
    Seq2SeqTrainingArguments,
    Seq2SeqTrainer
)

import torch
import json
import os
from datasets import Dataset

# 1. 配置参数
class Config:
    # 模型选择：可根据需求选择不同大小的T5模型
    # 中文推荐使用："Langboat/mengzi-t5-base" 或 "google/mt5-base"
    def __init__(self, model_name, output_dir, log_dir, data_path):
        self.model_name = model_name  # 孟子T5，中文优化版
        self.output_dir = output_dir
        self.log_dir = log_dir
        self.data_path = data_path  # 数据集路径
        # 训练参数
        self.batch_size = 8
        self.num_epochs = 10
        self.learning_rate = 3e-5  # T5通常使用较低的学习率
        self.max_input_length = 128
        self.max_target_length = 128
        self.val_size = 0.2  # 验证集比例
        self.task_prefix = "提取时间信息并转换为JSON: "  # T5任务前缀，明确任务目标
    
    
# 2. 加载和预处理数据集
def load_dataset(config):
    """加载数据集并分割为训练集和验证集"""
    if not os.path.exists(config.data_path):
        create_sample_dataset(config)
        print('===未找到本地数据集，使用示例数据集===')
    
    # 读取JSON数据
    with open(config.data_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # 转换为Dataset格式
    dataset = Dataset.from_list(data)
    
    # 分割训练集和验证集
    train_val = dataset.train_test_split(test_size=config.val_size, seed=42)
    return train_val

def preprocess_function(examples, tokenizer, config):
    """预处理函数：添加任务前缀并转换为模型输入"""
    # 为输入文本添加任务前缀，帮助模型理解任务目标
    inputs = [config.task_prefix + ex["input_text"] for ex in examples]
    
    # 处理输入文本
    model_inputs = tokenizer(
        inputs,
        max_length=config.max_input_length,
        padding="max_length",
        truncation=True,
        return_tensors="pt"
    )

    # 处理目标JSON字符串
    targets = [ex["target_json"] for ex in examples]
    labels = tokenizer(
        targets,
        max_length=config.max_target_length,
        padding="max_length",
        truncation=True,
        return_tensors="pt"
    ).input_ids

    # 用-100标记填充部分，避免计算损失
    labels = [[(l if l != tokenizer.pad_token_id else -100) for l in label] for label in labels]
    model_inputs["labels"] = labels
    
    return model_inputs

# 3. 初始化模型和分词器
def init_model_and_tokenizer(config):
    """初始化T5模型和分词器"""
    tokenizer = T5Tokenizer.from_pretrained(config.model_name)
    
    # 初始化模型
    if os.path.exists(os.path.join(config.output_dir, "pytorch_model.bin")):
        model = T5ForConditionalGeneration.from_pretrained(config.output_dir)
        print('===T5加载: 已保存===')
    else:
        model = T5ForConditionalGeneration.from_pretrained(config.model_name)
        print('===T5加载: 为保存===')
    
    return model, tokenizer

# 4. 训练函数
def train(config:Config):
    # 初始化配置
    # config = Config()
    
    # 创建输出目录
    os.makedirs(config.output_dir, exist_ok=True)
    os.makedirs(config.log_dir, exist_ok=True)
    print(f'======\noutput_dir:{config.output_dir}\nlog_dir:{config.log_dir}\n======')
    
    # 加载数据集
    dataset = load_dataset(config)
    print(f"训练集大小: {len(dataset['train'])}")
    print(f"验证集大小: {len(dataset['test'])}")
    
    # 初始化模型和分词器
    model, tokenizer = init_model_and_tokenizer(config)
    
    # 预处理数据集
    tokenized_dataset = dataset.map(
        lambda x: preprocess_function(x, tokenizer, config),
        batched=True,
        remove_columns=dataset["train"].column_names
    )
    
    # 数据收集器
    data_collator = DataCollatorForSeq2Seq(
        tokenizer=tokenizer,
        model=model,
        label_pad_token_id=-100
    )
    
    # 训练参数
    training_args = Seq2SeqTrainingArguments(
        output_dir=config.output_dir,
        num_train_epochs=config.num_epochs,
        per_device_train_batch_size=config.batch_size,
        per_device_eval_batch_size=config.batch_size,
        evaluation_strategy="epoch",
        save_strategy="epoch",
        logging_dir=config.log_dir,
        logging_steps=10,
        learning_rate=config.learning_rate,
        weight_decay=0.01,
        predict_with_generate=True,  # 评估时使用生成模式
        fp16=False,  # 有GPU支持可开启
        load_best_model_at_end=True,
        metric_for_best_model="eval_loss",
        greater_is_better=False,
        # T5特有的参数
        generation_max_length=config.max_target_length,
        generation_num_beams=5
    )
    
    # 初始化Trainer
    trainer = Seq2SeqTrainer(
        model=model,
        args=training_args,
        train_dataset=tokenized_dataset["train"],
        eval_dataset=tokenized_dataset["test"],
        tokenizer=tokenizer,
        data_collator=data_collator,
    )
    
    # 开始训练
    print("开始训练...")
    trainer.train()
    
    # 保存最终模型
    model.save_pretrained(config.output_dir)
    tokenizer.save_pretrained(config.output_dir)
    print(f"模型已保存至 {config.output_dir}")

# 5. 推理函数
def generate_time_json(input_text, model_path=None):
    """使用训练好的T5模型生成时间信息JSON"""
    config = Config()
    model_path = model_path or config.output_dir
    
    # 加载模型和分词器
    tokenizer = T5Tokenizer.from_pretrained(model_path)
    model = T5ForConditionalGeneration.from_pretrained(model_path)
    model.eval()
    
    # 为输入添加任务前缀
    input_with_prefix = config.task_prefix + input_text
    
    # 编码输入文本
    inputs = tokenizer(
        input_with_prefix,
        max_length=config.max_input_length,
        padding="max_length",
        truncation=True,
        return_tensors="pt"
    )
    
    # 生成JSON
    with torch.no_grad():
        outputs = model.generate(
            input_ids=inputs["input_ids"],
            attention_mask=inputs["attention_mask"],
            max_length=config.max_target_length,
            num_beams=5,
            early_stopping=True,
            no_repeat_ngram_size=2
        )
    
    # 解码生成结果
    generated_json = tokenizer.decode(outputs[0], skip_special_tokens=True)
    
    # 验证并修复JSON格式
    try:
        return json.loads(generated_json), generated_json
    except json.JSONDecodeError:
        # 简单修复尝试
        print(f'===错误，尝试修复:{generated_json}===')
        fixed = generated_json.replace("'", '"').strip()
        if not fixed.startswith("{"):
            fixed = "{" + fixed
        if not fixed.endswith("}"):
            fixed = fixed + "}"
        try:
            return json.loads(fixed), fixed
        except:
            return None, generated_json

# 6. 示例数据集生成
def create_sample_dataset(config):
    """创建示例数据集"""
    sample_data = [
        {
            "input_text": "下周三我们有个会议",
            "target_json": '{"month":"none","week":"none","weekday":3,"day":0,"term":"none"}'
        },
        {
            "input_text": "下个月第一个周一交报告",
            "target_json": '{"month":1,"week":1,"weekday":1,"day":"none","term":"none"}'
        },
        {
            "input_text": "上周四完成了项目",
            "target_json": '{"month":"none","week":"none","weekday":4,"day":0,"term":"none"}'
        },
        {
            "input_text": "这个月的第三周开始放假",
            "target_json": '{"month":0,"week":3,"weekday":1,"day":"none","term":"none"}'
        },
        {
            "input_text": "下学期开学时间是9月1日",
            "target_json": '{"month":"none","week":"none","weekday":"none","day":"none","term":1}'
        },
        {
            "input_text": "上学期期末考试在1月",
            "target_json": '{"month":1,"week":"none","weekday":"none","day":"none","term":-1}'
        },
        {
            "input_text": "明天下午三点开会",
            "target_json": '{"month":"none","week":"none","weekday":"none","day":1,"term":"none"}'
        },
        {
            "input_text": "前天完成了作业",
            "target_json": '{"month":"none","week":"none","weekday":"none","day":-2,"term":"none"}'
        }
    ]
    
    # 保存示例数据
    with open(config.data_path, 'w', encoding='utf-8') as f:
        json.dump(sample_data, f, ensure_ascii=False, indent=2)
    print(f"示例数据集已生成至 {config.data_path}")

# 主函数
if __name__ == "__main__":

    model_name = '/data/znzl/Projects/models/mengzi-t5-base'
    output_dir = "/data/znzl/Projects/T5Train/models/t5_time_json_model"
    log_dir = "/data/znzl/Projects/T5Train/models/t5_logs"
    data_path = "/data/znzl/Projects/T5Train/models/time_data.json"

    # 训练模型
    config = Config(model_name, output_dir, log_dir, data_path)
    train(config)
    
    # 测试推理
    test_texts = [
        "下周三下午开会",
        "下个月第一个周一开始放假",
        "上学期的考试安排在12月"
    ]
    
    print("\n推理测试:")
    for text in test_texts:
        result, raw = generate_time_json(text)
        print(f"输入: {text}")
        print(f"生成JSON: {raw}")
        print(f"解析结果: {result}\n")
