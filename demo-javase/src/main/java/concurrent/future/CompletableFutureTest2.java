package concurrent.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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
	}
	
	//使用一个预定义的结果创建一个完整的CompletableFuture
	public static void test1() {
		CompletableFuture<String> future = CompletableFuture.completedFuture("hello");
		if (future.isDone()) {
			//如果future已经完成,getNow方法返回future返回值，否则就返回传入参数("world")
			System.out.println(future.getNow("world"));
		}
	}
	
	//执行一个异步操作
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
	
	//同步执行转化future
	public static void test3() {
		//then表示操作会等待之前的操作完成
		//Apply表示返回的阶段会对之前产生的结果进行一次操作
		//thenApply会被阻塞
		CompletableFuture<String> future = CompletableFuture.completedFuture("hello").thenApply(s -> s.toUpperCase());
		System.out.println(future.getNow("world"));
	}
	
	//异步转化future
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
	
	//同步消费者(返回值为Void)
	public static void test6() {
		StringBuilder result = new StringBuilder();
		//thenApplay系列的方法会有返回值，而thenAccept方法则没有返回值，只对结果进行一些操作，类似消费者
	    CompletableFuture.completedFuture("thenAccept message").thenAccept(s ->result.append(s));
	    System.out.println(result.length());
	}
	
	//异步消费者
	public static void test7() {
		StringBuilder result = new StringBuilder();
		CompletableFuture<Void> future = CompletableFuture.completedFuture("thenAcceptAsync message")
				.thenAcceptAsync(s ->result.append(s));
		//操作完成就返回结果
		future.join();
	    System.out.println(result.length());
	}
	
	//处理异常
	public static void test8() {
		CompletableFuture<String> future = CompletableFuture.completedFuture("hello")
				.thenApplyAsync(s -> {
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return s.toUpperCase();
				});
		CompletableFuture<String> exceptionHandler = future.handle((s, th) -> { 
			return (th != null) ? "message uponcancel" : ""; 
		});
		
		future.completeExceptionally(new RuntimeException("completed exceptionally"));
		if (future.isCompletedExceptionally()) {
			try {
				future.join();
			} catch (Exception e) {
				System.out.println(e.getCause().getMessage());
			}
		}
		
		
	}

}
