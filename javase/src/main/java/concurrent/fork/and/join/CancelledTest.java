package concurrent.fork.and.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/*
 * fork/join取消任务
 * 
 * 注: 不能取消一个已经执行的任务
 */

public class CancelledTest {

	public static void main(String[] args) {
		//创建一个1000个包含0-10的随机数数组
		int array[] = new Random().ints(1000, 0, 10).toArray();
		TaskManager manager = new TaskManager();
		ForkJoinPool pool = new ForkJoinPool();
		SearchNumberTask task = new SearchNumberTask(array, 0, 1000, 5, manager);
		//使用execute()方法，在池中异步执行任务
		pool.execute(task);
		pool.shutdown();
		try {
			//等待任务的结束
			pool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Main: the program has finished");
	}

}

/*
 * 存储在ForkJoinPool中执行的所有任务
 */
class TaskManager {
	private List<ForkJoinTask<Integer>> tasks = new ArrayList<>();
	
	//添加ForkJoinTask对象到任务数列
	public void addTask(ForkJoinTask<Integer> task) {
		tasks.add(task);
	}
	
	//使用cancel()方法取消在数列中的所有ForkJoinTask对象。
	//它接收一个想要取消剩余任务的ForkJoinTask对象作为参数。这个方法取消所有任务
	public void cancelTask(ForkJoinTask<Integer> cancelTask) {
		for (ForkJoinTask<Integer> task : tasks) {
			if (task != cancelTask) {
				task.cancel(true);
				((SearchNumberTask)task).writeCancelMessage();
			}
		}
	}
}

//查找在整数数组的元素块中的数。
class SearchNumberTask extends RecursiveTask<Integer> {
	private static final long serialVersionUID = 1L;
	private int numbers[];
	private int start, end;
	//将要查找的数
	private int number;
	//取消所有任务功能类
	private TaskManager manager;
	//当任务没有找到这个数时，它将作为任务的返回值
	private final static int NOT_FOUND = -1;
	
	public SearchNumberTask(int[] numbers, int start, int end, int number, 
			TaskManager manager) {
		super();
		this.numbers = numbers;
		this.start = start;
		this.end = end;
		this.number = number;
		this.manager = manager;
	}

	@Override
	protected Integer compute() {
		System.out.println("Task: "+start+":"+end);
		int ret = 0;
		if (end-start>10) {
			ret = launchTasks();
		} else {
			ret = lookForNumber();
		}
		return ret;
	}
	
	private int launchTasks() {
		//首先，将这个任务要处理的数块分成两个部分，然后，创建两个Task对象来处理它们
		int mid = (start+end)/2;
		SearchNumberTask t1 = new SearchNumberTask(numbers,start,mid,number,manager);
		SearchNumberTask t2 = new SearchNumberTask(numbers,mid,end,number,manager);
		//添加这个任务到TaskManager对象中
		manager.addTask(t1);
		manager.addTask(t2);
		//使用fork()方法异步执行这两个任务
		t1.fork();
		t2.fork();
		//等待这个任务的结束，返回第一个任务的结果（如果它不等于1），或第二个任务的结果
		int ret;
		ret = t1.join();
		if (ret != -1) {
			return ret;
		}
		ret = t2.join();
		return ret;
	}
	
	/*
	 * 对于任务要处理的元素块中的所有元素，将你想要查找的数与存储在元素中的值进行比较。
	 * 如果他们相等，写入一条信息到控制台表明这种情形，
	 * 使用TaskManager对象的cancelTasks()方法来取消所有任务，并返回你已经找到的这个数对应元素的位置
	 */
	private int lookForNumber() {
		for (int i=start; i<end; i++) {
			if (numbers[i]==number) {
				System.out.println("Task: number "+number+" found in position "+i);
				manager.cancelTask(this);
				return i;
			}
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return NOT_FOUND;
	}
	
	//当任务取消时，写一条信息到控制台
	public void writeCancelMessage() {
		System.out.println("Task: Canceled task from "+start+" to "+end);
	}

}
