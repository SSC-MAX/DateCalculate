from datetime import datetime
from dateutil.relativedelta import relativedelta, MO, TU, WE, TH, FR, SA, SU

# 预设的学期开始与结束时间
term_start_date = datetime(2025, 8, 1)
term_end_date = datetime(2025, 10, 31)


def get_target_date(offset, start_date=None):
    """
    offset:{
        'month': 0表示当月，1表示下一个月，-1表示上一个月
        'week'： 0表示当周，1表示下一周，-1表示上一周
        'weekday'：周几
    }
    """
    weekday_map = {
        0: MO,
        1: MO,
        2: TU,
        3: WE,
        4: TH,
        5: FR,
        6: SA,
        7: SU
    }
    weekday = weekday_map[offset['weekday']]
    # 不根据学期查询
    if offset['term'] == 0:

        start_date = datetime.today()

        start_week = (start_date.day + start_date.replace(day=1).weekday() - 1) // 7 + 1        # 起始日期所在月的周数
        target_week = offset['week'] + start_week if offset['month'] == 0 else offset['week']   # 目标周

        if offset['month'] != 0 or offset['weekday'] != 0:
            """
            下个月的第一周的周三/下个月的第一周/本周的周三/上周的周三
            首先定位到月初第一天，然后再数到目标周
            """
            target_date = start_date + relativedelta(months=int(offset['month']), day=1, days=offset['day'],
                                                     weekday=weekday(target_week))
            print('===1===')
        else:
            # 不涉及月份及星期几
            target_date = start_date + relativedelta(months=0, weeks=offset['week'], days=offset['day'])
            print('===2===')
        return target_date.strftime('%Y-%m-%d-%A')
    # 根据学期查询
    else:
        target_month = offset['month']-1 if offset['month']!=0 else 0
        target_week = offset['week']-1 if offset['week']!=0 else 0
        print(f'target_month:{target_month}, target_week:{target_week}')
        target_date = term_start_date + relativedelta(months=target_month, weeks=target_week, weekday=weekday)
        if target_date <= term_end_date:
            return target_date.strftime('%Y-%m-%d-%A')
        else:
            return "日期错误"
