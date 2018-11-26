package com.apps.elliotgrin.chudnovsky;

import java.util.Date;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("Enter k:");
        Scanner scanner = new Scanner(System.in);
        Long k = scanner.nextLong();
        Date start = new Date();
        String pi = ChudnovskyAlgorithm.calculatePi(k).toString(true);
        Double time = (new Date().getTime() - start.getTime()) / 1000.0;
        System.out.println("Pi = " + pi + ";\nTime = " + time.toString() + " s.");
    }

}
