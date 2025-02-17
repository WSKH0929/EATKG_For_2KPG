package com.wskh.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PointSetSolver {

    public static List<Integer> normalPatterns(int[] ws, int W, int excludeIndex) {
        if (W < 0) return new ArrayList<>();
        boolean[] t = new boolean[W + 1];
        t[0] = true;
        for (int i = 0; i < ws.length; i++) {
            if (i != excludeIndex) {
                int wi = ws[i];
                for (int p = W - wi; p >= 0; p--) {
                    if (t[p]) {
                        t[p + wi] = true;
                    }
                }
            }
        }
        List<Integer> list = new ArrayList<>(t.length);
        for (int i = 0; i < t.length; i++) if (t[i]) list.add(i);
        return list;
    }

    public static List<Integer> normalPatterns(int[] ws, int W) {
        if (W < 0) return new ArrayList<>();
        int tLen = W + 1;
        boolean[] t = new boolean[tLen];
        t[0] = true;
        for (int wi : ws) for (int p = W - wi; p >= 0; p--) if (t[p]) t[p + wi] = true;
        List<Integer> list = new ArrayList<>(tLen);
        for (int i = 0; i < tLen; i++) if (t[i]) list.add(i);
        return list;
    }

    public static List<Integer>[] MIM(int[] ws, int W, List<Integer> xList) {
        int n = ws.length;
        // 计算 tMin
        int[] tLeft = new int[W + 1];
        int[] tRight = new int[W + 1];
        List<Integer>[] normalPatternLists = new List[n];
        for (int i = 0; i < n; i++) {
            normalPatternLists[i] = normalPatterns(ws, W - ws[i], i);
            for (Integer p : normalPatternLists[i]) {
                tLeft[p]++;
                tRight[W - ws[i] - p]++;
            }
        }
        for (int p = 1; p <= W; p++) {
            tLeft[p] += tLeft[p - 1];
            tRight[W - p] += tRight[W - (p - 1)];
        }
        int tMin = 1;
        int min = tLeft[0] + tRight[1];
        for (int p = 2; p <= W; p++) {
            int sum = tLeft[p - 1] + tRight[p];
            if (sum < min) {
                min = sum;
                tMin = p;
            }
        }

        // 根据算好的 tMin，进行后续计算，获取初步的xListList
        boolean[][] leftSelectedArray = new boolean[n][W + 1];
        boolean[][] rightSelectedArray = new boolean[n][W + 1];
        List<Integer>[] xListList = new List[n];
        for (int i = 0; i < n; i++) {
            List<Integer> list = new ArrayList<>();
            for (int p : normalPatternLists[i]) {
                if (p < tMin) {
                    if (!leftSelectedArray[i][p] && !rightSelectedArray[i][p]) list.add(p);
                    leftSelectedArray[i][p] = true;
                }
                int rightPoint = W - ws[i] - p;
                if (rightPoint >= tMin) {
                    if (!leftSelectedArray[i][rightPoint] && !rightSelectedArray[i][rightPoint]) list.add(rightPoint);
                    rightSelectedArray[i][rightPoint] = true;
                }
            }
            Collections.sort(list);
            xListList[i] = list;
        }

        // 获取所有List的并集
        boolean[] allMimSelectedArray = new boolean[W + 1];
        for (List<Integer> list : xListList) {
            for (int p : list) {
                if (!allMimSelectedArray[p]) {
                    allMimSelectedArray[p] = true;
                    xList.add(p);
                }
            }
        }
        Collections.sort(xList);

        return xListList;
    }

}