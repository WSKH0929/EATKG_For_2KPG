package com.wskh.components;

import com.wskh.classes.Item;
import com.wskh.classes.Parameter;
import com.wskh.classes.PlaceItem;
import com.wskh.utils.TimeUtil;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class DP_Heu_Solver {

    // 组合类型
    short V_Split;
    short H_Split;
    short None;
    double alpha;

    private void getPlaceItemListByBlock(int x, int y, Block block, List<PlaceItem> placeItemList) {
        if (block.splitType == V_Split) {
            getPlaceItemListByBlock(x, y, block.childBlockA, placeItemList);
            getPlaceItemListByBlock(x + block.splitPos, y, block.childBlockB, placeItemList);
        } else if (block.splitType == H_Split) {
            getPlaceItemListByBlock(x, y, block.childBlockA, placeItemList);
            getPlaceItemListByBlock(x, y + block.splitPos, block.childBlockB, placeItemList);
        } else {
            Item item = block.packedItem;
            placeItemList.add(new PlaceItem(item.id, item.index, x, y, item.w, item.h, item.s));
        }
    }

    @AllArgsConstructor
    static class Block {
        // 组合类型
        short splitType;
        // 分割线位置
        int splitPos;
        // 当前节点及其儿子的总价值
        int totalValue;
        // 当前节点放进去的物品
        Item packedItem;
        // 打包了的物品的指示数组
        short[] packedArr;
        // 左右（下上）儿子
        Block childBlockA;
        Block childBlockB;
    }

    private Block combineBlock(short splitType, int splitPos, Block blockA, Block blockB, Block combineBlock) {
        int totalV = blockA.totalValue + blockB.totalValue;
        if (combineBlock == null || totalV > combineBlock.totalValue) {
            short[] newPackedArr = blockA.packedArr.clone();
            for (int i = 0; i < blockB.packedArr.length; i++) {
                newPackedArr[i] += blockB.packedArr[i];
                if (newPackedArr[i] > items[i].num) return combineBlock;
            }
            return new Block(splitType, splitPos, totalV, null, newPackedArr, blockA, blockB);
        }
        return combineBlock;
    }

    private void dp(int subW, int subH, int subWIndex, int subHIndex, Block[][] memoryState) {
        // ---------------------------- 剪支判断 ----------------------------
        // 当前空间可放入物品的最大总价值
        int curUbV = dpV[dpX[subW] * dpY[subH]];
        // 除去当前空间，剩余空间可放入物品的最大总价值（两个最大加起来都不如LB，那就剪支）
        int remainH = H - subH;
        int remainW = W - subW;
        int remainS = S - subW * subH - subW * (remainH - dpY[remainH]) - subH * (remainW - dpX[remainW]);
        int curRemainUbV = Math.min(dpV[remainS], u[subW][subH]);
        if (curUbV + curRemainUbV < LB) return;

        // 当前空间至少要达到的价值（curLbV + curRemainUbV > LB）
        double curLbV = LB - curRemainUbV + 1;
        if (curUbV < curLbV) return;

        // ---------------------------- 对当前空间进行状态枚举 ----------------------------
        Block curBlock = null;

        // 找到能加入当前空间的最大价值的物品
        int bestK = -1;
        int bestV = -1;
        for (int k = 0; k < items.length; k++) {
            Item item = items[k];
            if (bestV < item.value && item.w <= subW && item.h <= subH) {
                bestV = item.value;
                bestK = k;
            }
        }
        if (bestK != -1) {
            Item item = items[bestK];
            short[] packedArr = new short[m];
            packedArr[bestK] = 1;
            curBlock = new Block(None, -1, item.value, item, packedArr, null, null);
        }

        if (curBlock == null) return;

        // 直接从上一个阶段拿状态
        if (memoryState[subWIndex - 1][subHIndex] != null) {
            if (curBlock.totalValue < memoryState[subWIndex - 1][subHIndex].totalValue) {
                curBlock = memoryState[subWIndex - 1][subHIndex];
            }
        }
        if (memoryState[subWIndex][subHIndex - 1] != null) {
            if (curBlock.totalValue < memoryState[subWIndex][subHIndex - 1].totalValue) {
                curBlock = memoryState[subWIndex][subHIndex - 1];
            }
        }

        // 垂直切割（横向组合）
        int halfSubW = subW / 2;
        for (int p = 1; p < xList.size(); p++) {
            int splitX = xList.get(p);
            if (splitX > halfSubW) break;
            int q = maxIndexX[subW - splitX];
            Block leftBlock = memoryState[p][subHIndex];
            Block rightBlock = memoryState[q][subHIndex];
            if (leftBlock != null && rightBlock != null) {
                curBlock = combineBlock(V_Split, splitX, leftBlock, rightBlock, curBlock);
            } else if (leftBlock != null) {
                if (curBlock == null || leftBlock.totalValue > curBlock.totalValue) {
                    curBlock = leftBlock;
                }
            } else if (rightBlock != null) {
                if (curBlock == null || rightBlock.totalValue > curBlock.totalValue) {
                    curBlock = rightBlock;
                }
            }
        }
        // 水平切割（竖向组合）
        int halfSubH = subH / 2;
        for (int p = 1; p < yList.size(); p++) {
            int splitY = yList.get(p);
            if (splitY > halfSubH) break;
            int q = maxIndexY[subH - splitY];
            Block leftBlock = memoryState[subWIndex][p];
            Block rightBlock = memoryState[subWIndex][q];
            if (leftBlock != null && rightBlock != null) {
                curBlock = combineBlock(H_Split, splitY, leftBlock, rightBlock, curBlock);
            } else if (leftBlock != null) {
                if (curBlock == null || leftBlock.totalValue > curBlock.totalValue) {
                    curBlock = leftBlock;
                }
            } else if (rightBlock != null) {
                if (curBlock == null || rightBlock.totalValue > curBlock.totalValue) {
                    curBlock = rightBlock;
                }
            }
        }

        // 状态转移
        if (curBlock != null) {
            if (curBlock.totalValue == UB) {
                List<PlaceItem> placeItemList = new ArrayList<>(n);
                getPlaceItemListByBlock(0, 0, curBlock, placeItemList);
                bestPlaceItemList = placeItemList;
                LB = curBlock.totalValue;
                return;
            }
            memoryState[subWIndex][subHIndex] = curBlock;
        }
    }

    private void dpPack() {
        Block[][] memoryState = new Block[xList.size()][yList.size()];

        for (int subWIndex = 1; subWIndex < xList.size(); subWIndex++) {
            int subW = xList.get(subWIndex);
            for (int subHIndex = 1; subHIndex < yList.size(); subHIndex++) {
                int subH = yList.get(subHIndex);
                dp(subW, subH, subWIndex, subHIndex, memoryState);
                if (LB == UB) return;
                if (TimeUtil.isTimeLimit()) return;
            }
        }

        Block bestBlock = memoryState[xList.size() - 1][yList.size() - 1];
        if (bestBlock != null && LB < bestBlock.totalValue) {
            List<PlaceItem> placeItemList = new ArrayList<>(n);
            getPlaceItemListByBlock(0, 0, bestBlock, placeItemList);
            bestPlaceItemList = placeItemList;
            LB = bestBlock.totalValue;
        }
    }

    public int m, n, W, H, S;
    public int UB, LB;
    public List<PlaceItem> bestPlaceItemList;
    public int[] dpV, dpX, dpY;
    public int[][] u;
    public int[] maxIndexX, maxIndexY;
    public Item[] items;
    public List<Integer> xList = new ArrayList<>();
    public List<Integer> yList = new ArrayList<>();

    public void solve(int m, int n, int W, int H, int S, Item[] items, int initUB, int initLB, List<PlaceItem> initPlaceItemList,
                      int[] dpV, int[] dpX, int[] dpY, int[][] u) {
        this.m = m;
        this.n = n;
        this.W = W;
        this.H = H;
        this.S = S;
        this.dpV = dpV;
        this.dpX = dpX;
        this.dpY = dpY;
        this.u = u;
        this.items = items;
        this.UB = initUB;
        this.LB = initLB;
        this.bestPlaceItemList = initPlaceItemList;
        this.V_Split = Parameter.V_Split;
        this.H_Split = Parameter.H_Split;
        this.None = Parameter.None;
        this.alpha = Parameter.Alpha;

        int[] ws = new int[n];
        int[] hs = new int[n];
        int a = 0;
        for (Item item : items) {
            for (int j = 0; j < item.num; j++) {
                ws[a] = item.w;
                hs[a++] = item.h;
            }
        }

        xList = new ArrayList<>();
        PointSetSolver.MIM(ws, W, xList);
        yList = new ArrayList<>();
        PointSetSolver.MIM(hs, H, yList);

        if (xList.get(xList.size() - 1) != W) xList.add(W);
        if (yList.get(yList.size() - 1) != H) yList.add(H);

        maxIndexX = new int[W + 1];
        int i = W;
        for (int j = xList.size() - 1; j >= 0; j--) {
            int x = xList.get(j);
            for (; i >= x; i--) maxIndexX[i] = j;
        }
        maxIndexY = new int[H + 1];
        i = H;
        for (int j = yList.size() - 1; j >= 0; j--) {
            int y = yList.get(j);
            for (; i >= y; i--) maxIndexY[i] = j;
        }

        // 开始动态规划
        dpPack();
    }

}