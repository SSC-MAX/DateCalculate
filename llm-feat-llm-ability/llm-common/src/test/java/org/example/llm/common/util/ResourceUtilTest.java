package org.example.llm.common.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class ResourceUtilTest {

    @Test
    public void test() throws IOException {
        String readJsonFromResource = ResourceUtil.readJsonFromResource("test-data.json");
        System.out.println(readJsonFromResource);

        System.out.println(getRecentlyTime());
    }

    private List<String> getRecentlyTime() {
        // 设置日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        SimpleDateFormat curFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 1. 今天
        LocalDate today = LocalDate.now();

        // 2. 昨天
        LocalDate yesterday = today.minusDays(1);

        // 3. 明天
        LocalDate tomorrow = today.plusDays(1);

        // 4. 后天
        LocalDate dayAfterTomorrow = today.plusDays(2);

        // 5. 大后天
        LocalDate dayAfterDayAfterTomorrow = today.plusDays(3);

        // 6. 前天
        LocalDate dayBeforeYesterday = today.minusDays(2);

        // 7. 本周一到本周日
        LocalDate mondayThisWeek = today.with(DayOfWeek.MONDAY);
        LocalDate sundayThisWeek = today.with(DayOfWeek.SUNDAY);

        // 8. 上周一到上周日
        LocalDate mondayLastWeek = mondayThisWeek.minusWeeks(1);
        LocalDate sundayLastWeek = sundayThisWeek.minusWeeks(1);

        // 9. 下周一到下周日
        LocalDate mondayNextWeek = mondayThisWeek.plusWeeks(1);
        LocalDate sundayNextWeek = sundayThisWeek.plusWeeks(1);

        // 10. 上月
        YearMonth lastMonth = YearMonth.from(today).minusMonths(1);
        LocalDate firstDayOfLastMonth = lastMonth.atDay(1);
        LocalDate lastDayOfLastMonth = lastMonth.atEndOfMonth();

        // 11. 本月
        YearMonth thisMonth = YearMonth.from(today);
        LocalDate firstDayOfThisMonth = thisMonth.atDay(1);
        LocalDate lastDayOfThisMonth = thisMonth.atEndOfMonth();

        // 12. 下月
        YearMonth nextMonth = YearMonth.from(today).plusMonths(1);
        LocalDate firstDayOfNextMonth = nextMonth.atDay(1);
        LocalDate lastDayOfNextMonth = nextMonth.atEndOfMonth();

        // 额外示例：获取本周所有日期
        List<String> datesThisWeek = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String weekName = "";
            switch (i) {
                case 0 -> weekName = "一";
                case 1 -> weekName = "二";
                case 2 -> weekName = "三";
                case 3 -> weekName = "四";
                case 4 -> weekName = "五";
                case 5 -> weekName = "六";
                case 6 -> weekName = "日";
            }
            datesThisWeek.add("本周"+weekName+": "+mondayThisWeek.plusDays(i));
        }
        List<String> strings = new ArrayList<>();
        strings.add("当前时间： " + curFormatter.format(new Date()));
        strings.add("前天: " + dayBeforeYesterday.format(formatter));
        strings.add("昨天: " + yesterday.format(formatter));
        strings.add("明天: " + tomorrow.format(formatter));
        strings.add("后天: " + dayAfterTomorrow.format(formatter));
        strings.add("大后天: " + dayAfterDayAfterTomorrow.format(formatter));
        strings.add("上周一: " + mondayLastWeek.format(formatter));
        strings.add("上周日: " + sundayLastWeek.format(formatter));
        strings.add("下周一: " + mondayNextWeek.format(formatter));
        strings.add("下周日: " + sundayNextWeek.format(formatter));
        strings.addAll(datesThisWeek);
        strings.add("上月第一天: " + firstDayOfLastMonth.format(formatter));
        strings.add("上月最后一天: " + lastDayOfLastMonth.format(formatter));
        strings.add("本月第一天: " + firstDayOfThisMonth.format(formatter));
        strings.add("本月最后一天: " + lastDayOfThisMonth.format(formatter));
        strings.add("下月第一天: " + firstDayOfNextMonth.format(formatter));
        strings.add("下月最后一天: " + lastDayOfNextMonth.format(formatter));
        return strings;
    }
}