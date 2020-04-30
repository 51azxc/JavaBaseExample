package algorithm;

import java.util.function.Function;

public class HappyNumber {
    public boolean isHappy(int n) {
        if (n <= 0) return false;
        if (n == 1) return true;
        Function<Integer, Integer> func = (x) -> Integer.toString(x).chars()
                .parallel().map(c -> c - '0').map(i -> i*i).reduce(0, (a, b) -> a + b);
        int result = func.apply(n);
        while (result != 1) {
            result = func.apply(result);
            if (result < 10 && result > 1) return false;
        }
        return true;
    }
    public static void main(String[] args) {
        HappyNumber obj = new HappyNumber();
        System.out.println(obj.isHappy(9));
    }
}
