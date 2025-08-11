package com.example.infomanagesystem.test;

import com.example.infomanagesystem.utils.MD5Util;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {

        String pwd="123";
        String encoded = MD5Util.encrypt(pwd);
        System.out.println(encoded);
    }
}

class Point {
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}