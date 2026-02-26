package org.shanguanling.project;

public class Information {

    // 数据类，仅包含三个字段用于存储商品信息
    // 每个对象代表一个商品，其中包含三种商品信息

    private String code;
    private String name;
    private int price;

    public Information(String code, String name, int price) {
        this.code = code;
        this.name = name;
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
}
