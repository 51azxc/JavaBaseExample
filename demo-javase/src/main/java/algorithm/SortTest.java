package algorithm;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class SortTest {

	public static void main(String[] args) {
		int[] list = IntStream.generate(() -> ThreadLocalRandom.current().nextInt(1, 100)).limit(10).toArray();
		System.out.print("原始数组: ");
        Arrays.stream(list).forEach(i -> System.out.print(i + " "));
        System.out.println();
        System.out.print("插入排序: ");
        Arrays.stream(insertSort(list)).forEach(i -> System.out.print(i + " "));
        System.out.println("\t时间复杂度 平均: O(n^2) 最好: O(n) 最坏: O(n^2) 稳定");
        System.out.print("希尔排序: ");
        Arrays.stream(shellSort(list)).forEach(i -> System.out.print(i + " "));
        System.out.println("\t时间复杂度 平均:? 最好: O(n) 最坏: O(n^2) 不稳定");
        System.out.print("选择排序: ");
        Arrays.stream(selectionSort(list)).forEach(i -> System.out.print(i + " "));
        System.out.println("\t时间复杂度 平均: O(n^2) 最好: O(n^2) 最坏: O(n^2) 不稳定");
        System.out.print("冒泡排序: ");
        Arrays.stream(bubbleSort(list)).forEach(i -> System.out.print(i + " "));
        System.out.println("\t时间复杂度 平均: O(n^2) 最好: O(n) 最坏: O(n^2) 稳定");
        System.out.print("快速排序: ");
        Arrays.stream(quickSort(list)).forEach(i -> System.out.print(i + " "));
        System.out.println("\t时间复杂度 平均: O(nlog2n) 最好: O(nlog2n) 最坏: O(n^2) 不稳定");
        System.out.print("归并排序: ");
        Arrays.stream(mergeSort(list)).forEach(i -> System.out.print(i + " "));
        System.out.println("\t时间复杂度 平均: O(nlog2n) 最好: O(nlog2n) 最坏: O(nlog2n) 稳定");
	}
	
    public static int[] insertSort(int[] list) {
        for (int i = 1; i < list.length; i++) {
            for (int j = i; j > 0; j--) {
                if (list[j] < list[j-1]) {
                    int temp = list[j-1];
                    list[j-1] = list[j];
                    list[j] = temp;
                }
            }
        }
        return list;
    }
    
    public static int[] shellSort(int[] list) {
        int divide = list.length / 2;
        while (divide >= 1) {
            for (int i = 0; i < list.length; i++) {
                int secondLength = list.length - divide;
                for (int j = i; j < secondLength; j += divide) {
                    int k = j + divide;
                    if (list[j] > list[k]) {
                        int temp = list[j];
                        list[j] = list[k];
                        list[k] = temp;
                    }
                }
            }
            divide /= 2;
        }
        return list;
    }
    
    public static int[] selectionSort(int[] list) {
        int pos = 0;
        for (int i = 0; i < list.length; i++) {
            pos = i;
            int temp = list[i];
            for (int j = i+1; j < list.length; j++) {
                if (list[j] < temp) {
                    temp = list[j];
                    pos = j;
                }
            }
            list[pos] = list[i];
            list[i] = temp;
        }
        return list;
    }
    
    public static int[] bubbleSort(int[] list) {
        for (int i = 0; i < list.length-1; i++) {
            for (int j = 0; j < list.length-1-i; j++) {
                if (list[j] > list[j+1]) {
                    int temp = list[j+1];
                    list[j+1] = list[j];
                    list[j] = temp;
                }
            }
        }
        return list;
    }
    
    public static int[] quickSort(int[] list) {
        quickSort2(list, 0, list.length - 1);
        return list;
    }
    public static void quickSort1(int[] list, int low, int high) {
        if (low < high) {
            int temp = list[high];
            int i = low - 1;
            for (int j = low; j < high; j++) {
                if (list[j] <= temp) {
                    i++;
                    int tmp = list[j];
                    list[j] = list[i];
                    list[i] = tmp;
                }
            }
            list[high] = list[i+1];
            list[i+1] = temp;
            quickSort1(list, low, i);
            quickSort1(list, i+1, high);
        }
    }
    
    public static void quickSort2(int[] list, int low, int high) {
    	if (low < high) {
    		int middle = getMiddle(list, low, high);
    		quickSort2(list, low, middle-1);
    		quickSort2(list, middle+1, high);
    	}
    }
    
    public static int getMiddle(int[] list, int low, int high) {
    	int temp = list[low];
    	while (low < high) {
    		while (low < high && list[high] >= temp ) {
    			high --;
    		}
    		list[low] = list[high];
    		while (low < high && list[low] <= temp) {
    			low ++;
    		}
    		list[high] = list[low];
    	}
    	list[low] = temp;
    	return low;
    }
    
    public static int[] mergeSort(int[] list) {
        return mergeSort(list, 0, list.length - 1);
    }
    
    public static int[] mergeSort(int[] list, int left, int right) {
        if (left < right) {
            int middle = (left + right) / 2;
            mergeSort(list, left, middle);
            mergeSort(list, middle+1, right);
            merge(list, left, middle, right);
        }
        return list;
    }
    
    public static void merge(int[] list, int left, int middle, int right) {
        int[] temp = new int[list.length];
        int center = middle + 1;
        int index = left;
        int tmp = left;
        while (left <= middle && center <= right) {
            if (list[left] <= list[center]) {
                temp[index++] = list[left++];
            } else {
                temp[index++] = list[center++];
            }
        }
        while(center <= right) {
            temp[index++] = list[center++];
        }
        while(left <= middle) {
            temp[index++] = list[left++];
        }
        while(tmp <= right) {
            list[tmp] = temp[tmp++];
        }
    }

}
