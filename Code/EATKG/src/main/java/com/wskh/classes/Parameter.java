package com.wskh.classes;

public class Parameter {
    public static double EPS = 1e-10;
    public static boolean CheckEnable = true;
    public static double Alpha = 1.2; // 控制一刀切空间划分

    // 组合类型
    public static final short V_Split = 0; // 左右
    public static final short H_Split = 1; // 上下
    public static final short None = 2; // 无
}
