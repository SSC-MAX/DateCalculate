import torch
import torch.nn as nn
from transformers import BertTokenizer, BertForTokenClassification, Trainer, TrainingArguments
from datetime import datetime, timedelta
import json

"""
数据格式:
{
  "sentence": "我们下周三开会",
  "time_phrase": "下周三",
  "label": {
    "month": "none",
    "week": "none",
    "weekday": 3,
    "day": 3,  // 假设今天是周日
    "term": "none"
  }
}
"""

# 1. 数据处理
class TimeDataset(torch.utils.data.Dataset):
    def __init__(self, data, tokenizer, max_len=128):
        self.data = data
        self.tokenizer = tokenizer
        self.max_len = max_len

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        item = self.data[idx]
        sentence = item['sentence']
        labels = self._create_labels(sentence, item['time_phrase'])

        encoding = self.tokenizer(
            sentence,
            return_tensors='pt',
            max_length=self.max_len,
            padding='max_length',
            truncation=True
        )

        input_ids = encoding['input_ids'].flatten()
        attention_mask = encoding['attention_mask'].flatten()

        # 调整标签长度以匹配tokenizer的输出
        adjusted_labels = [-100] * self.max_len
        for i, label in enumerate(labels[:self.max_len - 2]):  # 留出[CLS]和[SEP]的位置
            adjusted_labels[i + 1] = label

        return {
            'input_ids': input_ids,
            'attention_mask': attention_mask,
            'labels': torch.tensor(adjusted_labels, dtype=torch.long)
        }

    def _create_labels(self, sentence, time_phrase):
        # 简单的标签创建：0=非时间，1=时间短语开始，2=时间短语中间
        labels = [0] * len(sentence)
        start_idx = sentence.find(time_phrase)
        if start_idx != -1:
            labels[start_idx] = 1
            for i in range(start_idx + 1, start_idx + len(time_phrase)):
                if i < len(labels):
                    labels[i] = 2
        return labels


# 2. 时间短语转换为JSON
class TimeConverter:
    def __init__(self):
        # 初始化当前时间（实际应用中应使用实时时间）
        self.today = datetime.now()

    def convert(self, time_phrase):
        """将时间短语转换为目标JSON格式"""
        result = {
            "month": "none",
            "week": "none",
            "weekday": "none",
            "day": "none",
            "term": "none"
        }

        # 处理周相关
        weekdays = {
            "周一": 1, "周二": 2, "周三": 3, "周四": 4,
            "周五": 5, "周六": 6, "周日": 7, "周天": 7
        }

        for day_name, num in weekdays.items():
            if day_name in time_phrase:
                result["weekday"] = num
                # 计算与今天相隔的天数
                days_diff = (num - self.today.weekday() - 1) % 7
                if "下" in time_phrase:
                    days_diff += 7
                elif "上" in time_phrase:
                    days_diff -= 7
                result["day"] = days_diff
                break

        # 处理月相关
        if "上个月" in time_phrase:
            result["month"] = -1
        elif "这个月" in time_phrase or "本月" in time_phrase:
            result["month"] = 0
        elif "下个月" in time_phrase:
            result["month"] = 1

        # 处理学期相关
        if "上学期" in time_phrase:
            result["term"] = -1
        elif "下学期" in time_phrase:
            result["term"] = 1

        # 处理第几周
        for i in range(1, 6):  # 假设最多5周
            if f"第{i}周" in time_phrase:
                result["week"] = i
                break

        return result


# 3. 模型训练与预测
def train_model(train_data, val_data, model_name="bert-base-chinese", epochs=3):
    tokenizer = BertTokenizer.from_pretrained(model_name)
    model = BertForTokenClassification.from_pretrained(
        model_name,
        num_labels=3  # 0:非时间, 1:时间开始, 2:时间中间
    )

    train_dataset = TimeDataset(train_data, tokenizer)
    val_dataset = TimeDataset(val_data, tokenizer)

    training_args = TrainingArguments(
        output_dir="./time_extraction_model",
        num_train_epochs=epochs,
        per_device_train_batch_size=8,
        per_device_eval_batch_size=8,
        evaluation_strategy="epoch",
        logging_dir="./logs",
    )

    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=train_dataset,
        eval_dataset=val_dataset
    )

    trainer.train()
    return model, tokenizer


def extract_time_phrases(sentence, model, tokenizer):
    """从句子中提取时间短语"""
    encoding = tokenizer(
        sentence,
        return_tensors='pt',
        padding=True,
        truncation=True
    )

    with torch.no_grad():
        outputs = model(**encoding)
        predictions = torch.argmax(outputs.logits, dim=2)

    tokens = tokenizer.convert_ids_to_tokens(encoding['input_ids'][0])
    time_phrase = []
    in_time = False

    for token, pred in zip(tokens, predictions[0]):
        if pred == 1:  # 时间开始
            in_time = True
            time_phrase.append(token.replace('##', ''))
        elif pred == 2 and in_time:  # 时间中间
            time_phrase.append(token.replace('##', ''))
        else:
            in_time = False

    return ''.join(time_phrase)


# 4. 主函数
def main(sentence, model, tokenizer):
    # 提取时间短语
    time_phrase = extract_time_phrases(sentence, model, tokenizer)
    if not time_phrase:
        return json.dumps({
            "month": "none", "week": "none",
            "weekday": "none", "day": "none", "term": "none"
        }, ensure_ascii=False)

    # 转换为JSON格式
    converter = TimeConverter()
    result = converter.convert(time_phrase)
    return json.dumps(result, ensure_ascii=False)


# 使用示例
if __name__ == "__main__":
    # 示例数据（实际应用中需要更大的标注数据集）
    sample_train_data = [
        {
            "sentence": "下周三我们有个会议",
            "time_phrase": "下周三",
            "label": {"month": "none", "week": "none", "weekday": 3, "day": 3, "term": "none"}
        },
        {
            "sentence": "下个月第一个周一交报告",
            "time_phrase": "下个月第一个周一",
            "label": {"month": 1, "week": 1, "weekday": 1, "day": "none", "term": "none"}
        }
    ]

    # 训练模型（实际应用中需要更多数据和epochs）
    model, tokenizer = train_model(sample_train_data, sample_train_data, epochs=1)

    # 测试
    test_sentence = "下周三开会，下个月提交报告"
    result = main(test_sentence, model, tokenizer)
    print(result)
