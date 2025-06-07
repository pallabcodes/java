package com.pallab.algo.array;

import java.util.ArrayList;

public class Stack {
    public static void main(String[] args) {
        testStack();
    }

    ArrayList<Integer> stack = new ArrayList<Integer>();

    public Stack() {
    }

    public void push(int n) {
        stack.add(n);
    }

    public int pop() {
        return stack.remove(stack.size() - 1);
    }

    public int size() {
        return stack.size();
    }

    public static void testStack() {
        Stack stack = new Stack();
        stack.push(1);
        stack.push(2);
        System.out.println("Size: " + stack.size());
        System.out.println("Popped: " + stack.pop());
    }

    // https://leetcode.com/problems/baseball-game/
}





