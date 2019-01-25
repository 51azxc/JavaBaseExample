package com.example.thirdparty.reactive;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

public class ReactorTest {
	
	@Test
	public void test1() {
		//使用StepVerifier验证流中的元素
		//expectNext()方法测试所期待的流中的下一个元素的值
		//verifyComplete()方法则验证流是否正常结束
		StepVerifier.create(Flux.just("a", "b"))
        	.expectNext("a")
        	.expectNext("b")
        	.verifyComplete();
		
		StepVerifier.create(Flux.just(1, 3, 5, 2, 4, 6, 11, 12, 13)
								.groupBy(i -> i % 2 == 0 ? "even" : "odd")
								.concatMap(g -> g.defaultIfEmpty(-1)
								.map(String::valueOf)
						        .startWith(g.key()))
		).expectNext("odd", "1", "3", "5", "11", "13")
		 .expectNext("even", "2", "4", "6", "12")
		 .verifyComplete();
		
		StepVerifier.create(Flux.range(1, 10).window(3, 5)
				.concatMap(g -> g.defaultIfEmpty(-1))
		).expectNext(1, 2, 3)
		 .expectNext(6, 7, 8)
		 .verifyComplete();

		StepVerifier.create(Flux.range(1, 10).buffer(5, 3))
			.expectNext(Arrays.asList(1, 2, 3, 4, 5))
			.expectNext(Arrays.asList(4, 5, 6, 7, 8))
			.expectNext(Arrays.asList(7, 8, 9, 10))
			.expectNext(Collections.singletonList(10))
			.verifyComplete();

	}
	
	@Test
	public void test2() {
		//操作测试时间
		//需要验证的流中包含两个产生间隔为一天的元素，并且第一个元素的产生延迟是4个小时
		//expectNoEvent()方法用来验证在 4 个小时之内没有任何消息产生
		//然后验证第一个元素 0 产生
		//接着thenAwait()方法来让虚拟时钟前进一天
		//然后验证第二个元素 1 产生
		//最后验证流正常结束
		StepVerifier.withVirtualTime(() -> 
				Flux.interval(Duration.ofHours(4), 
						Duration.ofDays(1)).take(2))
        	.expectSubscription()
        	.expectNoEvent(Duration.ofHours(4))
        	.expectNext(0L)
        	.thenAwait(Duration.ofDays(1))
        	.expectNext(1L)
        	.verifyComplete();
	}
	
	@Test
	public void test3() {
		//使用TestPublisher控制流中元素产生
		final TestPublisher<String> testPublisher = TestPublisher.create();
		testPublisher.next("a");
		testPublisher.next("b");
		testPublisher.complete();
		 
		StepVerifier.create(testPublisher)
			.expectNext("a")
		    .expectNext("b")
		    .expectComplete();
	}
}
