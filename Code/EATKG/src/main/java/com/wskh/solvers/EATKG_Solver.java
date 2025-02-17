package com.wskh.solvers;

import com.wskh.classes.Item;
import com.wskh.classes.PlaceItem;
import com.wskh.classes.TerminationException;
import com.wskh.components.DP_Heu_Solver;
import com.wskh.components.FastGreedySolver;
import com.wskh.components.PointSetSolver;
import com.wskh.utils.TimeUtil;

import java.util.*;

public class EATKG_Solver {
    public int m, n, W, H, S;
    public Item[] items;

    public EATKG_Solver(int m, int n, int W, int H, int S, Item[] items) {
        this.m = m;
        this.n = n;
        this.W = W;
        this.H = H;
        this.S = S;
        this.items = items;
    }

    private boolean preprocess1() {
        List<Item> itemList = new ArrayList<>(Arrays.asList(items));
        // 支配关系
        HashSet<Integer>[] dominateArr2 = new HashSet[n]; // j=dominateArr2[i]: 表示 i 被 j dominate 的数组 (索引是index)
        for (int i = 0; i < dominateArr2.length; i++) dominateArr2[i] = new HashSet<>(m);
        for (int i = 0; i < itemList.size(); i++) {
            Item itemI = itemList.get(i);
            for (int j = i + 1; j < itemList.size(); j++) {
                Item itemJ = itemList.get(j);
                if (itemI.value >= itemJ.value && itemI.w <= itemJ.w && itemI.h <= itemJ.h) {
                    // itemI dominate itemJ
                    dominateArr2[j].add(i);
                } else if (itemJ.value >= itemI.value && itemJ.w <= itemI.w && itemJ.h <= itemI.h) {
                    // itemJ dominate itemI
                    dominateArr2[i].add(j);
                }
            }
        }
        boolean changed = false;

        // 开始遍历
        for (int t = 0; t < itemList.size(); t++) {
            HashSet<Integer> F = dominateArr2[t];
            Item item = itemList.get(t);

            // (a)
            int totalAreaInF = 0;
            for (int i : F) {
                Item itemF = itemList.get(i);
                totalAreaInF += (itemF.s * itemF.num);
            }
            if (item.s * item.num + totalAreaInF > S) {
                int newD = Math.max(0, (S - totalAreaInF) / item.s);
                if (newD < item.num) {
                    changed = true;
                    item.num = newD;
                    if (newD == 0) continue;
                }
            }

            // (b)
            int totalValueInF = 0;
            for (int i : F) {
                Item itemF = itemList.get(i);
                totalValueInF += (itemF.value * itemF.num);
            }
            if (item.value * item.num + totalValueInF > UB) {
                int newD = Math.max(0, (UB - totalValueInF) / item.value);
                if (newD < item.num) {
                    changed = true;
                    item.num = newD;
                    if (newD == 0) continue;
                }
            }

            // (c)
            int S_bar = S - totalAreaInF - item.s;
            int ub_bar = dpV[S_bar];
            if (item.value + totalValueInF + ub_bar < LB) {
                changed = true;
                item.num = 0;
                continue;
            }

            // (d)
            List<Item> itemFList = new ArrayList<>(F.size());
            for (Integer i : F) itemFList.add(items[i]);
            // 计算nx
            itemFList.sort(Comparator.comparingInt(o -> o.w));
            int nx = 0;
            int remainW = W;
            for (Item itemF : itemFList) {
                int newRemainW = remainW - itemF.w * itemF.num;
                if (newRemainW > 0) {
                    remainW = newRemainW;
                    nx += itemF.num;
                } else if (newRemainW < 0) {
                    nx += (remainW / itemF.w);
                } else {
                    break;
                }
            }
            // 计算ny
            itemFList.sort(Comparator.comparingInt(o -> o.h));
            int ny = 0;
            int remainH = H;
            for (Item itemF : itemFList) {
                int newRemainH = remainH - itemF.h * itemF.num;
                if (newRemainH > 0) {
                    remainH = newRemainH;
                    ny += itemF.num;
                } else if (newRemainH < 0) {
                    ny += (remainH / itemF.h);
                } else {
                    break;
                }
            }
            //
            int totalDemandF = 0;
            for (Item itemF : itemFList) totalDemandF += itemF.num;
            if (nx * ny < totalDemandF) {
                changed = true;
                item.num = 0;
            }

        }

        m = 0;
        n = 0;
        for (Item item : itemList) {
            if (item.num > 0) {
                m++;
                n += item.num;
            }
        }

        items = new Item[m];
        int i = 0;
        for (Item item : itemList) {
            if (item.num > 0) {
                item.index = i;
                items[i++] = item;
            }
        }

        return changed;
    }

    private boolean preprocess2() {
        int[] dpX = new int[W + 1];
        int[] dpY = new int[H + 1];
        // 遍历物品
        for (Item item : items) {
            int w = item.w;
            int h = item.h;
            int num = item.num;
            for (int i = 0; i < num; i++) {
                // 遍历背包容量
                for (int j = W; j >= w; j--) dpX[j] = Math.max(dpX[j], dpX[j - w] + w);
                for (int j = H; j >= h; j--) dpY[j] = Math.max(dpY[j], dpY[j - h] + h);
            }
        }
        int newS = dpX[W] * dpY[H];
        if (newS < S) {
            S = newS;
            W = dpX[W];
            H = dpY[H];
            return true;
        }
        return false;
    }

    private boolean preprocess3() {
        List<Item> itemList = new ArrayList<>(Arrays.asList(items));
        boolean change = false;

        for (int i = 0; i < itemList.size(); i++) {
            Item itemI = itemList.get(i);
            itemI.num--;
            int reducedW = W - itemI.w;
            int reducedH = H - itemI.h;
            int[] dpX = new int[reducedW + 1];
            int[] dpY = new int[reducedH + 1];
            // dp
            for (Item itemJ : itemList) {
                int w = itemJ.w;
                int h = itemJ.h;
                int num = itemJ.num;
                for (int l = 0; l < num; l++) {
                    // 遍历背包容量
                    for (int k = reducedW; k >= w; k--) dpX[k] = Math.max(dpX[k], dpX[k - w] + w);
                    for (int k = reducedH; k >= h; k--) dpY[k] = Math.max(dpY[k], dpY[k - h] + h);
                }
            }
            // 新尺寸
            int newW = W - dpX[reducedW];
            int newH = H - dpY[reducedH];
            int newS = newW * newH;
            if (newS > itemI.s) {
                // 尺寸变化
                // 寻找有没有和它尺寸、价值都一样的物品
                boolean find = false;
                for (Item item : itemList) {
                    if (item.value == itemI.value && item.w == newW && item.h == newH) {
                        // 如果找到了，则让它的需求加一
                        item.num++;
                        find = true;
                    }
                }
                if (!find) {
                    // 如果没找到，则创建一个新的物品类别，需求为一，加到集合最后
                    Item newItem = new Item(itemI.id, 0, newW, newH, newS, itemI.value, 1);
                    itemList.add(newItem);
                }
                change = true;
            } else {
                // 尺寸没有变化，还原数量
                itemI.num++;
            }
        }

        if (change) {
            m = 0;
            for (Item item : itemList) if (item.num > 0) m++;
            items = new Item[m];
            int i = 0;
            for (Item item : itemList) if (item.num > 0) items[i++] = item;
            Arrays.sort(items, (o1, o2) -> {
                int c = -Double.compare((double) o1.value / o1.s, (double) o2.value / o2.s);
                if (c == 0) c = -Integer.compare(o1.value, o2.value);
                if (c == 0)
                    c = -Double.compare(Math.max((double) o1.w / W, (double) o1.h / H), Math.max((double) o2.w / W, (double) o2.h / H));
                return c;
            });
            for (int j = 0; j < items.length; j++) items[j].index = j;
        }
        return change;
    }

    int[] dpV;
    int[] dpX;
    int[] dpY;

    private void computeFastBound() {
        // 计算下界
        FastGreedySolver fastGreedySolver = new FastGreedySolver();
        fastGreedySolver.solve(m, n, W, H, S, items);
        LB = fastGreedySolver.LB;
        bestPlaceItemList = fastGreedySolver.bestPlaceItemList;

        if (LB == UB) throw new TerminationException();

        // 计算上界
        dpX = new int[W + 1];
        dpY = new int[H + 1];
        for (Item item : items) {
            int w = item.w;
            int h = item.h;
            for (int i = 0; i < item.num; i++) {
                for (int j = W; j >= w; j--) dpX[j] = Math.max(dpX[j], dpX[j - w] + w);
                for (int j = H; j >= h; j--) dpY[j] = Math.max(dpY[j], dpY[j - h] + h);
            }
        }

        dpV = new int[S + 1];

        List<int[]> goods = new ArrayList<>();
        for (Item item : items) {
            int w = item.s;
            int v = item.value;
            int s = item.num;
            // 二进制拆分
            int k = 1;
            while (k <= s) {
                goods.add(new int[]{w * k, v * k});
                s -= k;
                k *= 2;
            }
            if (s > 0) goods.add(new int[]{w * s, v * s});
        }
        for (int[] item : goods) {
            int weight = item[0];
            int value = item[1];
            for (int j = S; j >= weight; j--) dpV[j] = Math.max(dpV[j], dpV[j - weight] + value);
        }

        UB = Math.min(UB, dpV[dpX[W] * dpY[H]]);
        if (UB <= LB) {
            UB = LB;
            throw new TerminationException();
        }
    }

    private void preprocessing() {
        // 把相同尺寸相同价值的物品合并，并且去掉无法装入容器的物品
        Map<String, Item> map = new HashMap<>();
        for (Item item : items) {
            if (item.w <= W && item.h <= H) {
                String key = item.w + "-" + item.h + "-" + item.value;
                Item getItem = map.get(key);
                if (getItem == null) {
                    map.put(key, item);
                } else {
                    getItem.num += item.num;
                }
            }
        }
        m = map.size();
        items = new Item[m];
        int a = 0;
        for (Map.Entry<String, Item> entry : map.entrySet()) items[a++] = entry.getValue();
        Arrays.sort(items, (o1, o2) -> {
            int c = -Double.compare((double) o1.value / o1.s, (double) o2.value / o2.s);
            if (c == 0) c = -Integer.compare(o1.value, o2.value);
            if (c == 0)
                c = -Double.compare(Math.max((double) o1.w / W, (double) o1.h / H), Math.max((double) o2.w / W, (double) o2.h / H));
            return c;
        });
        for (int i = 0; i < items.length; i++) items[i].index = i;

        // 迭代预处理
        computeFastBound();
        preprocess1();
        boolean changed = true;
        while (changed && !TimeUtil.isTimeLimit()) {
            computeFastBound();
            changed = preprocess2();
            changed |= preprocess3();
        }
    }

    public int[][] u;

    private void computeUB0() {
        // 计算无约束二维背包问题上界
        Item[] copyItems = items.clone();
        Arrays.sort(copyItems, (o1, o2) -> -Integer.compare(o1.value, o2.value));
        int maxItemValue = copyItems[0].value;

        int[][] F = new int[W + 1][H + 1];
        int[][] Fv = new int[W + 1][H + 1];
        int[][] Fh = new int[W + 1][H + 1];
        int[][] lambda = new int[W + 1][H + 1];
        int[][] omega = new int[W + 1][H + 1];
        boolean[] A = new boolean[W + 1];
        boolean[] O = new boolean[H + 1];

        for (int y2 = 1; y2 <= H; y2++) {
            for (int x2 = 1; x2 <= W; x2++) {

                int Fx2y2 = Math.max(F[x2 - 1][y2], F[x2][y2 - 1]);
                Fx2y2 = Math.max(Fx2y2, Math.max(Fv[x2][y2], Fh[x2][y2]));
                if (Fx2y2 < maxItemValue) {
                    for (Item item : copyItems) {
                        if (item.w <= x2 && item.h <= y2) {
                            Fx2y2 = Math.max(Fx2y2, item.value);
                            break;
                        }
                    }
                }
                F[x2][y2] = Fx2y2;

                if (Fx2y2 == F[x2 - 1][y2]) {
                    lambda[x2][y2] = 0;
                } else if (Fx2y2 > Fv[x2][y2]) {
                    lambda[x2][y2] = x2;
                }

                if (Fx2y2 == F[x2][y2 - 1]) {
                    omega[x2][y2] = 0;
                } else if (Fx2y2 > Fh[x2][y2]) {
                    omega[x2][y2] = y2;
                }

                if (omega[x2][y2] > 0) O[y2] = true;
                if (lambda[x2][y2] > 0) A[x2] = true;

                if (O[y2]) {
                    int xMax = Math.min(lambda[x2][y2], W - x2);
                    for (int x1 = 1; x1 <= xMax; x1++) {
                        if (lambda[x1][y2] == x1) {
                            int V = F[x1][y2] + Fx2y2;
                            if (V >= Fv[x1 + x2][y2]) {
                                Fv[x1 + x2][y2] = V;
                                lambda[x1 + x2][y2] = x1;
                            }
                        }
                    }
                }

                if (A[x2]) {
                    int yMax = Math.min(omega[x2][y2], H - y2);
                    for (int y1 = 1; y1 <= yMax; y1++) {
                        if (omega[x2][y1] == y1) {
                            int V = F[x2][y1] + Fx2y2;
                            if (V >= Fh[x2][y1 + y2]) {
                                Fh[x2][y1 + y2] = V;
                                omega[x2][y1 + y2] = y1;
                            }
                        }
                    }
                }

            }
        }

        if (F[W][H] < UB) {
            UB = F[W][H];
            if (LB == UB) return;
        }

        // u
        u = new int[W + 1][H + 1];

        int[] ws = new int[n];
        int[] hs = new int[n];
        int a = 0;
        for (Item item : copyItems) {
            for (int j = 0; j < item.num; j++) {
                ws[a] = item.w;
                hs[a++] = item.h;
            }
        }
        List<Integer> xList = PointSetSolver.normalPatterns(ws, W);
        List<Integer> yList = PointSetSolver.normalPatterns(hs, H);

        int xArrLength = xList.size();
        int yArrLength = yList.size();
        int[] xArr = new int[xArrLength];
        int[] yArr = new int[yArrLength];
        for (int i = 0; i < xArrLength; i++) xArr[i] = xList.get(i);
        for (int i = 0; i < yArrLength; i++) yArr[i] = yList.get(i);

        int[] maxX = new int[W + 1];
        int i = W;
        for (int j = xArrLength - 1; j >= 0; j--) {
            int x = xArr[j];
            for (; i >= x; i--) maxX[i] = x;
        }
        int[] maxY = new int[H + 1];
        i = H;
        for (int j = yArrLength - 1; j >= 0; j--) {
            int y = yArr[j];
            for (; i >= y; i--) maxY[i] = y;
        }
        for (int j = xArrLength - 1; j >= 0; j--) {
            int x = xArr[j];
            int[] FX = F[x];
            int[] UX = u[x];
            for (int k = yArrLength - 1; k >= 0; k--) {
                int y = yArr[k];
                int v = 0;

                // x + t <= W
                for (int p = j + 1; p < xArrLength; p++) {
                    int x_plus_t = xArr[p];
                    v = Math.max(v, u[x_plus_t][y] + F[maxX[x_plus_t - x]][y]);
                }
                // t + y <= H
                for (int p = k + 1; p < yArrLength; p++) {
                    int y_plus_t = yArr[p];
                    v = Math.max(v, UX[y_plus_t] + FX[maxY[y_plus_t - y]]);
                }

                UX[y] = v;
            }
        }
    }

    public boolean opt, OOM;
    public double gap;
    public long pre_time, ub0_time, dp_time, bid_time, time;
    public int UB = Integer.MAX_VALUE, LB;
    public List<PlaceItem> bestPlaceItemList;

    public int LB_Pie, UB_Pie;

    public void solve() {
        TimeUtil.startTime = System.currentTimeMillis();

        // 预处理
        pre_time = System.currentTimeMillis();
        try {
            preprocessing();
        } catch (TerminationException e) {
        }
        pre_time = System.currentTimeMillis() - pre_time;

        if (UB > LB && !TimeUtil.isTimeLimit()) {

            // 计算上界
            ub0_time = System.currentTimeMillis();
            computeUB0();
            ub0_time = System.currentTimeMillis() - ub0_time;

            if (UB > LB && !TimeUtil.isTimeLimit()) {

                // 贪心动态规划计算下界
                if (W <= 1000 && H <= 1000 && n <= 200) {
                    dp_time = System.currentTimeMillis();
                    DP_Heu_Solver dpHeuSolver = new DP_Heu_Solver();
                    dpHeuSolver.solve(m, n, W, H, S, items, UB, LB, bestPlaceItemList, dpV, dpX, dpY, u);
                    if (dpHeuSolver.LB > LB) {
                        LB = dpHeuSolver.LB;
                        bestPlaceItemList = dpHeuSolver.bestPlaceItemList;
                    }
                    dp_time = System.currentTimeMillis() - dp_time;
                }

                if (UB > LB && !TimeUtil.isTimeLimit()) {
                    Random random = new Random(929L);

                    // 双向打包过程
                    bid_time = System.currentTimeMillis();
                    Bidirectional_TS_Solver bidirectionalTreeSearchSolver = new Bidirectional_TS_Solver(m, n, W, H, S, items, random, UB, LB, bestPlaceItemList, dpV, dpX, dpY, u, TimeUtil.getRemainingTime());
                    try {
                        bidirectionalTreeSearchSolver.solve();
                    } catch (OutOfMemoryError e) {
                        OOM = true;
                    }
                    bestPlaceItemList = bidirectionalTreeSearchSolver.bestPlaceItemList;
                    LB = bidirectionalTreeSearchSolver.LB;
                    UB = bidirectionalTreeSearchSolver.UB;
                    bid_time = System.currentTimeMillis() - bid_time;

                    LB_Pie = LB;
                    UB_Pie = UB;

                    if (UB > LB && !TimeUtil.isTimeLimit()) {

                        // 精确枚举过程
                        ICE_Solver iceSolver = new ICE_Solver(m, n, W, H, S, dpV, items, random, UB, LB, bestPlaceItemList);
                        iceSolver.solve();
                        bestPlaceItemList = iceSolver.bestPlaceItemList;
                        LB = iceSolver.LB;
                        UB = iceSolver.UB;

                    }

                }
            }
        }

        if (!OOM) {
            LB_Pie = LB;
            UB_Pie = UB;
        }

        time = TimeUtil.getCurTime();
        if (UB == LB) opt = true;
        if (!opt) gap = (UB - LB) / (double) UB;
    }

}