import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    /**
     * 时间格式化方案
     * Java8之前使用SimpleDateFormat，
     * Java8之后使用DateTimeFormatter
     */
    public static void main(String[] args) {
        Date date1 = new Date();
        Date date2 = new Date();
        System.out.println(getTimeNow(date1));
        long timeDiffMillis = getTimeDiffMillis(date1, date2);
        System.out.println(timeDiffMillis);
        System.out.println(millisToString(timeDiffMillis));
    }

    /**
     * 获取现在的格式化时间
     */
    private static String getTimeNow(Date time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(time);
    }

    /**
     * 获取两个时间差的毫秒数
     */
    private static long getTimeDiffMillis(Date start, Date end) {
        // 获取两个日期对象相差的值（单位：毫秒数）
        long diffMillis = end.getTime() - start.getTime();
        // 避免出现负数，如果出现负数就取相反值
        if (diffMillis < 0) diffMillis = -diffMillis;
        return diffMillis;
    }

    /**
     * 将毫秒数转化为分钟
     */
    private static long millisToMinutes(long millis) {
        return millis / (60 * 1000);
    }

    /**
     * 将毫秒数转化为格式化字符（x小时x分钟）
     */
    private static String millisToString(long millis) {
        // 将毫秒数转化为分钟
        long totalMinutes = millisToMinutes(millis);
        // 获取小时数
        long hour = totalMinutes / 60;
        // 获取分钟数
        long minutes = totalMinutes % 60;
        // 进行拼接
        return hour + "小时" + minutes + "分钟";
    }
}
