import torch
from transformers import BertTokenizer
from torch.utils.data import Dataset

tokenizer = BertTokenizer.from_pretrained("bert-base-chinese")

# 字段值到索引的映射（示例，需根据实际标注扩展）
FIELD_MAPPINGS = {
    "term": {"none": 0, -1: 1, 0: 2, 1: 3},
    "month": {"none": 0, -1: 1, 0: 2, 1: 3, 2: 4, 3: 5},  # 扩展到12
    "week": {"none": 0, -1: 1, 0: 2, 1: 3, 2: 4},  # 扩展到52
    "weekday": {"none": 0, 1: 1, 2: 2, 3: 3, 4: 4, 5: 5, 6: 6, 7: 7},
    "day": {"none": 0, -2: 1, -1: 2, 0: 3, 1: 4, 2: 5}  # 扩展到31
}

class TimeDataset(Dataset):
    def __init__(self, data):
        self.data = data
        self.max_len = 128

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        item = self.data[idx]
        sentence = item["sentence"]
        time_phrases = item["time_phrases"]
        
        # 1. Tokenize句子
        encoding = tokenizer(
            sentence,
            max_length=self.max_len,
            padding="max_length",
            truncation=True,
            return_offsets_mapping=True,
            return_tensors="pt"
        )
        input_ids = encoding["input_ids"].flatten()
        attention_mask = encoding["attention_mask"].flatten()
        offset_mapping = encoding["offset_mapping"].flatten(start_dim=0)  # (max_len, 2)
        
        # 2. 生成实体识别标签（BIO标签）
        ner_labels = torch.zeros(self.max_len, dtype=torch.long)  # 0: O, 1: B-TIME, 2: I-TIME
        for phrase in time_phrases:
            start_char = phrase["start"]
            end_char = phrase["end"]
            # 找到token级别的起始和结束索引
            start_token = None
            end_token = None
            for i, (start, end) in enumerate(offset_mapping):
                if start <= start_char < end and start_token is None:
                    start_token = i
                if start < end_char <= end and end_token is None:
                    end_token = i
            if start_token is not None and end_token is not None:
                ner_labels[start_token] = 1  # B-TIME
                for i in range(start_token + 1, end_token + 1):
                    ner_labels[i] = 2  # I-TIME
        
        # 3. 生成字段标签（仅取第一个时间短语示例，多短语需扩展）
        if time_phrases:
            fields = time_phrases[0]["fields"]
            field_labels = {
                key: torch.tensor(FIELD_MAPPINGS[key][value], dtype=torch.long)
                for key, value in fields.items()
            }
        else:
            # 无时间短语时，字段均为"none"
            field_labels = {
                key: torch.tensor(0, dtype=torch.long) for key in FIELD_MAPPINGS.keys()
            }
        
        return {
            "input_ids": input_ids,
            "attention_mask": attention_mask,
            "ner_labels": ner_labels,
            **field_labels
        }