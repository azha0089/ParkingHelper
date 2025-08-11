package com.example.infomanagesystem.utils;

import org.springframework.util.DigestUtils;

public class MD5Util {
    
    public static String encrypt(String input) {
        return DigestUtils.md5DigestAsHex(input.getBytes());
    }
}