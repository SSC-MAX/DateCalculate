import pandas as pd
from openai import OpenAI
from tqdm import tqdm
import os

def get_rewrite(client, prompt, input_text):
    completion = client.chat.completions.create(
        # 指定您创建的方舟推理接入点 ID，此处已帮您修改为您的推理接入点 ID
        model="doubao-1-5-pro-32k-250115",
        messages=[
            {"role": "system", "content": f"{prompt}"},
            {"role": "user", "content": f"{input_text}"},
        ],
    )
    return completion.choices[0].message.content

if __name__ == '__main__':
    file_path = 'D:\\Projects\\GithubProjects\\DateCalculate\\T5Train\data\\v3\\train_1000_p.csv'
    output_path = 'D:\\Projects\\GithubProjects\\DateCalculate\\T5Train\data\\v3'
    output_name = 'train_1000_p.csv'

    api_key = '846a5aad-87c5-4966-82e3-a5c684fb1bdd'
    prompt = """
              为用户输入的文本添加一段上下文
              要求：
              1.不允许对输入文本做任何修改
              2.添加的上下文不允许出现任何与时间有关的短语
              3.添加后，整段文本不要超过40个字
              """
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

    for index in tqdm(range(len(df))):
        input_text = input_texts[index]
        target_text = target_texts[index]
        rewrite_text = '"' + get_rewrite(client, prompt, input_text) + '"'
        result.append({'input_text':rewrite_text, 'target_text':target_text})

    pd.DataFrame(result).to_csv(f'{output_path}\\{output_name}', index=False)
    print(f'================完成================')
