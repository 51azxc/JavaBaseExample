package thirdparty;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * 反应式流（Reactive Streams）
 * 
 * Flux 和 Mono 是 Reactor 中的两个基本概念。
 * 
 * Flux表示的是包含 0 到 N 个元素的异步序列。在该序列中可以包含三种不同类型的消息通知：
 * 正常的包含元素的消息、序列结束的消息和序列出错的消息。
 * 当消息通知产生时，订阅者中对应的方法 onNext(), onComplete()和 onError()会被调用。
 * 
 * Mono 表示的是包含 0 或者 1 个元素的异步序列。该序列中同样可以包含与 Flux 相同的三种类型的消息通知。
 * Flux 和 Mono 之间可以进行转换。对一个 Flux 序列进行计数操作，得到的结果是一个 Mono<Long>对象。
 * 把两个 Mono 序列合并在一起，得到的是一个 Flux 对象。
 * 
 */
public class ReactorTest {

	public static void main(String[] args) {
		//System.out.println("----------创建Flux----------");
		//createFlux();
		//System.out.println("----------创建Mono----------");
		//createMono();
		System.out.println("----------操作Flux----------");
		operateFlux();
	}

	//创建Flux
	public static void createFlux() {
		System.out.println("----------flux just----------");
		//简单的序列使用静态方法创建
		//just()方法指定序列中包含的全部元素。创建出来的 Flux 序列在发布这些元素之后会自动结束。
		Flux.just("Hello", "World").subscribe(i -> System.out.print(i + " "));
		System.out.println("\n----------flux fromArray----------");
		//fromArray()/fromIterable()/fromStream()方法可以从数组/迭代对象/Stream中创建
		Flux.fromArray(new Integer[] {1, 2, 3}).subscribe(i -> System.out.print(i + " "));
		System.out.println("\n----------flux empty----------");
		//empty()方法创建一个不包含任何元素，只发布结束消息的序列。
		Flux.empty().subscribe(System.out::println);
		System.out.println("\n----------flux range----------");
		//range(start, count)方法创建包含从 start起始的 count个数量的 Integer对象的序列。
		Flux.range(1, 10).subscribe(i -> System.out.print(i + " "));
		System.out.println("\n----------flux interval----------");
		//interval(period)方法创建一个包含了从 0 开始递增的Long对象的序列。其中包含的元素按照指定的间隔来发布。
		//interval(delay, period)方法创建一个除了间隔时间之外，还可以指定起始元素发布之前的延迟时间的递增序列。
		Flux.interval(Duration.of(1, ChronoUnit.SECONDS)).subscribe(i -> System.out.print(i + " "));
		
		System.out.println("\n----------flux generate----------");
		//generate()方法通过同步和逐一的方式来产生Flux序列。
		//序列的产生是通过调用所提供的SynchronousSink对象的 next()/complete()/error(Throwable)方法来完成的。
		Flux.generate(sink -> {
			//next()方法只能调用一次
		    sink.next("Hello");
		    //complete()方法结束序列，不然会一直生产下去
		    sink.complete();
		}).subscribe(System.out::println);
		
		//生成有状态的对象
		final Random random = new Random();
		//生成一个包含10个随机数的ArrayList数组
		Flux.generate(ArrayList::new, (list, sink) -> {
		    int value = random.nextInt(100);
		    list.add(value);
		    sink.next(value);
		    if (list.size() == 10) {
		        sink.complete();
		    }
		    return list;
		}).subscribe(i -> System.out.print(i + " "));
		
		System.out.println("\n----------flux create----------");
		//create()方法与 generate()方法的不同之处在于所使用的是FluxSink对象。
		//FluxSink支持同步和异步的消息产生，并且可以在一次调用中产生多个元素。
		Flux.create(sink -> {
		    for (int i = 0; i < 10; i++) {
		        sink.next(i);
		    }
		    sink.complete();
		}).subscribe(i -> System.out.print(i + " "));
		System.out.println();
	}
	
	//创建Mono
	public static void createMono() {
		//从Callable(fromCallable()) | CompletionStage(fromCompletionStage()) | 
		// CompletableFuture(fromFuture()) | Runnable(fromRunnable()) | Supplier(fromSupplier())中创建 Mono
		Mono.fromCompletionStage(CompletableFuture.completedFuture("Hello")).subscribe(System.out::println);
		Mono.fromFuture(CompletableFuture.supplyAsync(()->"World")).subscribe(System.out::println);
		Mono.fromSupplier(()->"supplier").subscribe(System.out::println);
		
		//delay(duration): 创建一个 Mono序列，在指定的延迟时间之后，产生数字0作为唯一值。
		Mono.delay(Duration.ofMillis(10)).subscribe(System.out::println);
		
		//justOrEmpty(T data)：从一个 Optional 对象或可能为 null 的对象中创建 Mono。
		//只有 Optional 对象中包含值或对象不为null时，Mono序列才产生对应的元素。
		Mono.justOrEmpty(Optional.of("justOrEmpty")).subscribe(System.out::println);
		
		//通过create()方法使用 MonoSink来创建 Mono
		Mono.create(sink -> sink.success("create")).subscribe(System.out::println);
	}
	
	//操作Flux
	public static void operateFlux() {
		System.out.println("----------buffer----------");
		//buffer会将流中的元素收集到集合中，并把集合对象作为流中的新元素
		//buffer()方法把流中10个数据元素分成3个数组
		Flux.range(1, 10).buffer(3).subscribe(System.out::println);
		System.out.println("----------bufferUntil----------");
		//bufferUntil会一直存放元素直到条件为true。这里就是每次碰到偶数就分割一个数组到流中
		Flux.range(1, 10).bufferUntil(i -> i % 2 == 0).subscribe(System.out::println);
		System.out.println("----------bufferWhile----------");
		//bufferWhile是只当条件为true时就存放那个元素，这里就是只存放偶数的数组流
		Flux.range(1, 10).bufferWhile(i -> i % 2 == 0).subscribe(System.out::println);
		
		System.out.println("----------filter----------");
		//filter可以按条件过滤元素
		Flux.range(1, 10).filter(i -> i % 2 == 0).subscribe(System.out::println);
		
		System.out.println("----------window----------");
		//window与buffer不同的是，window是将流中元素收集到另外的Flux序列中，返回的是Flux<Flux<T>>
		//所以需要使用flatMap拍平它
		Flux.range(1, 10).window(2).flatMap(s -> s).subscribe(System.out::println);
		
		System.out.println("----------zipWith----------");
		//zipWith是将两个流中的元素合并成一个元素为元组的流。可以加入一个函数对合并的元素进行处理
		Flux.just("Hello").zipWith(Flux.just("World")).subscribe(System.out::println);
		Flux.just("Hello").zipWith(Flux.just("World"), (s1, s2) -> s1+" "+s2).subscribe(System.out::println);
		
		System.out.println("----------take----------");
		//take()方法用来从当前流中提取元素
		Flux.range(1, 10).take(2).subscribe(System.out::println);
		System.out.println("----------takeLast----------");
		//takeLast(n)方法用来提取最后n个元素
		Flux.range(1, 10).takeLast(2).subscribe(System.out::println);
		System.out.println("----------takeUntil----------");
		//takeUntil()方法是提取元素直到条件为true
		Flux.range(1, 10).takeUntil(i -> i == 5).subscribe(System.out::println);
		System.out.println("----------takeWhile----------");
		//takeWhile()方法是当条件为true时才提取元素
		Flux.range(1, 10).takeWhile(i -> i < 5).subscribe(System.out::println);
		
		System.out.println("----------reduce----------");
		//reduce对元素进行操作，最终返回的是Mono
		Flux.range(1, 100).reduce((x, y) -> x + y).subscribe(System.out::println);
		//reduceWith会指定一个初始值
		Flux.range(1, 100).reduceWith(() -> 100, (x, y) -> x + y).subscribe(System.out::println);
		
		System.out.println("----------merge----------");
		//merge可以将两个流合并成一个流，顺序为元素实际产生的顺序
		Flux.merge(Flux.interval(Duration.ofMillis(10)).take(5), Flux.interval(Duration.ofMillis(20)).take(5))
			.toStream().forEach(i -> System.out.print(i + " "));
		System.out.println();
		//mergeSequential按照所有流被订阅的顺序，以流为单位进行合并
		Flux.mergeSequential(Flux.interval(Duration.ofMillis(10)).take(5), Flux.interval(Duration.ofMillis(20)).take(5))
			.toStream().forEach(i -> System.out.print(i + " "));
		
		System.out.println("\n----------flatMap----------");
		//flatMap是将流中的每个元素转换成一个流，然后再进行合并，类似拍平效果。
		Flux.just(5, 10).flatMap(x -> Flux.interval(Duration.ofMillis(10))
			.take(x)).toStream().forEach(i -> System.out.print(i + " "));
		
	}
}
