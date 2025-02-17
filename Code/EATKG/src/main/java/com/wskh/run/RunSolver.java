package com.wskh.run;

import com.wskh.classes.Instance;
import com.wskh.classes.Item;
import com.wskh.classes.PlaceItem;
import com.wskh.classes.TerminationException;
import com.wskh.components.FastGreedySolver;
import com.wskh.solvers.EATKG_Solver;
import com.wskh.utils.CheckUtil;
import com.wskh.utils.ReadUtil;
import com.wskh.utils.TimeUtil;
import com.wskh.utils.WriteUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RunSolver {

    private static void dfsCorrect(int[] usedCntArr, Item[] items, int i, int[] values, List<PlaceItem> bestPlaceItemList) {
        if (i == bestPlaceItemList.size()) throw new TerminationException();
        PlaceItem placeItem = bestPlaceItemList.get(i);
        int value = values[i];
        List<int[]> list = new ArrayList<>();
        for (int j = 0; j < items.length; j++) {
            if (usedCntArr[j] > 0) {
                Item item = items[j];
                if (item.value == value && item.w <= placeItem.w && item.h <= placeItem.h) {
                    list.add(new int[]{placeItem.w - item.w + placeItem.h - item.h, j});
                }
            }
        }
        list.sort(Comparator.comparingInt(o -> o[0]));
        for (int[] arr : list) {
            int itemIndex = arr[1];
            Item item = items[itemIndex];
            placeItem.id = item.id;
            placeItem.index = item.index;
            placeItem.w = item.w;
            placeItem.h = item.h;
            placeItem.s = item.s;
            // dfs
            usedCntArr[itemIndex]--;
            dfsCorrect(usedCntArr, items, i + 1, values, bestPlaceItemList);
            usedCntArr[itemIndex]++;
        }
    }

    private static void solve(FileOutputStream csv, String setName, String subsetName, String instanceDir, String resultDir) throws Exception {

        resultDir = resultDir + setName + "\\" + subsetName + "\\";
        String imgDir = resultDir + "img\\";
        String solutionDir = resultDir + "solution\\";
        new File(resultDir).mkdirs();
        new File(imgDir).mkdirs();
        new File(solutionDir).mkdirs();
        File[] files = new File(instanceDir).listFiles();
        Arrays.sort(files, Comparator.comparingInt((File o) -> o.getName().length()).thenComparing(File::getName));

        for (File file : files) {

            if (!file.getName().endsWith(".ins")) continue;

            if (csv != null) System.out.println("=========== " + file.getAbsolutePath() + " ===========");

            if (file.getName().equals("GCUT13.ins")) {
                TimeUtil.TimeLimit = (long) (365d / 3787d * 1158d * 1000);
            } else if (file.getName().contains("AB")) {
                TimeUtil.TimeLimit = (long) (240d / 3787d * 1158d * 1000);
            } else {
                TimeUtil.TimeLimit = (long) (120d / 3787d * 1158d * 1000);
            }

//            TimeUtil.TimeLimit = 530 * 1000L;

            Instance instance = ReadUtil.readInstance(file.getAbsolutePath());

            EATKG_Solver solver = new EATKG_Solver(instance.m, instance.n, instance.W, instance.H, instance.S, Item.copy(instance.items));
            List<PlaceItem> bestPlaceItemList;
            if (csv != null) {
                solver.solve();
                bestPlaceItemList = solver.bestPlaceItemList;

                for (PlaceItem placeItem : bestPlaceItemList) {
                    Item item = solver.items[placeItem.index];
                    if (item.id != placeItem.id || item.index != placeItem.index || item.w != placeItem.w || item.h != placeItem.h || item.s != placeItem.s) {
                        throw new RuntimeException();
                    }
                }

                // 修正尺寸和id（dfs，尝试所有可能）
                int[] usedCntArr = new int[instance.m];
                for (int i = 0; i < instance.items.length; i++) usedCntArr[i] = instance.items[i].num;
                int[] values = new int[bestPlaceItemList.size()];
                for (int i = 0; i < bestPlaceItemList.size(); i++)
                    values[i] = solver.items[bestPlaceItemList.get(i).index].value;

                boolean correct = false;
                try {
                    dfsCorrect(usedCntArr, instance.items, 0, values, bestPlaceItemList);
                } catch (TerminationException e) {
                    correct = true;
                }
                if (!correct) throw new RuntimeException();

            } else {
                bestPlaceItemList = new FastGreedySolver().solve(instance.m, instance.n, instance.W, instance.H, instance.S, Item.copy(instance.items));
            }

            // 检查
            CheckUtil.checkOverlapAndOutBin(instance.W, instance.H, bestPlaceItemList);
            CheckUtil.checkExceedTypeNum(instance.items, bestPlaceItemList);
            CheckUtil.checkGuillotineCut(instance.W, instance.H, bestPlaceItemList);

            // 导出结果文件
            if (csv != null) {

                int sum_w = 0, sum_h = 0;
                for (Item item : instance.items) {
                    sum_w += (item.w * item.num);
                    sum_h += (item.h * item.num);
                }

                int sum_wPie = 0, sum_hPie = 0;
                for (Item item : solver.items) {
                    sum_wPie += (item.w * item.num);
                    sum_hPie += (item.h * item.num);
                }

                csv.write((setName + "," + subsetName + "," + (file.getName().replace(".ins", "")) + "," + instance.m + "," + instance.n + "," + instance.W + ","
                        + instance.H + "," + sum_w + "," + sum_h + "," + solver.m + "," + solver.n + "," + solver.W + "," + solver.H + "," + sum_wPie + "," + sum_hPie + "," + solver.UB + "," + solver.LB + "," + solver.UB_Pie + "," + solver.LB_Pie + ","
                        + solver.gap + "," + (solver.opt ? 1 : 0) + "," + (solver.OOM ? 1 : 0) + ","
                        + solver.pre_time / 1000d + "," + solver.ub0_time / 1000d + "," + solver.dp_time / 1000d + "," + solver.bid_time / 1000d + "," + solver.time / 1000d + "\n").getBytes(StandardCharsets.UTF_8));

                // 画图
                WriteUtil.writePatternPlotToPng(instance.W, instance.H, bestPlaceItemList, imgDir + file.getName().split("\\.")[0] + ".png");
                WriteUtil.writeSolution(solver.opt, solver.LB, solver.UB, solver.gap, instance.W, instance.H, bestPlaceItemList, instance.items, solutionDir + file.getName().split("\\.")[0] + ".sol");

                // 输出
                System.out.println("pre_time: " + solver.pre_time + " ms");
                System.out.println("ub0_time: " + solver.ub0_time + " ms");
                System.out.println("dp_time: " + solver.dp_time + " ms");
                System.out.println("bid_time: " + solver.bid_time + " ms");
                System.out.println("total_time: " + solver.time + " ms");
                System.out.println("LB: " + solver.LB + " , UB: " + solver.UB + " , Gap: " + solver.gap + " , Opt: " + solver.opt);

            }

        }

    }

    public static void main(String[] args) throws Exception {

        String resDir = "res//";
        new File(resDir).mkdirs();
        File instancesDir = new File("Directory address of the benchmark instances"); // such as: "./EATKG_For_2KPG/Instances"
        FileOutputStream csv = null;

        // 预热
        for (int i = 0; i < 3; i++) {
            for (File setDir : Objects.requireNonNull(instancesDir.listFiles())) {
                if (setDir.isDirectory()) {
                    String setName = setDir.getName();
                    for (File subsetDir : Objects.requireNonNull(setDir.listFiles())) {
                        String subsetName = subsetDir.getName();
                        solve(csv, setName, subsetName, subsetDir.getAbsolutePath(), resDir);
                    }
                }
            }
        }

        csv = new FileOutputStream(resDir + "Res-2KP-G.csv");
        csv.write(("set,subset,instance,m,n,W,H,sum_w,sum_h,m',n',W',H',sum_w',sum_h',ub,lb,ub',lb',gap,opt,OOM,pre_time(s),ub0_time(s),dp_time(s),bid_time(s),time(s)" + "\n").getBytes(StandardCharsets.UTF_8));

        for (File setDir : Objects.requireNonNull(instancesDir.listFiles())) {
            if (setDir.isDirectory()) {
                String setName = setDir.getName();
                for (File subsetDir : Objects.requireNonNull(setDir.listFiles())) {
                    String subsetName = subsetDir.getName();
                    solve(csv, setName, subsetName, subsetDir.getAbsolutePath(), resDir);
                    System.gc();
                }
            }
        }

        csv.close();

    }
}