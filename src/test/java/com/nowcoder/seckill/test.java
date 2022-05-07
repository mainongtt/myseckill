package com.nowcoder.seckill;

import java.util.Scanner;

/**
 * @author zhang kun
 * @Classname test
 * @Description TODO
 * @Date 2021/6/1 16:13
 */
public class test {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()){
            String[] s = new String[6];
            s[0] = sc.nextLine();
            s[1] = sc.nextLine();
            s[2] = sc.nextLine();
            s[3] = sc.nextLine();
            s[4] = sc.nextLine();
            s[5] = sc.nextLine();

            for(int i = 0;i<6;i++){
                System.out.println(s[i] + " ");
            }
        }
    }
}
