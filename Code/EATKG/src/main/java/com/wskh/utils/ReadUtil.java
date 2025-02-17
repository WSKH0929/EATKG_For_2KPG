package com.wskh.utils;

import com.wskh.classes.Instance;
import com.wskh.classes.Item;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class ReadUtil {

    public static Instance readInstance(String instancePath) throws IOException {
        Instance instance = new Instance();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(instancePath));
        String input;
        int row = 0;
        int itemId = 0;
        while ((input = bufferedReader.readLine()) != null) {
            String[] split = input.split(" ");
            if (row == 0) {
                instance.m = Integer.parseInt(input);
                instance.items = new Item[instance.m];
            } else if (row == 1) {
                instance.n = Integer.parseInt(split[0]);
            } else if (row == 2) {
                instance.W = Integer.parseInt(split[0]);
                instance.H = Integer.parseInt(split[1]);
                instance.S = instance.W * instance.H;
            } else if (row > 2) {
                int w = Integer.parseInt(split[0]);
                int h = Integer.parseInt(split[1]);
                int s = w * h;
                int v = Integer.parseInt(split[2]);
                int num = Integer.parseInt(split[3]);
                instance.items[itemId] = new Item(itemId, itemId, w, h, s, v, num);
                itemId++;
            }
            row++;
        }
        bufferedReader.close();
        Arrays.sort(instance.items, (o1, o2) -> {
            int c = -Double.compare((double) o1.value / o1.s, (double) o2.value / o2.s);
            if (c == 0) c = -Integer.compare(o1.value, o2.value);
            if (c == 0)
                c = -Double.compare(Math.max((double) o1.w / instance.W, (double) o1.h / instance.H), Math.max((double) o2.w / instance.W, (double) o2.h / instance.H));
            return c;
        });
        for (int i = 0; i < instance.items.length; i++) instance.items[i].index = i;
        return instance;
    }

}