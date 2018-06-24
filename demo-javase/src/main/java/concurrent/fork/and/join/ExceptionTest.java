package concurrent.fork.and.join;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/*
 * fork/join抛出异常
 * 
 * 在ForkJoinTask类的compute()方法中，你不能抛出任何已检查异常，
 * 因为在这个方法的实现中，它没有包含任何抛出（异常）声明。
 */

public class ExceptionTest {

	public static void main(String[] args) {
		int array[] = new int[100];
		ExceptionTask task = new ExceptionTask(array, 0, 100);
		ForkJoinPool pool = new ForkJoinPool();
		pool.execute(task);
		pool.shutdown();
		try {
			//等待任务结束
			pool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//检查是否抛出异常
		if (task.isCompletedAbnormally()) {
			//通过getExsception方法获取异常
			System.out.println("Main exception: "+task.getException());
		}
		System.out.println("Main result: "+task.join());
	}
}

class ExceptionTask extends RecursiveTask<Integer> {

	private static final long serialVersionUID = 1L;
	private int array[];
	private int start, end;
	
	public ExceptionTask(int[] array, int start, int end) {
		this.array = array;
		this.start = start;
		this.end = end;
	}

	@Override
	protected Integer compute() {
		System.out.println("Task start from: "+start+" to: "+end);
		if (end-start<10) {
			//假定抛出异常
			if (start<3 && end>3) {
				throw new RuntimeException("Exception from: "+start+" to: "+end);
			}
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			int mid = (start+end)/2;
			ExceptionTask t1 = new ExceptionTask(array, start, mid);
			ExceptionTask t2 = new ExceptionTask(array, mid, end);
			invokeAll(t1, t2);
		}
		System.out.println("Task end from: "+start+" to: "+end);
		return 0;
	}
}
