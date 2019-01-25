package other;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
 * Date相关操作
 * DateFormat主要参数含义:
 * | G | 公元 | AD |
 * | y | 年 | 2015 |
 * | M | 月 | 11 |
 * | d | 月中天数 | 11 |
 * | D | 年中天数 | 364 |
 * | w | 年中周数 | 52 |
 * | W | 月中周数 | 2 |
 * | F | 月中星期？ | 3 |
 * | E | 星期几 | Thu |
 * | a | 上下午标志 | AM |
 * | H | 小时(0-23) | 21 |
 * | k | 小时(1-24) | 22 |
 * | K | 小时(1-12) | 11 |
 * | h | 小时(0-11) | 10 |
 * | m | 分钟 | 23 |
 * | s | 秒 | 34 |
 * | S | 毫秒 | 345 |
 * | z | 时区 | CST |
 * | Z | 时区 | +0800 |
 */
public class DateTest {

	/*
	 * String <-> Date
	 */
	public static void main(String[] args) {
		String str = "2015-01-01 12:34:45 Thu";
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E",Locale.ENGLISH);
		try{
			//Date -> String
			String dateStr = format.format(new Date());
			System.out.println("Date -> String: "+dateStr);
			//String -> Date
			Date date = format.parse(str);
			System.out.println("String -> Date: "+date);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
