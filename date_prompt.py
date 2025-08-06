from langchain.prompts import PromptTemplate
from langchain.chains import LLMChain


def get_time_extraction_chain(llm):
    time_extraction_prompt = PromptTemplate(
        input_variables=["user_query"],
        template="""
        任务：从用户问题中提取时间短语（如下周三、下个月第一个周一）,并将其转换为包含以下字段的json格式:"month"、"week"、"weekday"、"day"、"term"
        要求："month"表示据当前月份的距离，如下个月"month":1，上个月"month":-1，当前月:"month:0"；
        "week"表示第几周，
        "weekday"表示周几，该字段只填写数字，周一至周六分别对应123456，周日和周天都对应7，
        "day"表示与今天相隔几天，如"day:1"表示在今天之后1天，即明天；如"day:2"表示在今天之后2天，即后天；如"day:-1"表示在今天之前1一天，即昨天，
        "term"表示用户的问题是否根据学期来提问，"term:1"表示是根据学期提问，"term:0"表示不是根据学期提问，
        各字段如果根据用户问题找不到合适的值，就使用0填充
        只返回包含"month"、"week"、"weekday"、"term"、"day"字段的json对象即可,
        若包含多个不连续的时间短语，返回一个包含每个时间短语对应的json对象的列表，
        注意只提取与时间有关的短语，其他包含数字的短语如“第一节”等需要忽视
        用户问题：{user_query}
        """
    )
    return LLMChain(
        llm=llm,
        prompt=time_extraction_prompt
    )