package collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
 * Stream是一个高级的Iterator，构成由: 获取数据源-转换-执行操作三步构成。操作类型有:
 * 
 * Intermediate: 可以跟0...n个，主要为打开流，执行一些数据映射/过滤，返回一个新的流。
 * 这些操作都是懒加载的,调用方法时并没有开始遍历流。主要有: map、filter、 distinct、
 * sorted、 peek、 limit、 skip、 parallel、 sequential、 unordered
 * 
 * Terminal: 只能跟1个，执行完之后流就不能再使用了，因此是最后一个操作。最终产生结果，
 * 有副作用(side effect)。主要有forEach、 forEachOrdered、 toArray、 reduce、 
 * collect、 min、 max、 count、 anyMatch、 allMatch、 noneMatch、 findFirst、 
 * findAny、 iterator
 * 
 * Short-circuiting：如果接收流一个无限大的Stream，
 * 对于intermediate操作，返回一个有限的新Stream。对于terminal操作，则能在有限时间内输出结果。
 * 主要有anyMatch、 allMatch、 noneMatch、 findFirst、 findAny、 limit
 * 
 */
public class StreamsTest {

	public static void main(String[] args) {
		System.out.println("----------创建Stream----------");
		createStreams();
		System.out.println("----------操作Stream----------");
		handleStream();
		collectorsReduction();
	}
	
	//创建Stream
	public static void createStreams() {
		//通过静态方法构造
		Stream<String> s1 = Stream.of("a", "b", "c");
		//通过数组来构造
		String[] strArray = new String[] {"a", "b", "c"};
		Stream<String> s2 = Stream.of(strArray);
		Stream<String> s2_1 = Arrays.stream(strArray);
		//通过列表来构造
		Stream<String> s3 = Arrays.asList("a", "b", "c").stream();
		
		//内置三种基本数据类型Stream：IntStream、LongStream、DoubleStream
		IntStream.of(1,2,3).forEach(i -> System.out.print(i + " "));
		System.out.println();
		
		//接收一个区间来构建
		IntStream.range(1, 3).forEach(i -> System.out.print(i + " "));
		System.out.println();
		//接收一个闭合区间来构建
		IntStream.rangeClosed(1, 3).forEach(i -> System.out.print(i + " "));
		System.out.println();
		
		//将流转换为其他的数据结构
		//转换为数组
		String[] strArray1 = s1.toArray(String[]::new);
		System.out.println(strArray1.length);
		//转换为集合
		List<String> list1 = s2.collect(Collectors.toList());
		List<String> list2 = s2_1.collect(Collectors.toCollection(ArrayList::new));
		System.out.println(list1.equals(list2));
		//转换为String
		String str = s3.collect(Collectors.joining()).toString();
		System.out.println(str);
		
		System.out.println("----------generate----------");
		//通过实现Supplier接口,将其传入到Stream.generate()生成流
		Random seed = new Random();
		Supplier<Integer> random = seed::nextInt;
		Stream.generate(random).limit(10).forEach(System.out::println);
		//or
		IntStream.generate(() -> (int) (System.nanoTime() % 100))
			.limit(10).forEach(System.out::println);
		
		System.out.println("----------iterate----------");
		//iterate接收一个种子值与一个函数，然后通过不断的调用函数而生成流
		//例如第一个元素是种子值seed,第二个元素就是f(seed)，第三个就是f(f(seed))...
		Stream.iterate(2, n -> n*n).limit(5).forEach(System.out::println);
	}
	
	//操作Stream
	public static void handleStream() {
		//map将流中每个元素都执行一个函数
		//这里将每个字母都转换成大写
		System.out.println("----------map----------");
		Stream.of("a", "b").map(String::toUpperCase).forEach(System.out::println);
		//平方
		Stream.of(1, 2).map(i -> i * i).forEach(System.out::println);
		System.out.println("----------flatMap----------");
		//flatMap可以将Stream层级结构扁平化
		Stream<List<Integer>> intStream = Stream.of(
		    Arrays.asList(1),
		    Arrays.asList(2, 3),
		    Arrays.asList(4, 5, 6)
		);
		intStream.flatMap(i -> i.stream()).forEach(System.out::println);
		System.out.println("----------filter----------");
		//filter能够过滤元素并且生成一个新的Stream
		IntStream.range(1, 10).filter(i -> i > 5).forEach(System.out::println);
		
		//forEach负责消费元素，Stream被消费掉后就无法再使用了。
		//peek可以实现类似的效果并且是Intermediate操作
		System.out.println("----------peek----------");
		IntStream.range(1, 10).filter(i -> i > 5)
			.peek(i -> System.out.print(i + " "))
			.map(i -> i*i).peek(i -> System.out.println(i))
			.toArray();
		
		//reduce可以把Stream的元素组合起来，如果指定了一个初始值可以直接读取数据，
		//没有的话则返回的是一个Optional数据
		System.out.println("----------reduce----------");
		int sum1 = IntStream.range(1, 100).reduce(0, (a,b) -> a+b);
		int sum2 = IntStream.range(1, 100).reduce(Integer::sum).getAsInt();
		System.out.println(sum1 == sum2);
		
		System.out.println("----------limit/skip----------");
		//limit返回Stream的前n个元素；skip则是扔掉前n个元素
		IntStream.range(1, 10)
			.limit(5).peek(i -> System.out.print("limit: " + i + " "))
			.skip(3).peek(i -> System.out.print("skip: " + i + " "))
			.toArray();
		
		//排序,最好是放到Intermediate操作最后一位
		System.out.println("\n----------sorted----------");
		new Random().ints(10, 1, 100).sorted().forEach(System.out::println);
		
		System.out.println("min: " + IntStream.range(1, 10).min().getAsInt());
		System.out.println("max: " + IntStream.range(1, 10).max().getAsInt());
		System.out.println("----------distinct----------");
		Stream.of(1, 2, 2, 3, 4).distinct().forEach(System.out::println);
		
		System.out.println("----------match----------");
		//allMatch要全部元素都符合条件才返回true，如果有一个元素不符合后边就完全skip
		boolean allOdd = Stream.of(1, 3, 5, 7, 9).allMatch(i -> (i%2 == 1));
		//noneMatch是没有一个元素符合条件就返回true
		boolean noneOdd = Stream.of(2, 4, 6, 8).noneMatch(i -> (i%2 == 1));
		//anyMatch只要有一个元素符合条件就返回true
		boolean anyOdd = new Random().ints(10, 1, 100).anyMatch(i -> (i%2 == 1));
		System.out.println("all: " + allOdd + 
				" none: " + noneOdd + " any: " + anyOdd);
	}
	
	//对集合一些归类分组操作
	public static void collectorsReduction() {
		class Person {
			private String name;
			private int age;
			
			public Person(String name, int age) {
				this.name = name;
				this.age = age;
			}
			public String getName() { return name; }
			public int getAge() { return age; }
		}
		
		List<Person> persons = Arrays.asList(
			new Person("a", 11),
			new Person("b", 11),
			new Person("a", 12),
			new Person("d", 13)
		);
		
		System.out.println("----------grouping by----------");
		//groupingBy操作可以对流中元素进行分组，得到的是一个Map<Key, List>集合,
		//Key的类型取决于分组方法返回的值的类型
		Map<String, List<Person>> map1 = persons.stream()
				.collect(Collectors.groupingBy(Person::getName));
		for (Map.Entry<String, List<Person>> m: map1.entrySet()) {
			System.out.println(m.getKey() + " " + m.getValue().size());
		}
		
		System.out.println("----------partitioning by----------");
		//partitioningBy接收一个条件，按条件真假来划分两组，因此返回的是Map<Bool, List>
		Map<Boolean, List<Person>> map2 = persons.stream()
				.collect(Collectors.partitioningBy(p -> p.getAge() > 11));
		System.out.println(map2.get(true).size() + " " + map2.get(false).size());
		
	}

}
