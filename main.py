from date_prompt import get_time_extraction_chain
from date_utils import get_target_date
from langchain_openai import ChatOpenAI
import json

if __name__ == '__main__':
    api_key = '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'

    llm = ChatOpenAI(
        model = 'gpt-3.5-turbo',
        temperature=0,
        api_key=api_key
    )

    # 用户输入
    query = "因参加专业技能提升课程，本学期第1周所有的思政课申请调至第2周。?"

    offset_list = json.loads(get_time_extraction_chain(llm).run(query.strip()).strip())
    print(f'======\n{offset_list}\n======')

    target_date_list = []
    for offset in offset_list:
        target_date_list.append(get_target_date(offset))
    target_date_list.sort()
    print(f'======\n{target_date_list}\n======')
