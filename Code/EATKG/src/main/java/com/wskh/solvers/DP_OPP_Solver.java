package com.wskh.solvers;

import com.wskh.classes.Item;
import com.wskh.classes.PlaceItem;
import com.wskh.classes.TerminationException;
import com.wskh.components.FastGreedySolver;
import com.wskh.components.PointSetSolver;
import com.wskh.utils.CommonUtil;
import com.wskh.utils.DffUtil;
import com.wskh.utils.TimeUtil;
import lombok.AllArgsConstructor;

import java.util.*;

public class DP_OPP_Solver {

    private int f(int dffType, int k, int C, int x, int[] xs) {
        return switch (dffType) {
            case 1 -> DffUtil.dff0(k, C, x);
            case 2 -> DffUtil.dff1(k, C, x);
            case 3 -> DffUtil.dff3(k, C, x);
            case 4 -> DffUtil.dff2(k, C, x, xs);
            default -> throw new RuntimeException();
        };
    }

    public boolean fast_Bound_OPP() {
        // 冲突检测
        int totalS = 0;
        for (int i = 0; i < m; i++) {
            Item itemI = items[i];
            if (itemI.h > H) return false;
            totalS += (itemI.s * itemI.num);
            for (int j = i + 1; j < m; j++) {
                if (i != j) {
                    Item itemJ = items[j];
                    if (itemI.w + itemJ.w > W && itemI.h + itemJ.h > H) {
                        return false;
                    }
                }
            }
        }

        // 计算下界
        // LB0
        int sppH_LB = CommonUtil.ceilToInt((double) totalS / W);
        if (sppH_LB > H) return false;

        // LB1
        n = 0;
        for (Item item : items) n += item.num;
        Item[] copyItems = new Item[n];
        int a = 0;
        for (Item item : items) for (int i = 0; i < item.num; i++) copyItems[a++] = item;
        Arrays.sort(copyItems, (o1, o2) -> -Integer.compare(o1.h, o2.h));
        int[] totalW_Arr = new int[n];
        totalW_Arr[0] = copyItems[0].w;
        for (int i = 1; i < n; i++) {
            totalW_Arr[i] = copyItems[i].w + totalW_Arr[i - 1];
        }
        int k = 0;
        for (; k < n; k++) if (totalW_Arr[k] > W) break;
        k--;
        int l = k + 1;
        for (; l < n; l++) {
            Item itemL = copyItems[l];
            int il = k - 1;
            for (; il >= 0; il--) if (itemL.w + totalW_Arr[il] <= W) break;
            il++;
            int lb = itemL.h + copyItems[il].h;
            if (sppH_LB < lb) {
                if (lb > H) return false;
                sppH_LB = lb;
            }
        }

        // LB2
        int halfW = W / 2;
        for (int alpha = 1; alpha <= halfW; alpha++) {
            int part1 = 0;
            int part2 = 0;
            int part3 = 0;
            for (Item item : copyItems) {
                int w = item.w;
                int h = item.h;
                if (w > W - alpha) {
                    part1 += h;
                } else if (w > halfW) {
                    part1 += h;
                    part3 += ((W - w) * h);
                } else if (w >= alpha) {
                    part2 += item.s;
                }
            }
            int lb = part1 - Math.max(0, CommonUtil.ceilToInt((part2 - part3) / (double) W));
            if (sppH_LB < lb) {
                if (lb > H) return false;
                sppH_LB = lb;
            }
        }

        // LB3
        int[] ws = new int[n];
        for (int i = 0; i < n; i++) ws[i] = copyItems[i].w;
        Arrays.sort(ws);
        boolean[] booleans = new boolean[W + 1];
        booleans[0] = true;
        List<Integer> arr2 = new ArrayList<>();
        for (Item item : copyItems) {
            int w = item.w;
            if (w <= halfW) {
                if (!booleans[w]) {
                    booleans[w] = true;
                    arr2.add(w);
                }
            } else {
                w = W - w;
                if (!booleans[w]) {
                    booleans[w] = true;
                    arr2.add(w);
                }
            }
        }
        for (k = 1; k <= 4; k++) {
            for (int beta : arr2) {
                if (k == 1) {
                    for (int alpha = 1; alpha <= W; alpha++) {
                        int lb = 0;
                        for (Item item : copyItems) {
                            lb += (f(k, alpha, W, f(2, beta, W, item.w, ws), ws) * item.h);
                        }
                        lb = CommonUtil.ceilToInt((double) lb / f(k, alpha, W, f(2, beta, W, W, ws), ws));
                        if (sppH_LB < lb) {
                            if (lb > H) return false;
                            sppH_LB = lb;
                        }
                    }
                } else {
                    for (int alpha : arr2) {
                        int lb = 0;
                        for (Item item : copyItems) {
                            lb += (f(k, alpha, W, f(2, beta, W, item.w, ws), ws) * item.h);
                        }
                        lb = CommonUtil.ceilToInt((double) lb / f(k, alpha, W, f(2, beta, W, W, ws), ws));
                        if (sppH_LB < lb) {
                            if (lb > H) {
                                return false;
                            }
                            sppH_LB = lb;
                        }
                    }
                }
            }
        }

        // 启发式
        FastGreedySolver fastGreedySolver = new FastGreedySolver();
        fastGreedySolver.solve(m, n, W, H, S, items);
        List<PlaceItem> placeItemList = fastGreedySolver.bestPlaceItemList;
        if (placeItemList.size() == n) {
            feasiblePlacedItemList = placeItemList;
        }

        return true;
    }

    // 组合类型
    final short V_Split = 0; // 左右
    final short H_Split = 1; // 上下
    final short None = 2; // 无

    @AllArgsConstructor
    static class PartSolution {
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
        PartSolution childPartSolutionA;
        PartSolution childPartSolutionB;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PartSolution that = (PartSolution) o;
            return Arrays.equals(packedArr, that.packedArr);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(packedArr);
        }
    }

    class PartSolutionSet {
        Map<Integer, Set<PartSolution>> partSolutionMap = new HashMap<>();

        void packOneItem(int k, Item item) {
            short[] packedArr = new short[m];
            packedArr[k] = 1;
            addPartSolution(new PartSolution(None, -1, item.s, item, packedArr, null, null));
        }

        public void addPartSolution(PartSolution partSolution) {
            int key = partSolution.totalValue;
            Set<PartSolution> partSolutions = partSolutionMap.get(key);
            if (partSolutions != null) {
                partSolutions.add(partSolution);
            } else {
                partSolutions = new HashSet<>();
                partSolutions.add(partSolution);
                partSolutionMap.put(key, partSolutions);
            }
        }

        public void addPartSolutionSet(PartSolutionSet partSolutionSet, double curLbV) {
            if (partSolutionSet != null) {
                for (Map.Entry<Integer, Set<PartSolution>> entry : partSolutionSet.partSolutionMap.entrySet()) {
                    Integer key = entry.getKey();
                    if (key >= curLbV) {
                        Set<PartSolution> value = entry.getValue();
                        for (PartSolution partSolution : value) {
                            addPartSolution(partSolution);
                        }
                    }
                }
            }
        }
    }

    private void getPlaceItemListByPartSolution(int x, int y, PartSolution curPartSolution, List<PlaceItem> placeItemList) {
        if (curPartSolution.splitType == V_Split) {
            getPlaceItemListByPartSolution(x, y, curPartSolution.childPartSolutionA, placeItemList);
            getPlaceItemListByPartSolution(x + curPartSolution.splitPos, y, curPartSolution.childPartSolutionB, placeItemList);
        } else if (curPartSolution.splitType == H_Split) {
            getPlaceItemListByPartSolution(x, y, curPartSolution.childPartSolutionA, placeItemList);
            getPlaceItemListByPartSolution(x, y + curPartSolution.splitPos, curPartSolution.childPartSolutionB, placeItemList);
        } else {
            Item item = curPartSolution.packedItem;
            placeItemList.add(new PlaceItem(item.id, item.index, x, y, item.w, item.h, item.s));
        }
    }

    private PartSolution combinePartSolution(short splitType, int splitPos, int totalS, PartSolution partSolutionA, PartSolution partSolutionB) {
        short[] newPackedArr = partSolutionA.packedArr.clone();
        for (int i = 0; i < partSolutionB.packedArr.length; i++) {
            newPackedArr[i] += partSolutionB.packedArr[i];
            if (newPackedArr[i] > items[i].num) return null;
        }
        return new PartSolution(splitType, splitPos, totalS, null, newPackedArr, partSolutionA, partSolutionB);
    }

    private void combinePartSolutionSet(PartSolutionSet curPartSolutionSet, PartSolutionSet leftPartSolutionSet, PartSolutionSet rightPartSolutionSet, short splitType, int splitPos, double curLbV) {
        for (Map.Entry<Integer, Set<PartSolution>> entryA : leftPartSolutionSet.partSolutionMap.entrySet()) {
            int keyA = entryA.getKey();
            Set<PartSolution> valueA = entryA.getValue();
            for (Map.Entry<Integer, Set<PartSolution>> entryB : rightPartSolutionSet.partSolutionMap.entrySet()) {
                int keyB = entryB.getKey();
                int totalS = keyA + keyB;
                if (totalS >= curLbV) {
                    Set<PartSolution> valueB = entryB.getValue();
                    // 合并A和B
                    for (PartSolution partSolutionA : valueA) {
                        for (PartSolution partSolutionB : valueB) {
                            PartSolution combinePartSolution = combinePartSolution(splitType, splitPos, totalS, partSolutionA, partSolutionB);
                            if (combinePartSolution != null) {
                                curPartSolutionSet.addPartSolution(combinePartSolution);
                            }
                        }
                    }
                }
            }
        }
    }

    private void dp(int subW, int subH, int subWIndex, int subHIndex, PartSolutionSet[][] memoryState) {
        // ---------------------------- 剪支判断 ----------------------------
        // 当前空间可放入物品的最大总价值
        double curUbV = dpV[dpX[subW] * dpY[subH]];
        // 除去当前空间，剩余空间可放入物品的最大总价值（两个最大加起来都不如UB，那就剪支）
        double curRemainUbV = dpV[S - subW * subH];
        if (curUbV + curRemainUbV < targetValue) return;

        // 当前空间至少要达到的价值
        double curLbV = targetValue - curRemainUbV;
        if (curUbV < curLbV) return;

        // ---------------------------- 对当前空间进行状态枚举 ----------------------------
        PartSolutionSet curPartSolutionSet = new PartSolutionSet();
        int halfSubW = subW / 2;
        int halfSubH = subH / 2;

        // 找到能装入当前空间的物品并加入
        for (int k = 0; k < items.length; k++) {
            Item item = items[k];
            if (item.s >= curLbV && item.w <= subW && item.h <= subH) {
                curPartSolutionSet.packOneItem(k, item);
            }
        }

        // 垂直切割（横向组合）
        for (int p = 1; p < xList.size(); p++) {
            int splitX = xList.get(p);
            if (splitX > halfSubW) break;
            int q = maxIndexX[subW - splitX];
            PartSolutionSet leftPartSolutionSet = memoryState[p][subHIndex];
            PartSolutionSet rightPartSolutionSet = memoryState[q][subHIndex];
            if (leftPartSolutionSet != null && rightPartSolutionSet != null) {
                combinePartSolutionSet(curPartSolutionSet, leftPartSolutionSet, rightPartSolutionSet, V_Split, splitX, curLbV);
            } else if (leftPartSolutionSet != null) {
                curPartSolutionSet.addPartSolutionSet(leftPartSolutionSet, curLbV);
            } else if (rightPartSolutionSet != null) {
                curPartSolutionSet.addPartSolutionSet(rightPartSolutionSet, curLbV);
            }
        }
        // 水平切割（竖向组合）
        for (int p = 1; p < yList.size(); p++) {
            int splitY = yList.get(p);
            if (splitY > halfSubH) break;
            int q = maxIndexY[subH - splitY];
            PartSolutionSet leftPartSolutionSet = memoryState[subWIndex][p];
            PartSolutionSet rightPartSolutionSet = memoryState[subWIndex][q];
            if (leftPartSolutionSet != null && rightPartSolutionSet != null) {
                combinePartSolutionSet(curPartSolutionSet, leftPartSolutionSet, rightPartSolutionSet, H_Split, splitY, curLbV);
            } else if (leftPartSolutionSet != null) {
                curPartSolutionSet.addPartSolutionSet(leftPartSolutionSet, curLbV);
            } else if (rightPartSolutionSet != null) {
                curPartSolutionSet.addPartSolutionSet(rightPartSolutionSet, curLbV);
            }
        }

        // 直接从上一个阶段拿状态
        curPartSolutionSet.addPartSolutionSet(memoryState[subWIndex - 1][subHIndex], curLbV);
        curPartSolutionSet.addPartSolutionSet(memoryState[subWIndex][subHIndex - 1], curLbV);

        // 状态转移
        if (!curPartSolutionSet.partSolutionMap.isEmpty()) {
            memoryState[subWIndex][subHIndex] = curPartSolutionSet;
        }
    }

    public int[] dpV, dpX, dpY;
    public int[] maxIndexX, maxIndexY;
    public List<Integer> xList = new ArrayList<>();
    public List<Integer> yList = new ArrayList<>();

    private void Exact_OPP() {
        dpV = new int[S + 1];
        dpX = new int[W + 1];
        dpY = new int[H + 1];
        for (Item item : items) {
            int w = item.w;
            int h = item.h;
            int s = item.s;
            for (int i = 0; i < item.num; i++) {
                for (int j = S; j >= s; j--) dpV[j] = Math.max(dpV[j], dpV[j - s] + s);
                for (int j = W; j >= w; j--) dpX[j] = Math.max(dpX[j], dpX[j - w] + w);
                for (int j = H; j >= h; j--) dpY[j] = Math.max(dpY[j], dpY[j - h] + h);
            }
        }

        int[] ws = new int[n];
        int[] hs = new int[n];
        int a = 0;
        for (Item item : items) {
            for (int j = 0; j < item.num; j++) {
                ws[a] = item.w;
                hs[a++] = item.h;
            }
        }
        xList = PointSetSolver.normalPatterns(ws, W);
        yList = PointSetSolver.normalPatterns(hs, H);
        if (xList.getLast() != W) xList.add(W);
        if (yList.getLast() != H) yList.add(H);

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

        // DP 过程
        targetValue = 0;
        for (Item item : items) targetValue += (item.num * item.s);
        PartSolutionSet[][] memoryState = new PartSolutionSet[xList.size()][yList.size()];

        for (int subWIndex = 1; subWIndex < xList.size(); subWIndex++) {
            int subW = xList.get(subWIndex);
            for (int subHIndex = 1; subHIndex < yList.size(); subHIndex++) {
                int subH = yList.get(subHIndex);
                dp(subW, subH, subWIndex, subHIndex, memoryState);
                if (TimeUtil.isTimeLimit()) throw new TerminationException();
            }
        }

        PartSolutionSet partSolutionSet = memoryState[xList.size() - 1][yList.size() - 1];
        if (partSolutionSet != null) {
            int maxTotalValue = 0;
            Set<PartSolution> bestPartSolutions = null;
            for (Map.Entry<Integer, Set<PartSolution>> entry : partSolutionSet.partSolutionMap.entrySet()) {
                int key = entry.getKey();
                if (bestPartSolutions == null || key > maxTotalValue) {
                    maxTotalValue = key;
                    bestPartSolutions = entry.getValue();
                }
            }
            if (maxTotalValue == targetValue) {
                for (PartSolution bestPartSolution : bestPartSolutions) {
                    // 找到最优解了
                    feasiblePlacedItemList = new ArrayList<>(n);
                    getPlaceItemListByPartSolution(0, 0, bestPartSolution, feasiblePlacedItemList);
                }
            }
        }
    }

    public int W, H, S, m, n;
    int targetValue;
    public Item[] items;
    List<PlaceItem> feasiblePlacedItemList;

    public List<PlaceItem> solve(int initW, int initH, int initS, Item[] initItems) {

        this.W = initW;
        this.H = initH;
        this.S = initS;
        this.m = initItems.length;
        this.items = initItems;

        // 快速界限 OPP
        if (fast_Bound_OPP()) {
            // 精确动态规划枚举
            if (feasiblePlacedItemList == null) {
                try {
                    Exact_OPP();
                } catch (TerminationException e) {

                }
            }
        }

        return feasiblePlacedItemList;
    }

}