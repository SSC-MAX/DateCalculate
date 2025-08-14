import pandas as pd
from openai import OpenAI
from tqdm import tqdm
import os

def get_rewrite(client, input_text):
    completion = client.chat.completions.create(
        # 指定您创建的方舟推理接入点 ID，此处已帮您修改为您的推理接入点 ID
        model="doubao-1-5-pro-32k-250115",
        messages=[
            {"role": "system",
             "content": "在保证下述文本不变的情况下，给它加上一段上下文，上下文中不要出现任何与时间有关的描述，添加后上下文总长度不要超过30个字,直接给出添加后的文本"},
            {"role": "user", "content": f"{input_text}"},
        ],
    )
    return completion.choices[0].message.content

if __name__ == '__main__':
    # api-key-20250814161832
    file_path = 'D:\\Projects\\GithubProjects\\DateCalculate\\T5Train\data\\v1\\train.csv'
    output_path = 'D:\\Projects\\GithubProjects\\DateCalculate\\T5Train\data\\v3'

    api_key = '846a5aad-87c5-4966-82e3-a5c684fb1bdd'
    client = OpenAI(
        # 此为默认路径，您可根据业务所在地域进行配置
        base_url="https://ark.cn-beijing.volces.com/api/v3",
        api_key=api_key
    )

    df = pd.read_csv(file_path)
    os.makedirs(output_path, exist_ok=True)
    input_texts = [e for e in df['input_text']]
    target_texts = [e for e in df['target_text']]
    result = []

    print(f'================\nfile_path:{file_path}\noutput_path:{output_path}\n================')

    for index in tqdm(range(600)):
        input_text = input_texts[index]
        target_text = target_texts[index]
        rewrite_text = get_rewrite(client, input_text)
        result.append({'input_text':rewrite_text, 'target_text':target_text})

    pd.DataFrame(result).to_csv(output_path+'\\train.csv', index=False)
    print(f'================完成================')
