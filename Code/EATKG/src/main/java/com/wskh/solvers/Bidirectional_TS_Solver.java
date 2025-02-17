package com.wskh.solvers;

import com.wskh.classes.*;
import com.wskh.components.SpaceHeuSolver;
import com.wskh.utils.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

public class Bidirectional_TS_Solver {
    double alpha;

    private boolean isTimeLimit() {
        return System.currentTimeMillis() - timer >= timeLimit;
    }

    @ToString
    static class Block {
        int w, h, s, value;
        short[] usedCntArr;
        List<PlaceItem> placeItemList;
    }

    @AllArgsConstructor
    class Node {
        int bestCompleteV;
        int totalV;
        short[] usedCntArr;
        ArrayList<Space> spaces;
        List<PlaceItem> placeItemList;
        List<Block> blockList;

        public Node(List<Block> blockList) {
            spaces = new ArrayList<>();
            spaces.add(new Space(0, 0, W, H));
            placeItemList = new ArrayList<>();
            usedCntArr = new short[m];
            this.blockList = blockList;
        }
    }

    private Node createChild(int totalV, short[] usedCntArr, List<PlaceItem> placeItemList, ArrayList<Space> spaces, Space space, Block block, List<Block> blockList) {
        // 把block放置在左下角，然后切割空间
        int newW = space.w - block.w;
        int newH = space.h - block.h;

        if (newW < 0 || newH < 0) return null;

        int childV = totalV + block.value;
        short[] childUsedCntArr = usedCntArr.clone();
        for (int i = 0; i < m; i++) childUsedCntArr[i] += block.usedCntArr[i];
        List<PlaceItem> childPlaceItemList = new ArrayList<>(placeItemList);
        for (PlaceItem placeItem : block.placeItemList) {
            PlaceItem copy = placeItem.copy();
            copy.x += space.x;
            copy.y += space.y;
            childPlaceItemList.add(copy);
        }

        List<Block> childBlockList = new ArrayList<>(blockList.size());
        for (Block b : blockList) {
            boolean exc = false;
            for (int i = 0; i < m; i++) {
                if (childUsedCntArr[i] + b.usedCntArr[i] > items[i].num) {
                    exc = true;
                    break;
                }
            }
            if (!exc) childBlockList.add(b);
        }

        // 垂直/水平切割
        Space space1 = new Space(space.x, space.y + block.h, block.w, newH); // 垂直
        Space space2 = new Space(space.x + block.w, space.y, newW, space.h); // 垂直
        Space space3 = new Space(space.x, space.y + block.h, space.w, newH); // 水平
        Space space4 = new Space(space.x + block.w, space.y, newW, block.h); // 水平
        int s1 = space1.w * space1.h;
        int s2 = space2.w * space2.h;
        int s3 = space3.w * space3.h;
        int s4 = space4.w * space4.h;

        // 垂直切割
        ArrayList<Space> nextSpaces = new ArrayList<>(spaces);
        if (s1 > s2) {
            if (space1.w > 0 && space1.h > 0) nextSpaces.add(space1);
            if (space2.w > 0 && space2.h > 0) nextSpaces.add(space2);
        } else {
            if (space2.w > 0 && space2.h > 0) nextSpaces.add(space2);
            if (space1.w > 0 && space1.h > 0) nextSpaces.add(space1);
        }

        int bestCompleteV1 = completeBlock(childV, childUsedCntArr.clone(), new ArrayList<>(childPlaceItemList), new ArrayList<>(nextSpaces), childBlockList);

        // 水平切割
        ArrayList<Space> nextSpaces2 = new ArrayList<>(spaces);
        if (s3 > s4) {
            if (space3.w > 0 && space3.h > 0) nextSpaces2.add(space3);
            if (space4.w > 0 && space4.h > 0) nextSpaces2.add(space4);
        } else {
            if (space4.w > 0 && space4.h > 0) nextSpaces2.add(space4);
            if (space3.w > 0 && space3.h > 0) nextSpaces2.add(space3);
        }

        int bestCompleteV2 = completeBlock(childV, childUsedCntArr.clone(), new ArrayList<>(childPlaceItemList), new ArrayList<>(nextSpaces2), childBlockList);

        if (bestCompleteV2 > bestCompleteV1) {
            return new Node(bestCompleteV2, childV, childUsedCntArr, nextSpaces2, childPlaceItemList, childBlockList);
        } else {
            return new Node(bestCompleteV1, childV, childUsedCntArr, nextSpaces, childPlaceItemList, childBlockList);
        }
    }

    public void beamSearch(int beamSize, List<Block> blockList) {
        List<Node> curNodeList = new ArrayList<>();
        curNodeList.add(new Node(blockList));

        while (!curNodeList.isEmpty()) {
            List<Node> newNodeList = new ArrayList<>();
            for (Node node : curNodeList) {
                while (true) {
                    Space space = node.spaces.remove(node.spaces.size() - 1);
                    int addCnt = 0;
                    for (Block block : node.blockList) {
                        if (isTimeLimit()) throw new TerminationException();
                        Node childNode = createChild(node.totalV, node.usedCntArr, node.placeItemList, node.spaces, space, block, node.blockList);
                        if (childNode != null) {
                            if (childNode.spaces.isEmpty()) {
                                // 叶子节点
                                if (childNode.totalV > LB) {
                                    LB = childNode.totalV;
                                    bestPlaceItemList = childNode.placeItemList;
                                    if (LB == UB) throw new TerminationException();
                                }
                            } else {
                                newNodeList.add(childNode);
                                addCnt++;
                            }
                        }
                    }
                    if (addCnt > 0) {
                        break;
                    } else {
                        if (node.spaces.isEmpty()) {
                            // 叶子节点
                            if (node.totalV > LB) {
                                LB = node.totalV;
                                bestPlaceItemList = node.placeItemList;
                                if (LB == UB) throw new TerminationException();
                            }
                            break;
                        }
                    }
                }

            }
            newNodeList.sort((o1, o2) -> -Integer.compare(o1.bestCompleteV, o2.bestCompleteV));
            curNodeList.clear();
            Set<String> set = new HashSet<>();
            for (Node node : newNodeList) {
                if (set.add(Arrays.toString(node.usedCntArr))) {
                    curNodeList.add(node);
                    if (curNodeList.size() == beamSize) break;
                }
            }
        }
    }

    private boolean fathomingByBound(int blockValue, int blockW, int blockH, int blockS, short[] usedCntArr, int minV) {

        int ub1 = blockValue + u[blockW][blockH];
        if (ub1 < minV) return true;

        int remainH = H - blockH;
        int remainW = W - blockW;

        int remainS = S - blockS - blockW * (remainH - dpY[remainH]) - blockH * (remainW - dpX[remainW]);

        int ub2 = blockValue + dpV[remainS];
        if (ub2 < minV) return true;

        int ub3 = blockValue;
        for (int i = 0; i < m; i++) {
            int c = items[i].num - usedCntArr[i];
            if (c > 0) {
                Item item = items[i];
                if (item.w + blockW > W && item.h + blockH > H) continue;
                int newRemainS = remainS - item.s * c;
                if (newRemainS >= 0) {
                    ub3 += item.value * c;
                    remainS = newRemainS;
                } else {
                    ub3 += CommonUtil.ceilToInt((double) item.value / item.s * remainS);
                    break;
                }
            }
        }

        return ub3 < minV;
    }

    private void combineBlock(Block blockA, Block blockB, List<Block> blockList, Set<String> blockSet, int minV) {
        // 判断是否超出类型数量
        short[] usedCntArr = blockA.usedCntArr.clone();
        for (int i = 0; i < m; i++) {
            usedCntArr[i] += blockB.usedCntArr[i];
            if (usedCntArr[i] > items[i].num) return;
        }
        // Y
        String key = null;
        if (blockA.h + blockB.h <= H) {
            Block blockYCombine = new Block();
            blockYCombine.usedCntArr = usedCntArr;
            blockYCombine.h = blockA.h + blockB.h;
            blockYCombine.w = Math.max(blockA.w, blockB.w);
            blockYCombine.value = blockA.value + blockB.value;
            blockYCombine.s = blockYCombine.w * blockYCombine.h;
            if (!fathomingByBound(blockYCombine.value, blockYCombine.w, blockYCombine.h, blockYCombine.s, blockYCombine.usedCntArr, minV)) {
                key = Arrays.toString(usedCntArr);
                if (blockSet.add(blockYCombine.w + "-" + blockYCombine.h + "-" + key)) {
                    blockYCombine.placeItemList = new ArrayList<>(blockA.placeItemList);
                    for (PlaceItem placeItem : blockB.placeItemList) {
                        PlaceItem copyPlaceItem = placeItem.copy();
                        copyPlaceItem.y += blockA.h;
                        blockYCombine.placeItemList.add(copyPlaceItem);
                    }
                    blockList.add(blockYCombine);
                }
            }
        }
        // X
        if (blockA.w + blockB.w <= W) {
            Block blockXCombine = new Block();
            blockXCombine.usedCntArr = usedCntArr;
            blockXCombine.w = blockA.w + blockB.w;
            blockXCombine.h = Math.max(blockA.h, blockB.h);
            blockXCombine.value = blockA.value + blockB.value;
            blockXCombine.s = blockXCombine.w * blockXCombine.h;
            if (!fathomingByBound(blockXCombine.value, blockXCombine.w, blockXCombine.h, blockXCombine.s, blockXCombine.usedCntArr, minV)) {
                if (key == null) key = Arrays.toString(usedCntArr);
                if (blockSet.add(blockXCombine.w + "-" + blockXCombine.h + "-" + key)) {
                    blockXCombine.placeItemList = new ArrayList<>(blockA.placeItemList);
                    for (PlaceItem placeItem : blockB.placeItemList) {
                        PlaceItem copyPlaceItem = placeItem.copy();
                        copyPlaceItem.x += blockA.w;
                        blockXCombine.placeItemList.add(copyPlaceItem);
                    }
                    blockList.add(blockXCombine);
                }
            }
        }
    }

    private List<Block> grouping(int maxCnt, int minV) {
        // 构建初始块
        List<Block> blockList = new ArrayList<>();
        for (Item item : items) {
            Block block = new Block();
            block.w = item.w;
            block.h = item.h;
            block.s = item.s;
            block.value = item.value;
            block.usedCntArr = new short[m];
            block.usedCntArr[item.index] = 1;
            if (!fathomingByBound(block.value, block.w, block.h, block.s, block.usedCntArr, minV)) {
                block.placeItemList = new ArrayList<>(n);
                block.placeItemList.add(new PlaceItem(item.id, item.index, 0, 0, item.w, item.h, item.s));
                blockList.add(block);
            }
        }
        // 循环构建更多的块
        Set<String> blockSet = new HashSet<>();
        List<Block> preList = blockList;
        while (blockList.size() < maxCnt) {
            List<Block> curList = new ArrayList<>();
            for (Block b1 : blockList) {
                for (Block b2 : preList) {
                    if (isTimeLimit()) {
                        for (Block block : blockList) {
                            if (block.value > LB) {
                                LB = block.value;
                                bestPlaceItemList = block.placeItemList;
                            }
                        }
                        throw new TerminationException();
                    }
                    combineBlock(b1, b2, curList, blockSet, minV);
                    if (blockList.size() + curList.size() > maxCnt) {
                        blockList.addAll(curList);
                        for (Block block : blockList) {
                            if (block.value > LB) {
                                LB = block.value;
                                bestPlaceItemList = block.placeItemList;
                                if (LB == UB) throw new TerminationException();
                            }
                        }
                        return blockList;
                    }
                }
            }
            if (curList.isEmpty()) break;
            blockList.addAll(curList);
            preList = curList;
        }
        // 用最大价值的块更新当前最优解
        for (Block block : blockList) {
            if (block.value > LB) {
                LB = block.value;
                bestPlaceItemList = block.placeItemList;
                if (LB == UB) throw new TerminationException();
            }
        }
        return blockList;
    }

    @NoArgsConstructor
    static class TreeNode {
        // -1: V, -2: H, -3: 剩余空间
        int type;
        int x, y, W, H;
        TreeNode parentNode;
        int canRemoveNodeCnt;
        List<TreeNode> childNodeList = new ArrayList<>();

        public TreeNode(List<TreeNode> remainNodeList, TreeNode TreeNode) {
            type = TreeNode.type;
            x = TreeNode.x;
            y = TreeNode.y;
            W = TreeNode.W;
            H = TreeNode.H;
            if (type == -3) {
                remainNodeList.add(this);
            } else {
                canRemoveNodeCnt = TreeNode.canRemoveNodeCnt;
                childNodeList = new ArrayList<>(TreeNode.childNodeList.size());
                for (TreeNode childNode : TreeNode.childNodeList) {
                    addChildNode(new TreeNode(remainNodeList, childNode));
                }
            }
        }

        void addChildNode(TreeNode childNode) {
            childNode.parentNode = this;
            childNodeList.add(childNode);
        }
    }

    @NoArgsConstructor
    static class Bin {
        int totalV;
        short[] usedCntArr;
        TreeNode rootNode;
        List<TreeNode> remainSpaceNodeList = new ArrayList<>();

        public Bin(Bin bin) {
            totalV = bin.totalV;
            usedCntArr = bin.usedCntArr.clone();
            remainSpaceNodeList = new ArrayList<>(bin.remainSpaceNodeList.size());
            rootNode = new TreeNode(remainSpaceNodeList, bin.rootNode);
        }
    }

    private void addPosition(int p, boolean[] positionUsedArr, List<Integer> positions) {
        if (!positionUsedArr[p]) {
            positionUsedArr[p] = true;
            positions.add(p);
        }
    }

    private void setCanRemoveNodeCnt(TreeNode TreeNode) {
        TreeNode.canRemoveNodeCnt = 1;
        TreeNode parentNode = TreeNode.parentNode;
        while (parentNode != null) {
            parentNode.canRemoveNodeCnt++;
            parentNode = parentNode.parentNode;
        }
    }

    public void getNodeByPlaceItemList(TreeNode TreeNode, List<PlaceItem> placeItemList, Bin bin) {

        if (placeItemList.isEmpty()) {
            // 空间节点
            TreeNode.type = -3;
            bin.remainSpaceNodeList.add(TreeNode);
            return;
        }

        if (placeItemList.size() == 1) {
            // 物品节点
            int itemIndex = placeItemList.get(0).index;
            Item item = items[itemIndex];
            bin.totalV += item.value;
            bin.usedCntArr[itemIndex]++;
            TreeNode.type = itemIndex;
            setCanRemoveNodeCnt(TreeNode);
            return;
        }

        // 竖着切
        List<Integer> xPositions = new ArrayList<>(W);
        boolean[] xUsedArr = new boolean[W];
        for (PlaceItem placeItem : placeItemList) {
            int x1 = placeItem.x;
            int x2 = x1 + placeItem.w;
            if (x1 > TreeNode.x) addPosition(x1, xUsedArr, xPositions);
            if (x2 < TreeNode.x + TreeNode.W) addPosition(x2, xUsedArr, xPositions);
        }

        for (int cutX : xPositions) {
            boolean canCut = true;
            List<PlaceItem> leftPlaceItemList = new ArrayList<>(placeItemList.size());
            List<PlaceItem> rightPlaceItemList = new ArrayList<>(placeItemList.size());
            for (PlaceItem placeItem : placeItemList) {
                if (placeItem.x + placeItem.w <= cutX) {
                    leftPlaceItemList.add(placeItem);
                } else if (placeItem.x >= cutX) {
                    rightPlaceItemList.add(placeItem);
                } else {
                    canCut = false;
                    break;
                }
            }
            if (canCut) {
                TreeNode.type = -1;
                TreeNode childNode1 = new TreeNode();
                childNode1.x = TreeNode.x;
                childNode1.y = TreeNode.y;
                childNode1.W = cutX - TreeNode.x;
                childNode1.H = TreeNode.H;

                TreeNode childNode2 = new TreeNode();
                childNode2.x = cutX;
                childNode2.y = TreeNode.y;
                childNode2.W = TreeNode.x + TreeNode.W - cutX;
                childNode2.H = TreeNode.H;

                setCanRemoveNodeCnt(TreeNode);
                TreeNode.addChildNode(childNode1);
                TreeNode.addChildNode(childNode2);

                getNodeByPlaceItemList(childNode1, leftPlaceItemList, bin);
                getNodeByPlaceItemList(childNode2, rightPlaceItemList, bin);

                return;
            }
        }

        // 横着切
        List<Integer> yPositions = new ArrayList<>(H);
        boolean[] yUsedArr = new boolean[H];
        for (PlaceItem placeItem : placeItemList) {
            int y1 = placeItem.y;
            int y2 = y1 + placeItem.h;
            if (y1 > TreeNode.y) addPosition(y1, yUsedArr, yPositions);
            if (y2 < TreeNode.y + TreeNode.H) addPosition(y2, yUsedArr, yPositions);
        }
        for (int cutY : yPositions) {
            boolean canCut = true;
            List<PlaceItem> bottomPlaceItemList = new ArrayList<>(placeItemList.size());
            List<PlaceItem> topPlaceItemList = new ArrayList<>(placeItemList.size());
            for (PlaceItem placeItem : placeItemList) {
                if (placeItem.y + placeItem.h <= cutY) {
                    bottomPlaceItemList.add(placeItem);
                } else if (placeItem.y >= cutY) {
                    topPlaceItemList.add(placeItem);
                } else {
                    canCut = false;
                    break;
                }
            }
            if (canCut) {
                TreeNode.type = -1;
                TreeNode childNode1 = new TreeNode();
                childNode1.x = TreeNode.x;
                childNode1.y = TreeNode.y;
                childNode1.W = TreeNode.W;
                childNode1.H = cutY - TreeNode.y;

                TreeNode childNode2 = new TreeNode();
                childNode2.x = TreeNode.x;
                childNode2.y = cutY;
                childNode2.W = TreeNode.W;
                childNode2.H = TreeNode.y + TreeNode.H - cutY;

                setCanRemoveNodeCnt(TreeNode);
                TreeNode.addChildNode(childNode1);
                TreeNode.addChildNode(childNode2);

                getNodeByPlaceItemList(childNode1, bottomPlaceItemList, bin);
                getNodeByPlaceItemList(childNode2, topPlaceItemList, bin);

                return;
            }
        }

        // 意料之外的情况：报错
        System.out.println(placeItemList);
        throw new RuntimeException("意料之外的情况");
    }

    private void packItemInBin(Item item, Bin bin, int bestSpaceIndex, boolean isV) {
        bin.usedCntArr[item.index]++;
        bin.totalV += item.value;
        TreeNode TreeNode = bin.remainSpaceNodeList.remove(bestSpaceIndex);
        if (isV) {
            // 左分支，H
            TreeNode newHNode = new TreeNode();
            newHNode.type = -2;
            newHNode.x = TreeNode.x;
            newHNode.y = TreeNode.y;
            newHNode.W = item.w;
            newHNode.H = TreeNode.H;

            TreeNode newItemNode = new TreeNode();
            newItemNode.type = item.index;
            newItemNode.x = TreeNode.x;
            newItemNode.y = TreeNode.y;
            newItemNode.W = item.w;
            newItemNode.H = item.h;

            TreeNode newRemainNode1 = new TreeNode();
            newRemainNode1.type = -3;
            newRemainNode1.x = TreeNode.x;
            newRemainNode1.y = TreeNode.y + item.h;
            newRemainNode1.W = item.w;
            newRemainNode1.H = TreeNode.H - item.h;

            newHNode.addChildNode(newItemNode);
            newHNode.addChildNode(newRemainNode1);

            // 右分支，剩余
            TreeNode newRemainNode2 = new TreeNode();
            newRemainNode2.type = -3;
            newRemainNode2.x = TreeNode.x + item.w;
            newRemainNode2.y = TreeNode.y;
            newRemainNode2.W = TreeNode.W - item.w;
            newRemainNode2.H = TreeNode.H;

            // 修改当前节点为：V
            TreeNode.type = -1;
            TreeNode.addChildNode(newHNode);
            TreeNode.addChildNode(newRemainNode2);

            // 添加剩余节点
            bin.remainSpaceNodeList.add(newRemainNode1);
            bin.remainSpaceNodeList.add(newRemainNode2);

            // 更新可移除的节点数量
            TreeNode.canRemoveNodeCnt = 3;
            newHNode.canRemoveNodeCnt = 2;
            newItemNode.canRemoveNodeCnt = 1;
            TreeNode parentNode = TreeNode.parentNode;
            while (parentNode != null) {
                parentNode.canRemoveNodeCnt += 3;
                parentNode = parentNode.parentNode;
            }
        } else {
            // 左分支，V
            TreeNode newVNode = new TreeNode();
            newVNode.type = -1;
            newVNode.x = TreeNode.x;
            newVNode.y = TreeNode.y;
            newVNode.W = TreeNode.W;
            newVNode.H = item.h;

            TreeNode newItemNode = new TreeNode();
            newItemNode.type = item.index;
            newItemNode.x = TreeNode.x;
            newItemNode.y = TreeNode.y;
            newItemNode.W = item.w;
            newItemNode.H = item.h;

            TreeNode newRemainNode1 = new TreeNode();
            newRemainNode1.type = -3;
            newRemainNode1.x = TreeNode.x + item.w;
            newRemainNode1.y = TreeNode.y;
            newRemainNode1.W = TreeNode.W - item.w;
            newRemainNode1.H = item.h;

            newVNode.addChildNode(newItemNode);
            newVNode.addChildNode(newRemainNode1);

            // 右分支，剩余
            TreeNode newRemainNode2 = new TreeNode();
            newRemainNode2.type = -3;
            newRemainNode2.x = TreeNode.x;
            newRemainNode2.y = TreeNode.y + item.h;
            newRemainNode2.W = TreeNode.W;
            newRemainNode2.H = TreeNode.H - item.h;

            // 修改当前节点为：H
            TreeNode.type = -2;
            TreeNode.addChildNode(newVNode);
            TreeNode.addChildNode(newRemainNode2);

            // 添加剩余节点
            bin.remainSpaceNodeList.add(newRemainNode1);
            bin.remainSpaceNodeList.add(newRemainNode2);

            // 更新可移除的节点数量
            TreeNode.canRemoveNodeCnt = 3;
            newVNode.canRemoveNodeCnt = 2;
            newItemNode.canRemoveNodeCnt = 1;
            TreeNode parentNode = TreeNode.parentNode;
            while (parentNode != null) {
                parentNode.canRemoveNodeCnt += 3;
                parentNode = parentNode.parentNode;
            }
        }
    }

    @AllArgsConstructor
    static class Pair {
        int itemIndex;
        int spaceIndex;
        int bestCompleteV;
        boolean isV;
        List<PlaceItem> placeItemList;
        List<Block> blockList;
    }

    private int completeBlock(int v, short[] usedCntArr, List<PlaceItem> placeItemList, ArrayList<Space> spaces, List<Block> blockList) {
        if (!spaces.isEmpty()) {
            while (placeItemList.size() < n) {
                int bestBlockIndex = -1;
                short maxScore = -1;
                Space space = spaces.remove(spaces.size() - 1);

                for (int i = 0; i < blockList.size(); i++) {
                    Block block = blockList.get(i);
                    if (block.w > space.w || block.h > space.h) continue;

                    short score = 0;
                    if (space.w == block.w) score++;
                    if (space.h == block.h) score++;

                    if (score > maxScore) {
                        bestBlockIndex = i;
                        if (score == 2) break;
                        maxScore = score;
                    }
                }

                if (bestBlockIndex == -1) {
                    if (spaces.isEmpty()) break;
                    continue;
                }

                Block block = blockList.get(bestBlockIndex);

                v += block.value;
                for (int i = 0; i < m; i++) {
                    usedCntArr[i] += block.usedCntArr[i];
                }
                for (PlaceItem placeItem : block.placeItemList) {
                    PlaceItem copy = placeItem.copy();
                    copy.x += space.x;
                    copy.y += space.y;
                    placeItemList.add(copy);
                }

                SpaceHeuSolver.createNewSpaceByAlpha(spaces, space, alpha, block.w, block.h);

                if (!spaces.isEmpty()) {
                    List<Block> newBlockList = new ArrayList<>(blockList.size());
                    for (Block b : blockList) {
                        boolean exc = false;
                        for (int j = 0; j < m; j++) {
                            if (block.usedCntArr[j] > 0 && usedCntArr[j] + b.usedCntArr[j] > items[j].num) {
                                exc = true;
                                break;
                            }
                        }
                        if (!exc) {
                            newBlockList.add(b);
                        }
                    }
                    blockList = newBlockList;
                } else {
                    break;
                }
            }
        }

        if (v > LB) {
            LB = v;
            bestPlaceItemList = placeItemList;

            if (LB == UB) throw new TerminationException();
        }

        return v;
    }

    private Bin repair(Bin bin, List<Block> blockList) {
        List<PlaceItem> placeItemList = new ArrayList<>(n);
        getPlaceItemListByNode(bin.rootNode, placeItemList);

        List<Block> newBlockList = new ArrayList<>(blockList.size());
        for (Block b : blockList) {
            boolean exc = false;
            for (int j = 0; j < m; j++) {
                if (bin.usedCntArr[j] + b.usedCntArr[j] > items[j].num) {
                    exc = true;
                    break;
                }
            }
            if (!exc) {
                newBlockList.add(b);
            }
        }
        blockList = newBlockList;

        // bin.totalV < UB
        while (true) {
            if (isTimeLimit()) throw new TerminationException();
            int minOptionCnt = Integer.MAX_VALUE;
            List<Integer> insertItemIndexList = new ArrayList<>();
            List<List<Integer>> options = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                // 寻找物品i的可放置位置
                Item item = items[i];
                if (bin.usedCntArr[i] < item.num) {
                    List<Integer> optionsInOneBin = new ArrayList<>(bin.remainSpaceNodeList.size());
                    for (int nodeIndex = 0; nodeIndex < bin.remainSpaceNodeList.size(); nodeIndex++) {
                        TreeNode TreeNode = bin.remainSpaceNodeList.get(nodeIndex);
                        if (TreeNode.W >= item.w && TreeNode.H >= item.h) {
                            optionsInOneBin.add(nodeIndex);
                        }
                    }
                    int optionCnt = optionsInOneBin.size();
                    if (optionCnt > 0 && optionCnt < minOptionCnt) {
                        minOptionCnt = optionCnt;
                        insertItemIndexList.clear();
                        options.clear();
                        insertItemIndexList.add(i);
                        options.add(optionsInOneBin);
                    } else if (optionCnt == minOptionCnt) {
                        insertItemIndexList.add(i);
                        options.add(optionsInOneBin);
                    }
                }
            }

            if (minOptionCnt == Integer.MAX_VALUE) break;

            // 构建对
            Pair bestPair = null;
            for (int i = 0; i < insertItemIndexList.size(); i++) {
                int itemIndex = insertItemIndexList.get(i);
                Item item = items[itemIndex];
                List<Integer> option = options.get(i);
                for (int spaceIndex : option) {

                    // 眨眼策略
                    if (bestPair != null && random.nextDouble() <= 0.05) continue;

                    TreeNode spaceNode = bin.remainSpaceNodeList.get(spaceIndex);
                    Space space = new Space(spaceNode.x, spaceNode.y, spaceNode.W, spaceNode.H);
                    ArrayList<Space> spaces = new ArrayList<>();
                    for (int j = 0; j < bin.remainSpaceNodeList.size(); j++) {
                        if (j != spaceIndex) {
                            TreeNode s = bin.remainSpaceNodeList.get(j);
                            spaces.add(new Space(s.x, s.y, s.W, s.H));
                        }
                    }
                    spaces.sort((o1, o2) -> -Integer.compare(o1.w * o1.h, o2.w * o2.h));

                    // 把block放置在左下角，然后切割空间
                    int newW = space.w - item.w;
                    int newH = space.h - item.h;
                    int nextV = bin.totalV + item.value;
                    short[] nextUsedCntArr = bin.usedCntArr.clone();
                    nextUsedCntArr[itemIndex]++;
                    List<PlaceItem> nextPlaceItemList = new ArrayList<>(placeItemList);
                    nextPlaceItemList.add(new PlaceItem(item.id, item.index, space.x, space.y, item.w, item.h, item.s));
                    List<Block> nextBlockList = new ArrayList<>(blockList.size());
                    for (Block b : blockList) {
                        boolean exc = false;
                        for (int j = 0; j < m; j++) {
                            if (nextUsedCntArr[j] + b.usedCntArr[j] > items[j].num) {
                                exc = true;
                                break;
                            }
                        }
                        if (!exc) nextBlockList.add(b);
                    }

                    // 垂直切割
                    Space space1 = new Space(space.x, space.y + item.h, item.w, newH); // 垂直
                    Space space2 = new Space(space.x + item.w, space.y, newW, space.h); // 垂直
                    int s1 = space1.w * space1.h;
                    int s2 = space2.w * space2.h;
                    ArrayList<Space> nextSpaces = new ArrayList<>(spaces);
                    if (s1 > s2) {
                        if (s1 > 0) nextSpaces.add(space1);
                        if (s2 > 0) nextSpaces.add(space2);
                    } else {
                        if (s2 > 0) nextSpaces.add(space2);
                        if (s1 > 0) nextSpaces.add(space1);
                    }

                    int bestCompleteV1 = completeBlock(nextV, nextUsedCntArr.clone(), new ArrayList<>(nextPlaceItemList), new ArrayList<>(nextSpaces), nextBlockList);

                    // 水平切割
                    Space space3 = new Space(space.x, space.y + item.h, space.w, newH); // 水平
                    Space space4 = new Space(space.x + item.w, space.y, newW, item.h); // 水平
                    int s3 = space3.w * space3.h;
                    int s4 = space4.w * space4.h;
                    ArrayList<Space> nextSpaces2 = new ArrayList<>(spaces);
                    if (s3 > s4) {
                        if (s3 > 0) nextSpaces2.add(space3);
                        if (s4 > 0) nextSpaces2.add(space4);
                    } else {
                        if (s4 > 0) nextSpaces2.add(space4);
                        if (s3 > 0) nextSpaces2.add(space3);
                    }

                    int bestCompleteV2 = completeBlock(nextV, nextUsedCntArr.clone(), new ArrayList<>(nextPlaceItemList), new ArrayList<>(nextSpaces2), nextBlockList);

                    if (bestCompleteV2 > bestCompleteV1) {
                        if (bestPair == null || bestPair.bestCompleteV < bestCompleteV2) {
                            bestPair = new Pair(itemIndex, spaceIndex, bestCompleteV2, false, nextPlaceItemList, nextBlockList);
                        }
                    } else {
                        if (bestPair == null || bestPair.bestCompleteV < bestCompleteV1) {
                            bestPair = new Pair(itemIndex, spaceIndex, bestCompleteV1, true, nextPlaceItemList, nextBlockList);
                        }
                    }
                }
            }

            // 选择最好的对
            blockList = bestPair.blockList;
            placeItemList = bestPair.placeItemList;
            packItemInBin(items[bestPair.itemIndex], bin, bestPair.spaceIndex, bestPair.isV);
        }

        return bin;
    }

    private void dfsRemoveNode(Bin bin, TreeNode TreeNode) {
        if (TreeNode.type == -3) {
            bin.remainSpaceNodeList.remove(TreeNode);
        } else {
            if (!TreeNode.childNodeList.isEmpty()) {
                // 非叶子节点，先移除子节点，再移除当前节点
                for (TreeNode childNode : TreeNode.childNodeList) dfsRemoveNode(bin, childNode);
            }
            // 移除当前节点
            if (TreeNode.type == -1 || TreeNode.type == -2) {
                TreeNode.childNodeList.clear();
            } else {
                // 物品节点
                bin.totalV -= items[TreeNode.type].value;
                bin.usedCntArr[TreeNode.type]--;
            }
            TreeNode.type = -3;
            TreeNode.canRemoveNodeCnt = 0;
        }
    }

    private int dfsFindRemoveNode(int removeIndex, Bin bin, TreeNode TreeNode) {
        if (TreeNode.type != -3) {
            if (removeIndex == 0) {
                TreeNode parentNode = TreeNode.parentNode;
                while (parentNode != null) {
                    parentNode.canRemoveNodeCnt -= TreeNode.canRemoveNodeCnt;
                    parentNode = parentNode.parentNode;
                }
                dfsRemoveNode(bin, TreeNode);
                bin.remainSpaceNodeList.add(TreeNode);
                return -1;
            }
            removeIndex--;
        }
        for (TreeNode childNode : TreeNode.childNodeList) {
            removeIndex = dfsFindRemoveNode(removeIndex, bin, childNode);
            if (removeIndex == -1) return -1;
        }
        return removeIndex;
    }

    private Bin ruin(Bin bin) {
        int r = mu == 0 ? 0 : random.nextInt(2 * mu - 1) + 1;
        while (r > 0 && bin.rootNode.canRemoveNodeCnt > 0) {
            int removeIndex = random.nextInt(bin.rootNode.canRemoveNodeCnt);
            dfsFindRemoveNode(removeIndex, bin, bin.rootNode);
            r--;
        }
        return bin;
    }

    private void getPlaceItemListByNode(TreeNode TreeNode, List<PlaceItem> placeItemList) {
        switch (TreeNode.type) {
            case -1:
                // V
                for (TreeNode childNode : TreeNode.childNodeList) {
                    getPlaceItemListByNode(childNode, placeItemList);
                }
                break;
            case -2:
                // H
                for (TreeNode childNode : TreeNode.childNodeList) {
                    getPlaceItemListByNode(childNode, placeItemList);
                }
                break;
            case -3:
                break;
            default:
                Item item = items[TreeNode.type];
                PlaceItem placeItem = new PlaceItem(item.id, item.index, TreeNode.x, TreeNode.y, item.w, item.h, item.s);
                placeItemList.add(placeItem);
                break;
        }
    }

    private void treeSearch(int iterCnt, List<Block> blockList) {
        // 将当前最优解转化为树结构
        Bin binStar = new Bin();
        binStar.usedCntArr = new short[m];
        binStar.rootNode = new TreeNode();
        binStar.rootNode.type = -3;
        binStar.rootNode.W = W;
        binStar.rootNode.H = H;
        getNodeByPlaceItemList(binStar.rootNode, bestPlaceItemList, binStar);

        for (int i = 0; i < iterCnt; i++) {
            Bin newBin = ruin(new Bin(binStar));
            newBin = repair(newBin, blockList);
            if (newBin.totalV > binStar.totalV) {
                binStar = newBin;
                if (newBin.totalV > LB) {
                    LB = newBin.totalV;
                    bestPlaceItemList = new ArrayList<>(n);
                    getPlaceItemListByNode(newBin.rootNode, bestPlaceItemList);
                    if (LB == UB) throw new TerminationException();
                }
            }
        }
    }

    public List<PlaceItem> bestPlaceItemList;
    public int m, n, W, H, S, LB, UB, mu;
    public int[] dpV, dpX, dpY;
    public int[][] u;
    public Item[] items;
    public long timer, timeLimit;
    public Random random;

    public Bidirectional_TS_Solver(int m, int n, int W, int H, int S, Item[] items, Random random, int initUB, int initLB, List<PlaceItem> initPlaceItemList, int[] dpV, int[] dpX, int[] dpY, int[][] u, long timeLimit) {
        this.m = m;
        this.n = n;
        this.W = W;
        this.H = H;
        this.S = S;
        this.u = u;
        this.items = items;
        this.random = random;
        this.timeLimit = timeLimit;
        this.dpV = dpV;
        this.dpX = dpX;
        this.dpY = dpY;
        LB = initLB;
        UB = initUB;
        bestPlaceItemList = initPlaceItemList;
        this.alpha = Parameter.Alpha;
        if (n <= 100) {
            mu = 8;
        } else if (n <= 300) {
            mu = 6;
        } else if (n <= 500) {
            mu = 4;
        } else {
            mu = 2;
        }
    }

    public void solve() {
        timer = System.currentTimeMillis();
        // 开始搜索
        int lastMinV = UB;
        int maxBlockCnt = m;

        boolean FOCUS_BOTTOM_UP = n <= 30 && n / m < 3;
        FOCUS_BOTTOM_UP |= (n > 120 && n / m >= 3);
        int beamSize = FOCUS_BOTTOM_UP ? m : 1;

        while (!isTimeLimit() && LB < UB) {
            try {
                List<Block> blockList = new ArrayList<>();
                int ub = lastMinV, lb = LB + 1;
                while (ub >= lb) {
                    int minV = (lb + ub) / 2;
                    List<Block> temp = grouping(maxBlockCnt, minV); // 注意，这里可能导致LB提升
                    if (temp.size() >= maxBlockCnt) {
                        lastMinV = minV;
                        lb = minV + 1;
                        blockList = temp;
                    } else {
                        if (minV <= LB) {
                            UB = LB;
                            throw new TerminationException();
                        }
                        UB = minV;
                        ub = minV - 1;
                    }
                    lb = Math.max(lb, LB + 1);
                }

                // 如果生成的块数量小于所给参数，证明最优
                if (blockList.size() < maxBlockCnt) {
                    UB = LB;
                    break;
                }
                // 保留前 maxCnt 个块
                blockList.sort((o1, o2) -> {
                    int c = -Integer.compare(o1.value, o2.value);
                    if (c == 0) c = Integer.compare(o1.s, o2.s);
                    if (c == 0) c = Integer.compare(o1.h, o2.h);
                    if (c == 0) c = Integer.compare(o1.w, o2.w);
                    return c;
                });
                if (blockList.size() > maxBlockCnt) {
                    List<Block> newBlockList = new ArrayList<>(maxBlockCnt);
                    for (Block block : blockList) {
                        newBlockList.add(block);
                        if (newBlockList.size() == maxBlockCnt) {
                            blockList = newBlockList;
                            break;
                        }
                    }
                }

                // 搜索和后改进
                beamSearch(beamSize, blockList);
                treeSearch(beamSize, blockList);

                if (FOCUS_BOTTOM_UP) {
                    beamSize = Math.max(1, beamSize / 2);
                } else {
                    beamSize *= 2;
                }

                maxBlockCnt *= 2;
            } catch (TerminationException e) {
                break;
            }
        }

    }
}