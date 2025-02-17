package com.wskh.utils;

import java.util.List;

public class DffUtil {

    public static int solve1dCkp(int C, int[] xs) {
        // 要确保 xs 升序排序
        int res = 0;
        for (int x : xs) {
            C -= x;
            if (C >= 0) {
                res++;
            } else {
                break;
            }
        }
        return res;
    }

    public static int dff0(int k, int C, int x) {
        double r = (k + 1) * x / (double) C;
        if (CommonUtil.isInteger(r)) {
            return x;
        } else {
            return (int) r * C / k;
        }
    }

    public static int dff1(int k, int C, int x) {
        int r = C - k;
        if (x > r) {
            return C;
        } else if (x >= k) {
            return x;
        } else {
            return 0;
        }
    }

    public static int dff2(int k, int C, int x, int[] xs) {
        int r = C / 2;
        if (x > r) {
            return solve1dCkp(C, xs) - solve1dCkp(C - x, xs);
        } else if (x >= k) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int dff3(int k, int C, int x) {
        int r = C / 2;
        if (x > r) {
            return 2 * (C / k - (C - x) / k);
        } else if (x == r) {
            return C / k;
        } else {
            return 2 * (x / k);
        }
    }

}