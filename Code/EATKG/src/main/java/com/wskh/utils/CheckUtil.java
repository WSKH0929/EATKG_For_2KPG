package com.wskh.utils;

import com.wskh.classes.Item;
import com.wskh.classes.Parameter;
import com.wskh.classes.PlaceItem;

import java.util.List;

public class CheckUtil {

    public static void checkGuillotineCut(int W, int H, List<PlaceItem> placeItemList) {
        // 不是一刀切，报错
        if (!CommonUtil.isGuillotineCut(W, H, placeItemList)) {
            WriteUtil.writePatternPlotToPng(W, H, placeItemList, "./Non-Guillotine.png");
            throw new RuntimeException("不是一刀切");
        }
    }

    public static void checkOverlapAndOutBin(int W, int H, List<PlaceItem> placeItemList) {
        if (Parameter.CheckEnable) {
            int S = 0;
            for (PlaceItem placeItem1 : placeItemList) {
                S += placeItem1.s;
                if (S > W * H) {
                    throw new RuntimeException("Exceeding capacity: " + S + " > " + (W * H));
                }
                if (placeItem1.x + placeItem1.w > W || placeItem1.y + placeItem1.h > H) {
                    WriteUtil.writePatternPlotToPng(W, H, placeItemList, "./Beyond_Boundaries.png");
                    System.out.println(CommonUtil.getDataStr(W, H, placeItemList));
                    throw new RuntimeException("Beyond boundaries");
                }
                for (PlaceItem placeItem2 : placeItemList) {
                    if (placeItem2.id != placeItem1.id) {
                        if (CommonUtil.isOverlap(placeItem1, placeItem2)) {
                            WriteUtil.writePatternPlotToPng(W, H, placeItemList, "./Item_Overlap.png");
                            System.out.println(CommonUtil.getDataStr(W, H, placeItemList));
                            throw new RuntimeException("Item overlap");
                        }
                    }
                }
            }
        }
    }

    public static void checkExceedTypeNum(Item[] items, List<PlaceItem> placeItemList) {
        short[] usedCntArr = new short[items.length];
        for (PlaceItem placeItem : placeItemList) {
            int i = placeItem.index;
            usedCntArr[i]++;
            if (usedCntArr[i] > items[i].num) {
                System.err.println(items[i]);
                throw new RuntimeException("Exceeding type num: " + usedCntArr[i] + " > " + items[i].num);
            }
        }
    }

}