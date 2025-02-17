package com.wskh.solvers;

import com.wskh.classes.Item;
import com.wskh.classes.PlaceItem;
import com.wskh.classes.TerminationException;
import com.wskh.utils.CommonUtil;
import com.wskh.utils.TimeUtil;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ICE_Solver {

    public int UB, LB;
    public List<PlaceItem> bestPlaceItemList;
    Item[] items;
    int m, n, W, H, S;
    int[][] eachItemDifIndexList;
    int[][] eachItemDominateIndexList;
    int[] dpV;
    double[] fracValueArr;
    Random random;

    public ICE_Solver(int m, int n, int W, int H, int S, int[] dpV, Item[] items, Random random, int initUB, int initLB, List<PlaceItem> initPlaceItemList) {
        this.m = m;
        this.n = n;
        this.W = W;
        this.H = H;
        this.S = S;
        this.dpV = dpV;
        this.items = items;
        this.random = random;
        LB = initLB;
        UB = initUB;
        bestPlaceItemList = initPlaceItemList;
    }

    private void packedAll(int idx, Item item, int remainingCapacity, int[] itemDifList, boolean[] used, int value, int packedItemTypeNum, Item[] packedItemArr) {
        int s = item.num * item.s;
        value += item.num * item.value;
        if (remainingCapacity >= s && !used[idx] && value <= targetValue) {
            // 打包 idx
            // 更新所有与 i 冲突的物品 j ，将其设置为 1（防止后面被打包）
            used[idx] = true;
            for (int j : itemDifList) used[j] = true;
            // 更新容量和价值
            packedItemArr[packedItemTypeNum++] = item;

            if (value == targetValue) {
                // 进行 Opp check
                DP_OPP_Solver oppSolver = new DP_OPP_Solver();
                Item[] oppItems = new Item[packedItemTypeNum];
                for (int i = 0; i < packedItemTypeNum; i++) oppItems[i] = packedItemArr[i].copy();
                List<PlaceItem> placeItemList = oppSolver.solve(W, H, S, oppItems);
                oppCnt++;

                // 如果check通过，则回到正常DFS
                if (placeItemList != null) {
                    // 更新全局最优解
                    bestPlaceItemList = placeItemList;
                    LB = value;
                    UB = LB;
                    System.out.println("Find opt solution: " + LB);
                    throw new TerminationException();
                }
            } else {
                DepthFirstSearch(idx + 1, used, value, remainingCapacity - s, packedItemArr, packedItemTypeNum);
            }

            // 回溯
            used[idx] = false;
            for (int j : itemDifList) used[j] = false;
        }
    }

    public long exploredNodes;
    public long generatedNodes;
    public long oppCnt;
    public int targetValue;

    private void DepthFirstSearch(int idx, boolean[] used, int value, int remainingCapacity, Item[] packedItemArr, int packedItemTypeNum) {
        if (idx < m) {

            // 剪枝
            if (dpV[remainingCapacity] + value < targetValue) return;

            int tempIdx = idx;
            int localUB = value;
            int tempRemainingCapacity = remainingCapacity;
            for (; tempIdx < m; tempIdx++) {
                if (!used[tempIdx]) {
                    Item item = items[tempIdx];
                    int s = item.s * item.num;
                    int v = item.value * item.num;
                    if (s <= tempRemainingCapacity) {
                        localUB += v;
                        tempRemainingCapacity -= s;
                    } else {
                        localUB += (int) (fracValueArr[tempIdx] * tempRemainingCapacity);
                        break;
                    }
                }
            }
            if (localUB < targetValue) return;

            if (TimeUtil.isTimeLimit()) throw new TerminationException();

            generatedNodes++;

            // 放全部物品
            int[] itemDifList = eachItemDifIndexList[idx];
            Item item = items[idx];
            packedAll(idx, item, remainingCapacity, itemDifList, used, value, packedItemTypeNum, packedItemArr);

            // 放一部分物品
            int oldNum = item.num;
            int[] list = eachItemDominateIndexList[idx];
            for (int packNum = item.num - 1; packNum > 0; packNum--) {
                int s = packNum * item.s;
                int v = packNum * item.value;
                value += v;
                if (remainingCapacity >= s && !used[idx] && value <= targetValue) {
                    // 打包 idx
                    // 更新所有与 i 冲突的物品 j ，将其设置为 1（防止后面被打包）
                    used[idx] = true;
                    for (int j : itemDifList) used[j] = true;
                    for (int idxD : list) used[idxD] = true;

                    // 更新容量和价值
                    item.num = packNum;
                    packedItemArr[packedItemTypeNum++] = item;

                    if (value == targetValue) {
                        // 进行 Opp check
                        DP_OPP_Solver oppSolver = new DP_OPP_Solver();
                        Item[] oppItems = new Item[packedItemTypeNum];
                        for (int i = 0; i < packedItemTypeNum; i++) oppItems[i] = packedItemArr[i].copy();
                        List<PlaceItem> placeItemList = oppSolver.solve(W, H, S, oppItems);
                        oppCnt++;

                        // 如果check通过，则回到正常DFS
                        if (placeItemList != null) {
                            // 更新全局最优解
                            bestPlaceItemList = placeItemList;
                            LB = value;
                            UB = LB;
                            System.out.println("Find opt solution: " + LB);
                            throw new TerminationException();
                        }
                    } else {
                        DepthFirstSearch(idx + 1, used, value, remainingCapacity - s, packedItemArr, packedItemTypeNum);
                    }

                    // 回溯
                    used[idx] = false;
                    item.num = oldNum;
                    for (int idxD : list) used[idxD] = false;
                    for (int j : itemDifList) used[j] = false;
                    packedItemTypeNum--;
                }
                value -= v;
            }

            // 不放物品
            for (int idxD : list) used[idxD] = true;
            DepthFirstSearch(idx + 1, used, value, remainingCapacity, packedItemArr, packedItemTypeNum);
            for (int idxD : list) used[idxD] = false;

            exploredNodes++;
        }
    }

    public void solve() {

        System.out.println("init bounds: " + LB + " " + UB + " " + TimeUtil.getCurTime() + " ms");

        fracValueArr = new double[m];
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            fracValueArr[i] = (double) item.value / item.s;
        }

        List<Integer>[] tempEachItemDifIndexList = new List[m];
        eachItemDominateIndexList = new int[m][];

        for (int i = 0; i < m; i++) tempEachItemDifIndexList[i] = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            Item itemI = items[i];
            List<Integer> conflictListI = tempEachItemDifIndexList[i];
            List<Integer> domListI = new ArrayList<>(m);

            for (int j = i + 1; j < m; j++) {
                // 冲突
                Item itemJ = items[j];
                if (itemI.w + itemJ.w > W && itemI.h + itemJ.h > H) {
                    conflictListI.add(j);
                    tempEachItemDifIndexList[j].add(i);
                }
                // 支配
                if (itemI.w <= itemJ.w && itemI.h <= itemJ.h && itemI.value >= itemJ.value) {
                    domListI.add(j);
                }
            }

            int[] arr = new int[domListI.size()];
            int a = 0;
            for (int index : domListI) arr[a++] = index;
            eachItemDominateIndexList[i] = arr;
        }

        eachItemDifIndexList = new int[m][];
        for (int i = 0; i < m; i++) {
            List<Integer> list = tempEachItemDifIndexList[i];
            int[] arr = new int[list.size()];
            int a = 0;
            for (int index : list) arr[a++] = index;
            eachItemDifIndexList[i] = arr;
        }

        IloCplex cplex = null;
        try {
            cplex = new IloCplex();
            cplex.setOut(null);
            cplex.setWarning(null);
            cplex.setParam(IloCplex.IntParam.Threads, 1);
            cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 0.0001d / UB);
            IloIntVar[] x = new IloIntVar[m];
            IloLinearNumExpr objective = cplex.linearNumExpr();
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int i = 0; i < m; i++) {
                Item item = items[i];
                x[i] = cplex.intVar(0, item.num);
                objective.addTerm(x[i], item.value);
                expr.addTerm(x[i], item.s);
            }
            cplex.addLe(expr, S);
            cplex.addMaximize(objective);

            while (!TimeUtil.isTimeLimit()) {
                targetValue = UB;
                DepthFirstSearch(0, new boolean[m], 0, S, new Item[m], 0);

                cplex.addLe(objective, UB - 1);
                cplex.solve();

                UB = CommonUtil.roundToNearestInt(cplex.getObjValue());
                System.out.println("Find better UB: " + UB);

                if (UB == LB) break;
                if (UB < LB) throw new RuntimeException();
            }
        } catch (TerminationException _) {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        cplex.end();

        System.out.println(exploredNodes + " " + generatedNodes + " " + oppCnt);

    }

}