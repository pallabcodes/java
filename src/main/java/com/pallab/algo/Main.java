package com.pallab.algo;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Algorithm Practice in Java");
        new Main().usingScanner();
    }

    private boolean usingScanner() {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Enter a string:");
            String str = sc.nextLine();
            System.out.println(str);

            System.out.println("Enter number of skills:");
            int skills = sc.nextInt();
            System.out.println("I have these " + skills + " skills");

            sc.nextLine(); // consume newline

            System.out.println("Enter favorite food:");
            String food = sc.nextLine();
            System.out.println("favourite food is " + food);
            return true;
        }
    }
}