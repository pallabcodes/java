package com.pallab.algo.array;

public class ArrayOperations {
    public static int[] reverse(int[] arr) {
        if (arr == null || arr.length <= 1) return arr;
        
        int start = 0;
        int end = arr.length - 1;
        
        while (start < end) {
            int temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
        
        return arr;
    }
}