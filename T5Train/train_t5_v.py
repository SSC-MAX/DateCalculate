from transformers import T5Tokenizer, T5ForConditionalGeneration, Trainer, TrainingArguments
from datasets import load_dataset

# 预处理函数
def preprocess(example):
    inputs = [ex for ex in example["input_text"]]
    targets = [ex for ex in example["target_text"]]

    model_inputs = tokenizer(inputs, max_length=64, truncation=True, padding="max_length")
    labels = tokenizer(targets, max_length=64, truncation=True, padding="max_length")
    model_inputs["labels"] = labels["input_ids"]

    return model_inputs



if __name__ == '__main__':
    model_path = "models/mengzi-t5-base"  # 预训练模型
    train_dataset_path = 'data/v3/train_1000_r_p_fix.csv'
    dev_dataset_path = 'data/v3/dev_200_r_p_fix.csv'
    output_dir = 'models/models_v3/t5_time_extractor_r_p_1000'
    logging_dir = 'models/models_v3/t_p_1000/logs'

    # 加载 tokenizer与model
    tokenizer = T5Tokenizer.from_pretrained(model_path)
    model = T5ForConditionalGeneration.from_pretrained(model_path)
    print(f'===T5完毕===')

    # 加载数据集
    dataset = load_dataset("csv", data_files={"train": train_dataset_path, "validation": dev_dataset_path})
    tokenized_datasets = dataset.map(preprocess, batched=True, remove_columns=dataset["train"].column_names)
    train_dataset = tokenized_datasets['train']
    validation_dataset = tokenized_datasets['validation']

    # 训练参数
    training_args = TrainingArguments(
        output_dir=output_dir,
        eval_strategy="epoch",
        save_strategy="epoch",
        learning_rate=3e-5,
        per_device_train_batch_size=8,
        per_device_eval_batch_size=8,
        num_train_epochs=10,
        weight_decay=0.01,
        save_total_limit=2,
        logging_dir=logging_dir,
        logging_steps=50,
        # max_steps=-1
    )

    # Trainer
    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=train_dataset,
        eval_dataset=validation_dataset,
        tokenizer=tokenizer
    )

    # 开始训练
    print(f'======开始训练======\ntrain_dataset:{train_dataset_path}\ndev_dataset:{dev_dataset_path}\noutput_dir:{output_dir}\n======')
    trainer.train()

    # 保存模型
    model.save_pretrained(f'{output_dir}_{len(train_dataset)}')
    tokenizer.save_pretrained(f'{output_dir}_{len(train_dataset)}')

