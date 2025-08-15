import pandas as pd
import json

def compare_predict(target_text, predict_text):
    target_format = target_text.replace(" ", "").replace("\n", "").replace("\\n", "").replace("\r", "")
    predict_format = predict_text.replace(" ", "").replace("\n", "").replace("\\n", "").replace("\r", "")
    return target_format == predict_format

train_path = 'D:\Projects\GithubProjects\DateCalculate\T5Train\data\\v3\\train_1000_fix.csv'
test_path = 'D:\Projects\GithubProjects\DateCalculate\T5Train\data\\v3\\test_1000_fix.csv'
df1 = pd.read_csv(train_path)
df2 = pd.read_csv(test_path)

train_input_texts = [e for e in df1['input_text']]
test_input_texts = [e for e in df2['input_text']]
area_list = []

for index in range(0, len(df1)):
    train_input_text = train_input_texts[index]
    test_input_text = test_input_texts[index]
    if compare_predict(train_input_text, test_input_text):
        area_list.append({
            'train_text':train_input_text,
            'test_text':test_input_text
        })

print(len(area_list))
