package algorithm;
/*
 * 取（0-9）*2共18各数字，使得等式xxx*yyy*zzz = aaaaa成立
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiplicationTest {

	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		final List<Integer> list1 = IntStream.range(101, 999).filter(l->l%111!=0)
				.boxed().sorted().collect(Collectors.toList());
		final List<Integer> list2 = IntStream.concat(IntStream.range(0, 10), IntStream.range(0, 10))
				.boxed().sorted().collect(Collectors.toList());
		list1.stream().anyMatch(a -> {
			List<Integer> result = list1.stream().filter(b -> a < b && a * b < 99888)
				.map(b -> {
					int c = a * b;
					List<Integer> bList = splitNumber(b);
					return Arrays.asList(a, b, a*bList.get(2), a*bList.get(1), a*bList.get(0), c);
				}).filter(c -> {
					List<Integer> list3 = c.stream().flatMap(d -> splitNumber(d).stream())
							.sorted().collect(Collectors.toList());
					return list3.equals(list2);
				}).flatMap(e -> e.stream()).collect(Collectors.toList());
			if (!result.isEmpty()) {
				result.stream().forEach(r -> System.out.print(r + " "));
				System.out.println();
			}
			return !result.isEmpty();
		});
		long t2 = System.currentTimeMillis();
		System.out.println("cost: " + (t2-t1));
	}
	
	public static List<Integer> splitNumber(int num) {
		List<Integer> list = new ArrayList<>();
		while (num > 0) {
			list.add(num % 10);
			num /= 10;
		}
		return list;
	}

}
