package concurrent.fork.and.join;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

/*
 * 使用fork/join模型来计算
 */

public class ResultTest1 extends RecursiveTask<Integer> {

	private static final long serialVersionUID = 1L;
	private final static int THRESHOLD = 10;
	private int start;
	private int end;

	public ResultTest1(int start, int end) {
		super();
		this.start = start;
		this.end = end;
	}

	@Override
	protected Integer compute() {
		int sum = 0;
		//小于10就直接计算
		if ((end - start) <= THRESHOLD) {
			for (int i = start; i <= end; i++) {
				sum += i;
			}
		} else {
			//大于10就使用fork/join方式计算
			int middle = (start + end) / 2;
			ResultTest1 leftTask = new ResultTest1(start, middle);
			ResultTest1 rightTask = new ResultTest1(middle + 1, end);
			leftTask.fork();
			rightTask.fork();
			int leftResult = leftTask.join();
			int rightResult = rightTask.join();
			sum = leftResult + rightResult;
		}
		return sum;
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ForkJoinPool pool = new ForkJoinPool();
		ResultTest1 task = new ResultTest1(1, 100);
		Future<Integer> result = pool.submit(task);
		System.out.println("result: " + result.get());
	}
}
