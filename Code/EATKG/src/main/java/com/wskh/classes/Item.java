package com.wskh.classes;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Item {
    public int id, index, w, h, s, value, num;

    public Item copy() {
        return new Item(id, index, w, h, s, value, num);
    }

    public static Item[] copy(Item[] items) {
        Item[] copy = new Item[items.length];
        for (int i = 0; i < copy.length; i++) copy[i] = items[i].copy();
        return copy;
    }
}