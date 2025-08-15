import pandas as pd
import random
import json

# 为每个月添加具体的星期数
def add_week(file_path, output_path, output_name):
    df = pd.read_csv(file_path)
    input_texts = [e for e in df['input_text']]
    target_texts = [e for e in df['target_text']]
    select_choice = ['第一周', '第二周', '第三周', '第一个星期', '第二个星期', '第三个星期']
    select_map = {
        '第一周': "1", '第一个星期': "1",
        '第二周': "2", '第二个星期': "2",
        '第三周': "3", '第三个星期': "3"
    }

    result = []
    for index in range(len(df)):
        input_text = input_texts[index]
        target_text_list = json.loads(target_texts[index])
        target_result = []
        for target_text in target_text_list:  # 处理每一个target
            if '月星期' in input_text or '月周' in input_text:
                selected_text = random.choice(select_choice)
                selected_target = select_map[selected_text]  # 随机选择的周
                insert_index = input_text.find('月星期')
                if insert_index < 0:
                    insert_index = input_text.find('月周')
                store_text = input_text[insert_index + 1:]
                input_text = input_text[:insert_index + 1] + selected_text + store_text  # 组装输入
                # print(target_text)
                target_json = target_text
                target_json['week'] = selected_target
                target_text = json.dumps(target_json, ensure_ascii=False, indent=2)
            target_result.append(target_text)

        result.append({
            "input_text": input_text,
            "target_text": str(target_result)
        })

    pd.DataFrame(result).to_csv(f'{output_path}{output_name}', index=False)

# 当只涉及天时，将weekday设为none
def fix_weekday(file_path, output_path, output_name):
    df = pd.read_csv(file_path)
    input_texts = [e for e in df['input_text']]
    target_texts = [e for e in df['target_text']]

    result = []
    for index in range(len(df)):
        input_text = input_texts[index]
        target_text_list = json.loads(target_texts[index])
        target_text_result = []
        for target_text in target_text_list:
            json_target = target_text
            if "天" in input_text and json_target['weekday'] != "none" and json_target['day'] != "none":
                json_target['weekday'] = 'none'
                target_text = json.dumps(json_target, ensure_ascii=False, indent=2)
            target_text_result.append(target_text)

        result.append({
            "input_text":input_text,
            "target_text":str(target_text_result)
        })
    pd.DataFrame(result).to_csv(f'{output_path}{output_name}', index=False)

# 删除空格和换行
def delete_space(file_path, output_path, output_name):
    df = pd.read_csv(file_path)
    input_texts = [e for e in df['input_text']]
    target_texts = [e for e in df['target_text']]
    result = []
    for index in range(len(df)):
        target_text_list = json.loads(target_texts[index])
        target_result = []
        for target_text in target_text_list:
            target_result.append(json.dumps(target_text, separators=(',', ':')).replace(" ", "").replace("\n", "").replace("\r", "").replace("\\n",""))
        result.append({
            'input_text': input_texts[index].strip().replace(" ", "").replace("\n", "").replace("\r", "").replace("\\n",""),
            'target_text': str(target_result)
        })
    pd.DataFrame(result).to_csv(f'{output_path}{output_name}', index=False)



if __name__ == '__main__':
    file_path = 'D:\Projects\GithubProjects\DateCalculate\T5Train\data\\v4\\train_1000.csv'
    output_path = 'D:\Projects\GithubProjects\DateCalculate\T5Train\data\\v4\\'
    output_name = 'train_1000_fix.csv'

    # delete_space(file_path, output_path, output_name)

    # fix_weekday(file_path, output_path, output_name)
    # add_week(file_path, output_path, output_name)
    delete_space(file_path, output_path, output_name)
