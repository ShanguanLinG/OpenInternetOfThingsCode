package org.shanguanling.project;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CarIdInfo {
    private String rfid;
    private String carId;
    private long startTime;
    private long endTime;
    private CarState state;

    public CarState getState() {
        return state;
    }

    public void setState(CarState state) {
        this.state = state;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public CarIdInfo(String uhfId, String carId, long startTime, long endTime) {
        this.rfid = uhfId;
        this.carId = carId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.state = CarState.None;
    }


    public String getCarId() {
        return carId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getFormatStartTime() {
        return getFormatTime(new Date(getStartTime()));
    }

    public long getEndTime() {
        return endTime;
    }

    public String getFormatEndTime() {
        return getFormatTime(new Date(getEndTime()));
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getTime() {
        return getEndTime() - getStartTime();
    }

    public String getFormatTime() {
        return millisToString(getEndTime() - getStartTime());
    }

    public int getPrice() {
        return (int) millisToMinutes(getEndTime() - getStartTime()) * 10 + 10;
    }

    private static String millisToString(long millis) {
        long totalMinutes = millisToMinutes(millis);
        long hour = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hour + "小时" + minutes + "分钟";
    }

    private static long millisToMinutes(long millis) {
        return millis / (60 * 1000);
    }

    private static String getFormatTime(Date time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(time);
    }
}