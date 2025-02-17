package com.wskh.utils;

import com.wskh.classes.Parameter;
import com.wskh.classes.PlaceItem;

import java.util.ArrayList;
import java.util.List;

public class CommonUtil {

    public static int floorToInt(double x) {
        return (int) Math.floor(x + Parameter.EPS);
    }

    public static int ceilToInt(double x) {
        return (int) Math.ceil(x - Parameter.EPS);
    }

    public static boolean isInteger(double x) {
        // 计算差值并与 EPS 比较
        return Math.abs(x - Math.round(x)) < Parameter.EPS;
    }

    public static int roundToNearestInt(double x) {
        int a = floorToInt(x);
        int b = ceilToInt(x);
        if (x - a < b - x) {
            return a;
        } else {
            return b;
        }
    }

    public static boolean isOverlap(PlaceItem r1, PlaceItem r2) {
        return !(r2.x >= r1.x + r1.w || // r2 在 r1 的右边
                r2.x + r2.w <= r1.x || // r2 在 r1 的左边
                r2.y >= r1.y + r1.h || // r2 在 r1 的上方
                r2.y + r2.h <= r1.y);  // r2 在 r1 的下方
    }

    public static void addPosition(int p, boolean[] positionUsedArr, List<Integer> positions) {
        if (!positionUsedArr[p]) {
            positionUsedArr[p] = true;
            positions.add(p);
        }
    }

    public static String getDataStr(int W, int H, List<PlaceItem> placeItems) {
        StringBuilder str = new StringBuilder("canvasWidth:" + W + ",\ncanvasHeight:" + H + ",\ndata:[");
        for (PlaceItem placeItem : placeItems) {
            str.append("{x:").append(placeItem.x).append(",y:").append(placeItem.y).append(",h:").append(placeItem.h).append(",w:").append(placeItem.w).append("},");
        }
        str.append("],");
        return str.toString();
    }

    public static boolean isGuillotineCut(int W, int H, List<PlaceItem> placeItemList) {
        if (placeItemList.size() <= 3) return true;

        List<Integer> xPositions = new ArrayList<>(W);
        List<Integer> yPositions = new ArrayList<>(H);
        boolean[] xUsedArr = new boolean[W];
        boolean[] yUsedArr = new boolean[H];

        // Collect all unique x and y positions (both start and end)
        for (PlaceItem placeItem : placeItemList) {
            int x1 = placeItem.x;
            int x2 = x1 + placeItem.w;
            int y1 = placeItem.y;
            int y2 = y1 + placeItem.h;
            if (x1 > 0) CommonUtil.addPosition(x1, xUsedArr, xPositions);
            if (x2 < W) CommonUtil.addPosition(x2, xUsedArr, xPositions);
            if (y1 > 0) CommonUtil.addPosition(y1, yUsedArr, yPositions);
            if (y2 < H) CommonUtil.addPosition(y2, yUsedArr, yPositions);
        }

        // Try to find a vertical cut
        for (int x : xPositions) {
            boolean validCut = true;
            List<PlaceItem> leftPlaceItemList = new ArrayList<>(placeItemList.size());
            List<PlaceItem> rightPlaceItemList = new ArrayList<>(placeItemList.size());
            for (PlaceItem placeItem : placeItemList) {
                if (placeItem.x + placeItem.w <= x) {
                    leftPlaceItemList.add(placeItem);
                } else if (placeItem.x >= x) {
                    rightPlaceItemList.add(placeItem);
                } else {
                    validCut = false;
                    break;
                }
            }
            if (validCut && !leftPlaceItemList.isEmpty() && !rightPlaceItemList.isEmpty()) {
                return isGuillotineCut(W, H, leftPlaceItemList) && isGuillotineCut(W, H, rightPlaceItemList);
            }
        }

        // Try to find a horizontal cut
        for (int y : yPositions) {
            boolean validCut = true;
            List<PlaceItem> topPlaceItemList = new ArrayList<>(placeItemList.size());
            List<PlaceItem> bottomPlaceItemList = new ArrayList<>(placeItemList.size());
            for (PlaceItem placeItem : placeItemList) {
                if (placeItem.y + placeItem.h <= y) {
                    bottomPlaceItemList.add(placeItem);
                } else if (placeItem.y >= y) {
                    topPlaceItemList.add(placeItem);
                } else {
                    validCut = false;
                    break;
                }
            }
            if (validCut && !topPlaceItemList.isEmpty() && !bottomPlaceItemList.isEmpty()) {
                return isGuillotineCut(W, H, topPlaceItemList) &&
                        isGuillotineCut(W, H, bottomPlaceItemList);
            }
        }

        // 不是一刀切
        return false;
    }

}