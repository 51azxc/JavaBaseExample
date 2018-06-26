package concurrent.future;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
 * CompletableFuture的一些简单示例
 */
public class CompletableFutureTest2 {

	public static void main(String[] args) {
		System.out.println("----------创建完整的CompletableFuture----------");
		test1();
		System.out.println("----------简单的异步操作----------");
		test2();
		System.out.println("----------同步转化future----------");
		test3();
		System.out.println("----------异步转化future----------");
		test4();
		System.out.println("----------使用自定义线程池----------");
		test5();
		System.out.println("----------同步消费----------");
		test6();
		System.out.println("----------异步消费----------");
		test7();
		System.out.println("----------取消任务----------");
		test8();
		System.out.println("----------从两个异步任务中接收其中一个最快返回的结果----------");
		test9();
		System.out.println("----------运行两个阶段后执行----------");
		test10();
		System.out.println("----------整合两个计算结果----------");
		test11();
		System.out.println("----------整合两个计算结果----------");
		test12();
	}
	
	//使用一个预定义的结果创建一个完整的CompletableFuture
	public static void test1() {
		CompletableFuture<String> future = CompletableFuture.completedFuture("hello");
		if (future.isDone()) {
			//如果future已经完成,getNow方法返回future返回值，否则就返回传入参数("world")
			System.out.println(future.getNow("world"));
		}
	}
	
	//使用runAsync()方法执行一个异步操作
	public static void test2() {
		//以Async结尾的方法皆为异步执行。异步通过ForkJoinPool实现，它使用守护线程去执行任务
		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
			System.out.println("daemon thread ? " + Thread.currentThread().isDaemon());
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		System.out.println("1. future is done? " + future.isDone());
		try {
			TimeUnit.MILLISECONDS.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("2. future is done ?" + future.isDone());
	}
	
	//使用thenApply()方法同步执行转化future
	public static void test3() {
		//then表示操作会等待之前的操作完成
		//Apply表示返回的阶段会对之前产生的结果进行一次操作
		//thenApply会被阻塞
		CompletableFuture<String> future = CompletableFuture.completedFuture("hello").thenApply(s -> s.toUpperCase());
		System.out.println(future.getNow("world"));
	}
	
	//使用thenApplyAsync()方法异步转化future
	public static void test4() {
		//使用ForkJoinPool.commonPool()
		CompletableFuture<String> future = CompletableFuture.completedFuture("hello").thenApplyAsync(s -> {
			System.out.println("daemon thread ? " + Thread.currentThread().isDaemon());
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return s.toUpperCase();
		});
		System.out.println(future.getNow("world"));
		System.out.println(future.join());
	}
	
	//使用自定义线程池
	public static void test5() {
		ExecutorService exec = Executors.newFixedThreadPool(5, new ThreadFactory() {
			int count = 1;
			
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "custom-executor-"+count++);
			}
		});
		CompletableFuture<String> future = CompletableFuture.completedFuture("hello").thenApplyAsync(s -> {
			System.out.println("daemon thread ? " + Thread.currentThread().isDaemon());
			System.out.println(Thread.currentThread().getName());
			return s.toUpperCase();
		}, exec);
		System.out.println(future.getNow("world"));
		System.out.println(future.join());
	}
	
	//使用thenAccept()方法操作同步消费者(返回值为Void)
	public static void test6() {
		StringBuilder result = new StringBuilder();
		//thenApplay系列的方法会有返回值，而thenAccept方法则没有返回值，只对结果进行一些操作，类似消费者
	    CompletableFuture.completedFuture("thenAccept message").thenAccept(s ->result.append(s));
	    System.out.println(result.length());
	}
	
	//使用thenAcceptAsync()方法操作异步消费者
	public static void test7() {
		StringBuilder result = new StringBuilder();
		CompletableFuture<Void> future = CompletableFuture.completedFuture("thenAcceptAsync message")
				.thenAcceptAsync(s ->result.append(s));
		//操作完成就返回结果
		future.join();
	    System.out.println(result.length());
	}
	
	//使用cancel()方法取消任务
	public static void test8() {
		CompletableFuture<String> future1 = CompletableFuture.completedFuture("hello")
				.thenApplyAsync(s -> {
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return s.toUpperCase();
				});
		CompletableFuture<String> future2 = future1.exceptionally(throwable -> "canceled message");
		//通过调用 cancel(boolean mayInterruptIfRunning)方法取消计算任务。
		//cancel()方法与 completeExceptionally(new CancellationException())等价
		future1.cancel(true);
		if (future1.isCompletedExceptionally()) {
			System.out.println("canceled message, " + future2.join());
		}
	}
	
	//使用applyToEither()/acceptEither()方法从两个异步任务中接收其中一个最快返回的结果
	public static void test9() {
		String original = "Message";
		Random r = new Random();
		//任务将花费一段时间将字符串转成大写
		CompletableFuture<String> future1 = CompletableFuture.completedFuture(original)
				.thenApplyAsync(s -> {
					try {
						TimeUnit.MILLISECONDS.sleep(r.nextInt(100));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return s.toUpperCase();
				});
		//任务将花费一段时间将字符串转成小写
		CompletableFuture<String> future2 = CompletableFuture.completedFuture(original)
				.thenApplyAsync(s -> {
					try {
						TimeUnit.MILLISECONDS.sleep(r.nextInt(100));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return s.toLowerCase();
				});
		//优先获取最先返回的数据
		CompletableFuture<String> future = future1.applyToEither(future2, s -> s + " from applyToEither");
		System.out.println(future.join());
		
		//也可使用消费者直接处理
		future1.acceptEither(future2, s -> {
			System.out.println(s + " from acceptEither");
		});
	}
	
	//等待执行两个操作之后再触发一个执行过程
	public static void test10() {
		String original = "Message";
		StringBuilder result = new StringBuilder();
		//runAfterBoth等待两个操作都执行完毕后再触发一个执行操作
		CompletableFuture.completedFuture(original).thenApply(String::toUpperCase).runAfterBoth(
		CompletableFuture.completedFuture(original).thenApply(String::toLowerCase), 
			() -> result.append("done"));
		System.out.println(result);
		
		//thenAcceptBoth则是有返回值的版本
		CompletableFuture.completedFuture(original).thenApply(String::toUpperCase).thenAcceptBoth(
	    CompletableFuture.completedFuture(original).thenApply(String::toLowerCase),
	    	(s1, s2) -> System.out.println(s1 + s2));
	}

	//通过thenCombine()方法整合两个计算结果
	public static void test11() {
		String original = "Message";
		
		//通过thenCombine方法将以同步方式执行两个方法后再合成字符串
		CompletableFuture<String> originFuture = CompletableFuture.completedFuture(original);
		CompletableFuture<String> cf = originFuture.thenApply(String::toUpperCase)
				.thenCombine(originFuture.thenApply(String::toLowerCase), (s1, s2) -> s1 + s2);
		System.out.println(cf.getNow(null));
		
		Random r = new Random();
		//任务将花费一段时间将字符串转成大写
		CompletableFuture<String> future1 = originFuture.thenApplyAsync(s -> {
			try {
				TimeUnit.MILLISECONDS.sleep(r.nextInt(100));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return s.toUpperCase();
		});
		//任务将花费一段时间将字符串转成小写
		CompletableFuture<String> future2 = originFuture.thenApplyAsync(s -> {
			try {
				TimeUnit.MILLISECONDS.sleep(r.nextInt(100));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return s.toLowerCase();
		});
		
		CompletableFuture<String> future = future1.thenCombine(future2, (s1, s2) -> s1 + s2);
		//由于是异步的方式，因此需要通过join()方法来等待执行完毕
		//join()的作用是："等待该线程终止"
		System.out.println("thenCombine: " + future.join());
		
		CompletableFuture<String> futureAsync = future1.thenCombineAsync(future2, (s1, s2) -> s1 + s2);
		System.out.println("thenCombine: " + futureAsync.join());
	}
	
	//通过thenCompose()方法整合两个计算结果
	public static void test12() {
		String original = "Message";
		//通过thenCompose方法将也可以组合两个计算结果
		//与thenCombine不同的是，它接收的是一个函数而不是一个CompletableStage
		CompletableFuture<String> originFuture = CompletableFuture.completedFuture(original);
		CompletableFuture<String> future = originFuture.thenApply(String::toUpperCase)
				.thenCompose(upper -> originFuture.thenApply(String::toLowerCase)
				.thenApply(s -> upper + s));
		System.out.println(future.join());
	}
	
	public static void test13() {
		StringBuilder result = new StringBuilder();
		List<String> messages = Arrays.asList("a", "b", "c");
		List<CompletableFuture> futures = messages.stream()
				.map(msg -> CompletableFuture.completedFuture(msg))
				.collect(Collectors.toList());
		CompletableFuture.anyOf(futures.toArray(new CompletableFuture[futures.size()]))
			.whenComplete((res, th) -> {
				
			});
	}
}
