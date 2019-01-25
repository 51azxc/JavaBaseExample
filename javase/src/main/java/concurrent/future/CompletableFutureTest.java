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
 * 
 * CompletableFuture有很多种方法，其中分为不同的类型:
 * 
 * 创建方法有completedFuture/runAsync/supplyAsync
 * 转换方法有thanApply/thenApplyAsync
 * 消费方法有thenAccept/thenAcceptAsync/thenAcceptBoth/thenAcceptBothAsync
 * 			/runAfterBoth/runAfterBothAsync
 * 组合方法有thenCompose/thenComposeAsync/thenCombine/thenCombineAsync
 * 			/acceptEither/acceptEitherAsync/applyToEither/applyToEitherAsync
 * 获取结果方法有get/getNow/join
 * 结果处理方法有whenComplete/whenCompleteAsync/exceptionally
 * 以及一些辅助方法anyOf/allOf
 * 
 * 转换/消费/组合功能的各方法中不带Async后缀的方法表示操作都是由同个线程执行
 * 而带Async后缀的版本表示为异步执行,也就是开启其他的线程执行。
 * Async系列方法则还有一个带Executor参数的版本，表示需要自己指定线程池，
 * 不带Exec参数的默认是指定ForkJoinPool.commonPool()为线程池,它使用守护线程去执行任务
 * 
 * 带apply名字的方法则一般都会有生成对应的返回值交给下一个CompletableFuture操作
 * 带accept名字的方法则会有操作的结果，但是不会作为返回值返回。这些方法的返回值为Void。
 * 带run名字的方法则结果值也不会获取到
 * 
 */
public class CompletableFutureTest {

	public static void main(String[] args) {
		System.out.println("----------创建CompletableFuture----------");
		createCompletableFuture();
		System.out.println("----------异步创建CompletableFuture----------");
		createCompletableFutureAsync();
		System.out.println("----------取消任务----------");
		useCancelMethod();
		System.out.println("----------使用自定义线程池----------");
		customExecutorService();
		System.out.println("----------处理返回结果----------");
		handleResult();
		System.out.println("----------同步转换----------");
		useThenApplyMethod();
		System.out.println("----------异步转换----------");
		useThenApplyAsyncMethod();
		System.out.println("----------消费结果----------");
		useThenAcceptMethod();
		System.out.println("----------从两个任务中获取其中一个最快返回的结果----------");
		userEitherSeriesMethod();
		System.out.println("----------运行两个任务后执行监听任务----------");
		userBothSeriesMethod();
		System.out.println("----------整合两个计算结果----------");
		useThenCombineMethod();
		System.out.println("----------按顺序整合两个计算结果----------");
		useThenComposeMethod();
		System.out.println("----------从一组任务中获取最快返回的结果----------");
		useAnyOfMethod();
		System.out.println("----------获取一组任务的所有返回结果----------");
		useAllOfMethod();
	}
	
	/* 创建一个完整的CompletableFuture */
	public static void createCompletableFuture() {
		CompletableFuture<String> future1 = CompletableFuture.completedFuture("hello");
		if (future1.isDone()) {
			//如果future已经完成,getNow方法返回future返回值，否则就返回传入参数("world")
			System.out.println(future1.getNow("world"));
		}
		
		CompletableFuture<Integer> future2 = new CompletableFuture<>();
		//通过complete方法可以直接返回数据，
		//当然如果调用了这个方法之后CompletableFuture后续将会失效
		future2.complete(1);
		System.out.println(future2.getNow(0));
	}
	
	/* 使用runAsync()/suppleyAsync()方法异步创建CompletableFuture对象 */
	public static void createCompletableFutureAsync() {
		//run开头表明没有返回值(Void)的CompletableFuture对象
		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
			System.out.println("daemon thread ? " + Thread.currentThread().isDaemon());
			delayExec(100);
		});
		System.out.println("1. future is done? " + future.isDone());
		delayExec(200);
		System.out.println("2. future is done? " + future.isDone());
		
		//supplyAsync表明创建一个有返回值的CompletableFuture对象。
		CompletableFuture<String> futureAsync = CompletableFuture.supplyAsync(() -> "supplyAsync");
		//由于是异步，因此需要join来阻塞线程直至获取结果
		//join()的作用是："等待该线程终止"
		System.out.println(futureAsync.join());
	}
	
	/* 使用cancel()方法取消任务 */
	public static void useCancelMethod() {
		CompletableFuture<String> future1 = CompletableFuture.completedFuture("cancel")
				.thenApplyAsync(s -> delayUpperCase(s));
		CompletableFuture<String> future2 = future1.exceptionally(throwable -> "canceled message");
		//通过调用 cancel(boolean mayInterruptIfRunning)方法取消计算任务。
		//cancel()方法与 completeExceptionally(new CancellationException())等价
		future1.cancel(true);
		if (future1.isCompletedExceptionally()) {
			System.out.println("canceled message, " + future2.join());
		}
	}
	
	/* 处理返回结果 */
	public static void handleResult() {
		CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
			delayExec(100);
			int i = new Random().nextInt(10);
			//模拟抛出异常
			if (i > 5) {
				throw new RuntimeException("random test exception");
			}
			return i;
		});
		//whenComplete可以对操作结果进行一些处理，也可以直接返回
		future.whenComplete((v, e) -> {
			if (e != null) {
				System.out.println("whenComplete exception: " + e.getMessage());
			} else {
				System.out.println("whenComplete result: " + v);
			}
		});
		
		//通过exceptionally方法可以捕获异常，还可以指定出现异常时的返回值
		Integer i = future.whenCompleteAsync((v, e) -> {
			if (e != null) {
				System.out.println("whenCompleteAsync exception: " + e.getMessage());
			} else {
				System.out.println("whenCompleteAsync result: " + v);
			}
		}).exceptionally(e -> {
			System.out.println("exceptionally get: " + e.getMessage());
			return 0;
		}).join();
		System.out.println("exceptionally result: " + i);
		
		//handle也可以对返回结果进行一些处理。与whenComplete不同的是，
		//whenComplete的返回值必须与CompletableFuture生成的一样，而handle则可以转换成其他类型的返回对象。
		String s = future.handle((v, e) -> {
			if (e != null) {
				System.out.println("handle exception: " + e.getMessage());
				return "error";
			} else {
				return "Hello " + v;
			}
		}).join();
		System.out.println("handle result: " + s);
	}
	
	/* 使用自定义线程池 */
	public static void customExecutorService() {
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
		exec.shutdown();
	}
	
	/* 使用thenApply()方法同步执行转换CompletableFuture */
	public static void useThenApplyMethod() {
		//then表示操作会等待之前的操作完成
		//Apply表示返回的阶段会对之前产生的结果进行一次操作
		//thenApply会被阻塞
		CompletableFuture<String> future = CompletableFuture.completedFuture("hello")
				.thenApply(s -> s.toUpperCase());
		System.out.println(future.getNow("world"));
	}
	
	/* 使用thenApplyAsync()方法异步转化future */
	public static void useThenApplyAsyncMethod() {
		//使用ForkJoinPool.commonPool()
		CompletableFuture<String> future = CompletableFuture.completedFuture("thenApplyAsync")
				.thenApplyAsync(s -> {
					System.out.println("daemon thread ? " + Thread.currentThread().isDaemon());
					return delayUpperCase(s);
		});
		//getNow会立刻返回，不会得到异步处理的数据，因此返回值是预设值null
		System.out.println(future.getNow(null));
		//join会阻塞等待，因此能得到正确返回值
		System.out.println(future.join());
	}
	
	/* thenAccept方法可以消费结果 */
	public static void useThenAcceptMethod() {
		StringBuilder result = new StringBuilder();
		//thenApply系列的方法会有返回值，而thenAccept方法则没有返回值，只对结果进行一些操作，类似消费者
		//使用thenAccept()方法操作同步消费者(返回值为Void)
	    CompletableFuture.completedFuture("thenAccept").thenAccept(s -> result.append(s));
	    System.out.println(result);
	    
	    //异步消费
	    CompletableFuture.completedFuture("thenAcceptAsync")
	    	.thenAcceptAsync(s -> System.out.println(s));
	    
	    result.delete(0, result.length());
	    //thenRun可以监听是否完成处理，与thenAccept不同的是它没有拿到返回结果
	    CompletableFuture.runAsync(() -> result.append("thenRun"))
    	.thenRun(() -> System.out.println("finished"));
	    System.out.println(result);
	}
	
	/* 使用applyToEither()/acceptEither()方法从两个异步任务中接收其中一个最快返回的结果 */
	public static void userEitherSeriesMethod() {
		String original = "Message";
		CompletableFuture<String> originFuture = CompletableFuture.completedFuture(original);
		//任务将花费一段时间将字符串转成大写
		CompletableFuture<String> future1 = originFuture.thenApplyAsync(s -> delayUpperCase(s));
		//任务将花费一段时间将字符串转成小写
		CompletableFuture<String> future2 = originFuture.thenApplyAsync(s -> delayLowerCase(s));
		
		//applyToEither优先获取最先返回的数据
		CompletableFuture<String> future = future1.applyToEither(future2, s -> "applyToEither reslut: " + s);
		System.out.println(future.join());
		
		//acceptEither直接消费结果
		future1.acceptEither(future2, s -> System.out.println("acceptEither result: " + s));
		
		//同样也有异步的对应方法applyToEitherAsync
		CompletableFuture<String> future3 = originFuture.thenApply(String::toUpperCase);
		CompletableFuture<String> future4 = originFuture.thenApply(String::toLowerCase);
		CompletableFuture<String> futureAsync = future3
				.applyToEitherAsync(future4,  s -> "applyToEitherAsync reslut: " + s);
		System.out.println(futureAsync.getNow(null));
		System.out.println(futureAsync.join());
		
		//acceptEitherAsync异步消费结果
		future3.acceptEitherAsync(future4, 
				s -> System.out.println("acceptEitherAsync result: " + s)).join();
		
	}
	
	/* runAfterBoth/thenAcceptBoth等待执行两个操作之后再触发一个执行过程 */
	public static void userBothSeriesMethod() {
		String original = "Message";
		
		CompletableFuture<String> originFuture = CompletableFuture.completedFuture(original);
		CompletableFuture<String> future1 = originFuture.thenApply(String::toUpperCase);
		CompletableFuture<String> future2 = originFuture.thenApply(String::toLowerCase);
		//runAfterBoth等待两个操作都执行完毕后再触发一个执行操作(类似CyclicBarrier?)
		//所有操作皆为同步执行,runAfterBoth并不能获取返回值
		future1.runAfterBoth(future2, () -> System.out.println("runAfterBoth Done"));
		//异步版本
		future1.runAfterBothAsync(future2, () -> System.out.println("runAfterBothAsync done")).join();
		
		//thenAcceptBoth可以获取到两个操作的最终结果
		//如果希望对两个返回值做些操作但是又不想将这个值给传递下去，就可以使用thenAcceptBoth
		CompletableFuture.supplyAsync(() -> "supplyAsync")
			.thenAcceptBoth(CompletableFuture.supplyAsync(() -> "thenAcceptBoth"), 
					(s1, s2) -> System.out.println(s1 + " " + s2));
		//异步版本
		future1.thenAcceptBothAsync(future2, 
				(s1, s2) -> System.out.println("thenAcceptBothAsync result: " + s1 + " " + s2)).join();
	}

	/* 通过thenCombine()方法整合两个计算结果 */
	public static void useThenCombineMethod() {
		String original = "Message";
		
		//通过thenCombine方法将以同步方式执行两个方法后再合成字符串
		//两个CompletionStage是并行执行的，它们之间相互独立
		//功能类似有返回值版的thenAcceptBoth
		CompletableFuture<String> originFuture = CompletableFuture.completedFuture(original);
		CompletableFuture<String> cf = originFuture.thenApply(String::toUpperCase)
				.thenCombine(originFuture.thenApply(String::toLowerCase), (s1, s2) -> s1 + " " +s2);
		System.out.println("thenCombine result: " + cf.getNow(null));
		
		//任务将花费一段时间将字符串转成大写
		CompletableFuture<String> future1 = originFuture.thenApplyAsync(s -> delayUpperCase(s));
		//任务将花费一段时间将字符串转成小写
		CompletableFuture<String> future2 = originFuture.thenApplyAsync(s -> delayLowerCase(s));
		
		//由于是异步的方式，因此需要通过join()方法来等待执行完毕
		//由于接收的任务是异步的，因此thenCombine也是异步的(与thenCombineAsync一样)
		CompletableFuture<String> future = future1.thenCombine(future2, (s1, s2) -> s1 + " " +s2);
		System.out.println("thenCombine result: " + future.join());
		
		//thenCombineAsync是异步的方式等待处理结果，因此也需要join()
		CompletableFuture<String> futureAsync = originFuture.thenApply(String::toUpperCase)
				.thenCombineAsync(originFuture.thenApply(String::toLowerCase), (s1, s2) -> s1 + " " +s2);
		System.out.println("thenCombineAsync result: " + futureAsync.join());
	}
	
	/* 通过thenCompose()方法整合两个计算结果 */
	public static void useThenComposeMethod() {
		String original = "Message";
		//通过thenCompose方法将也可以组合两个计算结果。
		//与thenCombine不同的是，它接收的是一个函数,如果函数没有算出来，则会生成一个新的组合CompletableFuture。
		//它依赖于前一个CompletableFuture的结果,类似flatMap函数
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> 100)
				.thenCompose(i -> CompletableFuture.supplyAsync(() -> String.valueOf(i)));
		CompletableFuture<String> originFuture = CompletableFuture.completedFuture(original);
		System.out.println("thenCompose result: " + future.getNow(null));
		
		//同样也有异步的版本thenComposeAsync
		CompletableFuture<String> futureAsync = originFuture.thenApply(String::toUpperCase)
				.thenComposeAsync(upper -> originFuture.thenApply(String::toLowerCase)
				.thenApply(s -> upper + s));
		System.out.println("thenComposeAsync result: " + futureAsync.join());
	}
	
	/* 从一组CompletableFuture中获取任意返回的结果。
	 * 类似CountLatchDown */
	public static void useAnyOfMethod() {
		List<String> messages = Arrays.asList("a", "b", "c");
		List<CompletableFuture<String>> futures = messages.stream()
				.map(msg -> CompletableFuture.completedFuture(msg).thenApply(s -> delayUpperCase(s)))
				.collect(Collectors.toList());
		//当CompletableFuture的计算完成时则会调用whenComplete方法
		CompletableFuture.anyOf(futures.toArray(new CompletableFuture[futures.size()]))
			.whenComplete((res, th) -> {
				System.out.print("anyOf result: ");
				futures.forEach(cf -> System.out.print(cf.getNow(null)));
				System.out.println("\tdone");
			});
	}
	
	/* 获取一组CompletableFuture所有返回结果 */
	public static void useAllOfMethod() {
		List<String> messages = Arrays.asList("a", "b", "c");
		List<CompletableFuture<String>> futures = messages.stream()
				.map(msg -> CompletableFuture.completedFuture(msg).thenApply(s -> delayUpperCase(s)))
				.collect(Collectors.toList());
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
			.whenComplete((res, th) -> {
				System.out.print("allOf result: ");
				futures.forEach(cf -> System.out.print(cf.getNow(null)));
				System.out.println("\tdone");
			});
		
		//如果是一组异步的任务则需要通过join()来阻塞
		List<CompletableFuture<String>> futureAsyncs = messages.stream()
				.map(msg -> CompletableFuture.completedFuture(msg).thenApplyAsync(s -> delayUpperCase(s)))
				.collect(Collectors.toList());
		CompletableFuture<Void> allOfFutures = CompletableFuture.allOf(
				futureAsyncs.toArray(new CompletableFuture[futures.size()]))
			.whenComplete((res, th) -> {
				System.out.print("allOf async result: ");
				futureAsyncs.forEach(cf -> System.out.print(cf.getNow(null)));
				System.out.println("\tdone");
			});
		allOfFutures.join();
	}
	
	private static void delayExec(int ms) {
		ms = ms < 0 ? new Random().nextInt(500) : ms;
		try {
			TimeUnit.MILLISECONDS.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static String delayUpperCase(String s) {
		delayExec(-1);
		return s.toUpperCase();
	}
	
	private static String delayLowerCase(String s) {
		delayExec(-1);
		return s.toLowerCase();
	}
}
