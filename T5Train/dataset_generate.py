import pandas as pd
import random
from datetime import datetime, timedelta

# 当前日期假设
today = datetime(2025, 8, 12)  # 可以改为 datetime.today()

# 星期映射（周一=1，周日=7）
weekday_map = {
    "周一": 1, "星期一": 1,
    "周二": 2, "星期二": 2,
    "周三": 3, "星期三": 3,
    "周四": 4, "星期四": 4,
    "周五": 5, "星期五": 5,
    "周六": 6, "星期六": 6,
    "周日": 7, "星期日": 7, "周天": 7
}

# 常用时间短语模板
month_phrases = ["本月", "下个月", "上个月"]
week_phrases = ["第一周", "第二周", "第三周", "第四周", "第五周"]
weekday_phrases = list(weekday_map.keys())
day_phrases = ["今天", "明天", "后天", "昨天", "前天"]
term_phrases = ["本学期", "下学期", "上学期"]
week_map = ["一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二", "十三", "十四", " 十五", "十六"]
things = ["开会", "请假", "上课", "考试", "检查"]

# 随机生成样本
def generate_sample():
    # 随机选择一个模板类型
    template_type = random.choice(["month_weekday", "day_only", "term_only"])

    if template_type == "month_weekday":
        month_phrase = random.choice(month_phrases)
        weekday_phrase = random.choice(weekday_phrases)
        sentence = f"{month_phrase}{weekday_phrase}{random.choice(things)}"
        month_val = {"本月": 0, "下个月": 1, "上个月": -1}[month_phrase]
        weekday_val = weekday_map[weekday_phrase]
        day_val = "none"  # 不精确计算具体天数
        return sentence, month_val, "none", weekday_val, day_val, "none"

    elif template_type == "day_only":
        day_phrase = random.choice(day_phrases)
        sentence = f"{day_phrase}{random.choice(things)}"
        month_val = 0
        weekday_val = (today + timedelta(days={
            "今天": 0, "明天": 1, "后天": 2, "昨天": -1, "前天": -2
        }[day_phrase])).isoweekday()
        day_val = {"今天": 0, "明天": 1, "后天": 2, "昨天": -1, "前天": -2}[day_phrase]
        return sentence, "none", "none", "none", day_val, "none"

    elif template_type == "term_only":
        term_phrase = random.choice(term_phrases)
        if random.randint(1,3) < 2:
            term_text = "最后一周"
        else:
            week_selected = random.randint(1, 16)
            if random.randint(1,10) < 5:
                term_text = f"第{week_selected}周"
            else:
                term_text = f"第{week_map[week_selected-1]}周"
        sentence = f"{term_phrase}的{term_text}{random.choice(things)}"
        term_val = {"本学期": 0, "下学期": 1, "上学期": -1}[term_phrase]
        if term_text == "最后一周":
            return sentence, "none", -1, "none", "none", term_val
        else:
            return sentence, "none", week_selected, "none", "none", term_val

# 生成数据集
def create_dataset(n_samples=500, cycle=1):
    rows = []
    for _ in range(n_samples):
        sentence_result = ""
        target_result = []
        for i in range(cycle):
            sentence, month, week, weekday, day, term = generate_sample()
            json_label = {
                "month": str(month),
                "week": str(week),
                "weekday": str(weekday),
                "day": str(day),
                "term": str(term)
            }
            if i == cycle-1:
                sentence_result += sentence
            else:
                sentence_result += sentence + ","
            target_result.append(str(json_label).replace("'", '"'))
        rows.append({"input_text": sentence_result, "target_text": target_result})

    return pd.DataFrame(rows)

if __name__ == '__main__':
    train_path = "data/v3/test_1000_r.csv"
    dev_path = "data/v3/dev_200_r.csv"

    train_df = create_dataset(n_samples=1000,cycle=1)
    # dev_df = create_dataset(n_samples=200, cycle=1)
    train_df.to_csv(train_path, index=False)
    # dev_df.to_csv(dev_path, index=False)

    print(f"数据集已生成：\ntrain:{train_path}\ndev:{dev_path}")

