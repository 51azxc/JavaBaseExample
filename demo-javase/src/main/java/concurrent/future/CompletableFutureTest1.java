package concurrent.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CompletableFutureTest1 {

	public static void main(String[] args) {
		System.out.println("-----------test1--------------");
		test1();
		System.out.println("-----------test2--------------");
		test2();
		System.out.println("-----------test3--------------");
		test3();
		System.out.println("-----------test4--------------");
		test4();
		System.out.println("-----------test5--------------");
		test5();
	}
	
	public static void test1() {
		System.out.println("async exec");
		final CompletableFuture<Integer> future1 = new CompletableFuture<>();
		new Thread(new AsyncThread(future1)).start();
		System.out.println(1);
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		future1.complete(60);
	}
	
	public static void test2() {
		System.out.println("async exec");
		final CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(()->calc(50));
		try {
			System.out.println(future2.get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public static void test3() {
		System.out.println("stream");
		CompletableFuture<Void> future3 = CompletableFuture
				.supplyAsync(()->calc(40))
				.thenApply((i)->Integer.toString(i))
				.thenAccept(System.out::println);
		try {
			future3.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public static void test4() {
		System.out.println("compose");
		CompletableFuture<Void> future4 = CompletableFuture
				.supplyAsync(()->calc(30))
				.thenCompose((i)->CompletableFuture.supplyAsync(()->calc(i)))
				.thenApply((i)->Integer.toString(i))
				.thenAccept(System.out::println);
		try {
			future4.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public static void test5() {
		System.out.println("runAsync vs supplyAsync");
		CompletableFuture<Void> futureRun = CompletableFuture.runAsync(()->{System.out.println("Hello");});
		try {
			futureRun.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		System.out.println("future runAsync complete");
		
		CompletableFuture<String> futureSupply = CompletableFuture.supplyAsync(()->"Hello");
		try {
			System.out.println(futureSupply.get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		System.out.println("future supplyAsync complete");
	}
	
	public static Integer calc(Integer para) {
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return para * para;
	}

}

class AsyncThread implements Runnable {
	CompletableFuture<Integer> re = null;
	public AsyncThread(CompletableFuture<Integer> re) {
		this.re = re;
	}
	@Override
	public void run() {
		int i = 0;
		try {
			i = re.get() * re.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(i);
	}
}
