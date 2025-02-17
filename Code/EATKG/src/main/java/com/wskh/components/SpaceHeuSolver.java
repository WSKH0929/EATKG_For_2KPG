package com.wskh.components;

import com.wskh.classes.Space;

import java.util.Collection;

public class SpaceHeuSolver {
    public static void createNewSpaceByAlpha(Collection<Space> spaces, Space space, double alpha, int w, int h) {
        int newW = space.w - w;
        int newH = space.h - h;

        Space space1 = new Space(space.x, space.y + h, w, newH);
        Space space2 = new Space(space.x + w, space.y, newW, space.h);

        Space space3 = new Space(space.x, space.y + h, space.w, newH);
        Space space4 = new Space(space.x + w, space.y, newW, h);

        int s1 = space1.w * space1.h;
        int s2 = space2.w * space2.h;
        int s3 = space3.w * space3.h;
        int s4 = space4.w * space4.h;

        boolean isV = Math.pow(s1, alpha) + Math.pow(s2, alpha) > Math.pow(s3, alpha) + Math.pow(s4, alpha);

        if (isV) {
            if (s1 > s2) {
                if (s1 > 0) spaces.add(space1);
                if (s2 > 0) spaces.add(space2);
            } else {
                if (s2 > 0) spaces.add(space2);
                if (s1 > 0) spaces.add(space1);
            }
        } else {
            if (s3 > s4) {
                if (s3 > 0) spaces.add(space3);
                if (s4 > 0) spaces.add(space4);
            } else {
                if (s4 > 0) spaces.add(space4);
                if (s3 > 0) spaces.add(space3);
            }
        }
    }
}