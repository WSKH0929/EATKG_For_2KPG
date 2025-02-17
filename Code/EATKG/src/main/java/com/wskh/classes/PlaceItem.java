package com.wskh.classes;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PlaceItem {
    public int id, index, x, y, w, h, s;

    public PlaceItem copy() {
        return new PlaceItem(id, index, x, y, w, h, s);
    }

}