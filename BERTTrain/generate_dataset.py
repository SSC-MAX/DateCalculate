import json
import random
from datetime import datetime

def generate_time_phrase():
    """生成随机时间短语及其对应的字段值"""
    # 基础时间组件及其对应字段值
    terms = [
        ("上学期", {"term": -1, "month": "none", "week": "none", "weekday": "none", "day": "none"}),
        ("本学期", {"term": 0, "month": "none", "week": "none", "weekday": "none", "day": "none"}),
        ("下学期", {"term": 1, "month": "none", "week": "none", "weekday": "none", "day": "none"})
    ]
    
    months = [
        ("上个月", {"term": "none", "month": -1, "week": "none", "weekday": "none", "day": "none"}),
        ("本月", {"term": "none", "month": 0, "week": "none", "weekday": "none", "day": "none"}),
        ("下个月", {"term": "none", "month": 1, "week": "none", "weekday": "none", "day": "none"})
    ]
    # 添加第X个月
    for i in range(1, 13):
        months.append((f"第{i}个月", {"term": "none", "month": i, "week": "none", "weekday": "none", "day": "none"}))
    
    weeks = [
        ("上周", {"term": "none", "month": "none", "week": -1, "weekday": "none", "day": "none"}),
        ("本周", {"term": "none", "month": "none", "week": 0, "weekday": "none", "day": "none"}),
        ("下周", {"term": "none", "month": "none", "week": 1, "weekday": "none", "day": "none"})
    ]
    # 添加第X周
    for i in range(1, 53):
        weeks.append((f"第{i}周", {"term": "none", "month": "none", "week": i, "weekday": "none", "day": "none"}))
    
    weekdays = [
        ("周一", {"term": "none", "month": "none", "week": "none", "weekday": 1, "day": "none"}),
        ("周二", {"term": "none", "month": "none", "week": "none", "weekday": 2, "day": "none"}),
        ("周三", {"term": "none", "month": "none", "week": "none", "weekday": 3, "day": "none"}),
        ("周四", {"term": "none", "month": "none", "week": "none", "weekday": 4, "day": "none"}),
        ("周五", {"term": "none", "month": "none", "week": "none", "weekday": 5, "day": "none"}),
        ("周六", {"term": "none", "month": "none", "week": "none", "weekday": 6, "day": "none"}),
        ("周日", {"term": "none", "month": "none", "week": "none", "weekday": 7, "day": "none"}),
        ("周天", {"term": "none", "month": "none", "week": "none", "weekday": 7, "day": "none"})
    ]
    
    days = [
        ("前天", {"term": "none", "month": "none", "week": "none", "weekday": "none", "day": -2}),
        ("昨天", {"term": "none", "month": "none", "week": "none", "weekday": "none", "day": -1}),
        ("今天", {"term": "none", "month": "none", "week": "none", "weekday": "none", "day": 0}),
        ("明天", {"term": "none", "month": "none", "week": "none", "weekday": "none", "day": 1}),
        ("后天", {"term": "none", "month": "none", "week": "none", "weekday": "none", "day": 2})
    ]
    # 添加X号
    for i in range(1, 32):
        days.append((f"{i}号", {"term": "none", "month": "none", "week": "none", "weekday": "none", "day": i}))
    
    # 组合所有组件
    all_components = terms + months + weeks + weekdays + days
    
    # 随机选择1-3个组件组合成时间短语
    num_components = random.randint(1, 3)
    selected_components = random.sample(all_components, num_components)
    
    # 合并短语部分和字段信息
    phrase_parts = []
    combined_fields = {
        "term": "none",
        "month": "none",
        "week": "none",
        "weekday": "none",
        "day": "none"
    }
    
    for part, fields in selected_components:
        phrase_parts.append(part)
        # 合并字段，后面的组件可以覆盖前面的
        for key, value in fields.items():
            if value != "none":
                combined_fields[key] = value
    
    # 随机选择连接词
    connectors = ["的", "", "，", "至"]
    phrase = ""
    for i, part in enumerate(phrase_parts):
        phrase += part
        if i < len(phrase_parts) - 1:
            phrase += random.choice(connectors)
    
    return phrase, combined_fields

def generate_sentence(templates, time_phrases):
    """根据模板和时间短语生成句子，并记录短语位置"""
    # 随机选择一个模板
    template = random.choice(templates)
    
    # 计算需要的时间短语数量
    required_phrases = template.count("{time}")
    selected_phrases = random.sample(time_phrases, min(required_phrases, len(time_phrases)))
    
    # 替换模板中的占位符并记录位置
    sentence = template
    phrase_info = []
    offset = 0
    
    for phrase, fields in selected_phrases:
        # 找到下一个占位符
        placeholder_pos = sentence.find("{time}", offset)
        if placeholder_pos == -1:
            break
            
        # 替换占位符
        sentence = sentence.replace("{time}", phrase, 1)
        
        # 记录短语信息
        phrase_info.append({
            "text": phrase,
            "start": placeholder_pos,
            "end": placeholder_pos + len(phrase) - 1,
            "fields": fields
        })
        
        # 更新偏移量
        offset = placeholder_pos + len(phrase)
    
    return sentence, phrase_info

def generate_dataset(size, output_file):
    """生成指定大小的数据集并保存到文件"""
    # 句子模板
    templates = [
        "{time}有一场重要的会议，记得参加。",
        "请在{time}前完成作业，并于{time}提交。",
        "{time}的课程将调整到{time}进行。",
        "学校将在{time}举行运动会，{time}开始报名。",
        "报告的截止日期是{time}，请务必在{time}前发送给我。",
        "{time}我们将进行期中考试，复习范围是{time}前讲过的内容。",
        "图书馆将在{time}闭馆整修，{time}重新开放。",
        "教师会议定于{time}召开，讨论{time}的教学安排。",
        "请于{time}到办公室领取{time}的活动门票。",
        "{time}的讲座取消，改为{time}举办。",
        "考试成绩将于{time}公布，{time}可以申请复查。",
        "学校将在{time}放暑假，{time}开学。"
    ]
    
    dataset = []
    
    for _ in range(size):
        # 生成一些时间短语
        num_phrases = random.randint(1, 3)
        time_phrases = [generate_time_phrase() for _ in range(num_phrases)]
        
        # 生成句子
        sentence, phrase_info = generate_sentence(templates, time_phrases)
        
        # 添加到数据集
        dataset.append({
            "sentence": sentence,
            "time_phrases": phrase_info
        })
    
    # 保存数据集
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(dataset, f, ensure_ascii=False, indent=2)
    
    print(f"数据集生成完成，共{size}条记录，已保存到{output_file}")

if __name__ == "__main__":
    # 生成1000条记录的数据集，保存为time_dataset.json
    generate_dataset(10, "time_dataset.json")
