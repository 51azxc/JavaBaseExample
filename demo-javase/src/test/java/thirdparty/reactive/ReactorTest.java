package thirdparty.reactive;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

public class ReactorTest {
	
	@Test
	public void testFluxCreate1() {
		System.out.println("---just---");
		Flux.just("Hello", "World").subscribe(r -> System.out.print(r + " "));
		System.out.println("\n---from array---");
		Flux.fromArray(new Integer[] { 1, 2, 3 }).subscribe(System.out::print);
		System.out.println("\n---empty---");
		Flux.empty().subscribe(System.out::println);
		System.out.println("---range---");
		Flux.range(1, 10).subscribe(System.out::print);
		System.out.println("\n---interval---");
		Flux.interval(Duration.of(10, ChronoUnit.SECONDS)).subscribe(System.out::println);
	}

	@Test
	public void testFluxCreate2() {
		Flux.generate(sink -> {
			sink.next("Hello");
			sink.complete();
		}).subscribe(System.out::println);

		final Random random = new Random();
		Flux.generate(ArrayList::new, (list, sink) -> {
			int value = random.nextInt(100);
			list.add(value);
			sink.next(list);
			if (list.size() == 10) {
				sink.complete();
			}
			return list;
		}).subscribe(System.out::println);

		Flux.create(sink -> {
			for (int i = 0; i < 10; i++) {
				sink.next(i);
			}
			sink.complete();
		}).subscribe(s -> System.out.print(s + " "));
	}

	@Test
	public void testMonoCreate() {
		Mono.fromSupplier(() -> "Hello").subscribe(System.out::println);
		Mono.justOrEmpty(Optional.of("Hello")).subscribe(System.out::println);
		Mono.create(sink -> sink.success("Hello")).subscribe(System.out::println);
	}

	@Test
	public void testFluxOperator1() {
		Flux.range(1, 100).buffer(20).subscribe(System.out::println);
		Flux.range(1, 10).bufferUntil(i -> i % 2 == 0).subscribe(System.out::println);
		Flux.range(1, 10).bufferWhile(i -> i % 2 == 0).subscribe(System.out::println);
		Flux.range(1, 10).filter(i -> i % 2 == 0).subscribe(System.out::println);

		Flux.range(1, 100).window(20).take(2).toStream().forEach(System.out::println);
	}

	@Test
	public void testFluxOperator2() {
		Flux.just("a", "b").zipWith(Flux.just("c", "d")).subscribe(System.out::println);
		Flux.just("a", "b").zipWith(Flux.just("c", "d"), (s1, s2) -> String.format("%s-%s", s1, s2))
				.subscribe(System.out::println);

		Flux.range(1, 10).take(1).subscribe(System.out::println);
		Flux.range(1, 10).takeLast(1).subscribe(System.out::println);
		Flux.range(1, 10).takeWhile(i -> i < 4).subscribe(System.out::print);
		System.out.println("\n----");
		Flux.range(1, 10).takeUntil(i -> i < 4).subscribe(System.out::print);
		System.out.println("\n----");
		Flux.range(1, 100).reduce((x, y) -> x + y).subscribe(System.out::println);
		Flux.range(1, 100).reduceWith(() -> 100, (x, y) -> x + y).subscribe(System.out::println);
	}

	@Test
	public void testFluxOperator3() {
		// Flux.just(5,10).flatMap(x -> Flux.interval(Duration.of(x,
		// ChronoUnit.SECONDS)).take(x)).toStream().forEach(System.out::println);
		Flux.just(1, 2).concatWith(Mono.error(new IllegalStateException())).retry(1).onErrorReturn(0)
				.subscribe(System.out::println, System.err::println);

		Flux.create(sink -> {
			sink.next(Thread.currentThread().getName());
			sink.complete();
		}).publishOn(Schedulers.single()).map(x -> String.format("[%s] %s", Thread.currentThread().getName(), x))
				.publishOn(Schedulers.elastic()).map(x -> String.format("[%s] %s", Thread.currentThread().getName(), x))
				.subscribeOn(Schedulers.parallel()).toStream().forEach(System.out::println);

		StepVerifier.create(Flux.just("a", "b")).expectNext("a").expectNext("b").verifyComplete();

	}

	private static List<String> words = Arrays.asList("the", "quick", "brown", "fox", "jumped", "over", "the", "lazy",
			"dog");

	@Test
	public void testReactor1() {
		Flux<String> fewWords = Flux.just("Hello", "World");
		Flux<String> manyWords = Flux.fromIterable(words);

		fewWords.subscribe(System.out::println);
		System.out.println();
		manyWords.subscribe(System.out::println);
	}

	@Test
	public void testReactor2() {
		Flux.fromIterable(words).flatMap(word -> Flux.fromArray(word.split(""))).concatWith(Mono.just("s")).distinct()
				.sort()
				.zipWith(Flux.range(1, Integer.MAX_VALUE), (string, count) -> String.format("%2d. %s", count, string))
				.subscribe(System.out::println);
	}

	@Test
	public void testReactor3() {
		Flux.range(1, 2).log("Range").subscribe(System.out::println);

		StepVerifier.withVirtualTime(() -> Flux.interval(Duration.ofHours(4), Duration.ofDays(1)).take(2))
				.expectSubscription().expectNoEvent(Duration.ofHours(4)).expectNext(0L).thenAwait(Duration.ofDays(1))
				.expectNext(1L).verifyComplete();

	}

	@Test
	public void testReactor4() {
		StepVerifier.create(Flux.just(1, 3, 5, 2, 4, 6, 11, 12, 13).groupBy(i -> i % 2 == 0 ? "even" : "odd")
				.concatMap(g -> g.defaultIfEmpty(-1) // if empty groups, show them
						.map(String::valueOf) // map to string
						.startWith(g.key())) // start with the group's key
		).expectNext("odd", "1", "3", "5", "11", "13").expectNext("even", "2", "4", "6", "12").verifyComplete();
		// Flux.range(1, 10).windowWhile(i -> i % 2 ==
		// 0).concatMap(g->g.defaultIfEmpty(-1)).subscribe(System.out::println);

		StepVerifier.create(Flux.range(1, 10).window(5, 3) // overlapping windows
				.concatMap(g -> g.defaultIfEmpty(-1)) // show empty windows as -1
		).expectNext(1, 2, 3, 4, 5).expectNext(4, 5, 6, 7, 8).expectNext(7, 8, 9, 10).expectNext(10).verifyComplete();

		StepVerifier.create(Flux.range(1, 10).buffer(5, 3) // overlapping buffers
		).expectNext(Arrays.asList(1, 2, 3, 4, 5)).expectNext(Arrays.asList(4, 5, 6, 7, 8))
				.expectNext(Arrays.asList(7, 8, 9, 10)).expectNext(Collections.singletonList(10)).verifyComplete();

		Flux.range(1, 10).parallel(2).subscribe(i -> System.out.println(Thread.currentThread().getName() + " -> " + i));

		Flux.range(1, 10).parallel(2).runOn(Schedulers.parallel())
				.subscribe(i -> System.out.println(Thread.currentThread().getName() + " -> " + i));
	}
}
