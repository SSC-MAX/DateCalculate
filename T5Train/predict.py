from transformers import T5Tokenizer, T5ForConditionalGeneration
import json
from tqdm import tqdm
import pandas as pd
import ast

def trans_list(list_str):
    list_with_json_strings = ast.literal_eval(list_str)

    # 第二步：遍历列表，将每个JSON字符串转换为字典
    result_list = []
    for json_str in list_with_json_strings:
        # 解析JSON字符串为字典
        data_dict = json.loads(json_str)
        result_list.append(data_dict)
    return result_list

def delete_space(text):
    return json.dumps(text, separators=(',', ':')).replace(" ", "").replace("\n", "").replace("\r", "").replace("\\n","")

def compare(predict_text, target_text):
    predict_list = trans_list(predict_text)
    target_list = trans_list(target_text)
    if len(predict_list) != len(target_list):
        return False
    for i in range(len(predict_list)):
        if delete_space(predict_list[i]) != delete_space(target_list[i]):
            return False
    return True


def get_predict(tokenizer, model, input_text):
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
    return predicted_text


if __name__ == '__main__':
    # 模型保存路径（训练时 TrainingArguments.output_dir）
    model_path = 'models/models_v3/t5_time_extractor_r_p_1000_1000'
    file_path = "data/v3/test_1000_r_p_fix.csv"
    output_file = "data/v3/predict/test_1000_r_p_fix"

    df = pd.read_csv(file_path)
    input_texts = [e for e in df['input_text']]
    target_texts = [e for e in df['target_text']]

    # 加载 tokenizer 和 模型
    tokenizer = T5Tokenizer.from_pretrained(model_path)
    model = T5ForConditionalGeneration.from_pretrained(model_path)
    print(f'===T5加载完成===')

    print(f'================测试================\n'
          f'model_path: {model_path}\n'
          f'file_path:{file_path}\n'
          f'output_file:{output_file}\n'
          '================================')

    correct_count = 0
    result = []
    for index in tqdm(range(len(df))):
        input_text = input_texts[index]
        target_text = target_texts[index]
        try:
            predict_text = get_predict(tokenizer, model, input_text)
            if compare(predict_text, target_text):
                correct_count += 1
            result.append({
                'input_text': input_text,
                'target_text': target_text,
                'predict_text': predict_text
            })
            with open(f"{output_file}.json", "w") as f:
                json.dump(result, f, ensure_ascii=False, indent=4)
        except Exception as e:
            print("错误")
            with open(f"data/v3/predict/error.json", "w") as f:
                json.dump({"index":index, "input_text":input_text, "target_text":target_text, "error":str(e)}, f, ensure_ascii=False, indent=4)
    print(f"correct_count: {correct_count}/{len(df)}\n")

    with open(f"{output_file}.txt", 'w') as f:
        f.write(f"correct_count: {correct_count}/{len(df)}\n")
        f.write(f"correct_acc: {correct_count/len(df)}\n")

