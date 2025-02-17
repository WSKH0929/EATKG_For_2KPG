package com.wskh.components;

import com.wskh.classes.Item;
import com.wskh.classes.Parameter;
import com.wskh.classes.PlaceItem;
import com.wskh.classes.Space;

import java.util.ArrayList;
import java.util.List;

public class FastGreedySolver {
    private void greedyComplete(int v, short[] usedCntArr, List<Space> spaces, List<PlaceItem> placeItemList) {
        while (!spaces.isEmpty() && placeItemList.size() < n) {
            // 选出最佳空间和最佳物品
            short maxScore = -1;
            int bestItemIndex = -1;
            int bestSpaceIndex = -1;
            for (int s = 0; s < spaces.size() && maxScore < 2; s++) {
                Space space = spaces.get(s);
                for (int i = 0; i < m; i++) {
                    if (usedCntArr[i] < items[i].num) {
                        Item item = items[i];
                        if (item.w > space.w || item.h > space.h) continue;
                        short score = 0;
                        if (space.w == item.w) score++;
                        if (space.h == item.h) score++;
                        if (score > maxScore) {
                            bestItemIndex = i;
                            bestSpaceIndex = s;
                            maxScore = score;
                            if (maxScore == 2) break;
                        }
                    }
                }
            }

            if (bestSpaceIndex == -1) break;

            Space space = spaces.remove(bestSpaceIndex);
            Item bestInsertItem = items[bestItemIndex];
            usedCntArr[bestItemIndex]++;
            v += bestInsertItem.value;
            SpaceHeuSolver.createNewSpaceByAlpha(spaces, space, alpha, bestInsertItem.w, bestInsertItem.h);
            placeItemList.add(new PlaceItem(bestInsertItem.id, bestInsertItem.index, space.x, space.y, bestInsertItem.w, bestInsertItem.h, bestInsertItem.s));
        }
        if (v > LB) {
            LB = v;
            bestPlaceItemList = placeItemList;
        }
    }

    public int m, n, W, H, S, LB;
    public double alpha;
    public Item[] items;
    public List<PlaceItem> bestPlaceItemList;
    public long timer;

    public List<PlaceItem> solve(int m, int n, int W, int H, int S, Item[] items) {
        this.m = m;
        this.n = n;
        this.W = W;
        this.H = H;
        this.S = S;
        this.items = items;
        this.alpha = Parameter.Alpha;
        this.bestPlaceItemList = new ArrayList<>();

        timer = System.currentTimeMillis();

        for (int i = 0; i < m; i++) {
            short[] usedCntArr = new short[m];
            usedCntArr[i] = 1;
            Item item = items[i];
            // 把item放置在左下角，然后切割空间
            int newW = W - item.w;
            int newH = H - item.h;
            List<PlaceItem> placeItemList = new ArrayList<>(n);
            placeItemList.add(new PlaceItem(item.id, item.index, 0, 0, item.w, item.h, item.s));
            // 水平切割
            List<Space> spaces = new ArrayList<>();
            if (newH > 0) spaces.add(new Space(0, item.h, item.w, newH));
            if (newW > 0) spaces.add(new Space(item.w, 0, newW, H));
            greedyComplete(item.value, usedCntArr.clone(), spaces, new ArrayList<>(placeItemList));
            // 垂直切割
            spaces = new ArrayList<>();
            if (newH > 0) spaces.add(new Space(0, item.h, W, newH));
            if (newW > 0) spaces.add(new Space(item.w, 0, newW, item.h));
            greedyComplete(item.value, usedCntArr, spaces, placeItemList);
        }

        timer = System.currentTimeMillis() - timer;

        return bestPlaceItemList;
    }
}