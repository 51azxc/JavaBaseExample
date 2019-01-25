package concurrent.fork.and.join;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/*
 * fork/join基本操作
 * fork: 当你把任务分成更小的任务和使用这个框架执行它们
 * join: 当一个任务等待它创建的任务的结束
 */

public class BaseTest {

	public static void main(String[] args) {
		//创建一个包括10000个产品的数列
		List<Product> products = generateProductList(1000);
		BaseTask t = new BaseTask(products, 0, products.size(), 0.20);
		ForkJoinPool pool = new ForkJoinPool();
		pool.execute(t);
		
		do {
			System.out.println("Main: Thread count: " + pool.getActiveThreadCount());
			System.out.println("Main: Thread steal: " + pool.getStealCount());
			System.out.println("Main: Parallelism: " + pool.getParallelism());
			
			try {
				TimeUnit.MILLISECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while(!t.isDone());
		//关闭
		pool.shutdown();
		
		//isCompletedNormally方法检查假设任务完成时没有出错
		if (t.isCompletedNormally()) {
			System.out.println("Main: the process has completed normally");
		}
		
		for (int i=0; i<products.size(); i++) {
			Product p = products.get(i);
			if (p.getPrice()!=12) {
				System.out.println("Product: "+p.getName()+"\tprice:"+p.getPrice());
			}
		}
		
		System.out.println("Main: End of the program");
	}
	
	/*
	 * 产生随机产品的数列
	 */
	public static List<Product> generateProductList(int size) {
		List<Product> ret = new ArrayList<>();
		for (int i=0; i<size; i++) {
			Product p = new Product();
			p.setName("Product"+i);
			p.setPrice(10);
			ret.add(p);
		}
		return ret;
	}
}

//RecursiveAction 不会返回结果
class BaseTask extends RecursiveAction {
	//RecursiveAction类的父类ForkJoinTask实现了Serializable接口
	private static final long serialVersionUID = 1L;
	private List<Product> products;
	private int first;
	private int last;
	//存储产品价格的增长
	private double increment;
	
	public BaseTask(List<Product> products, int first, int last, double increment) {
		this.products = products;
		this.first = first;
		this.last = last;
		this.increment = increment;
	}

	@Override
	protected void compute() {
		//任务只能更新价格小于10的产品
		if (last-first<10) {
			updatePrices();
		} else {
			//如果last和first的差大于或等于10，则创建两个新的Task对象，一个处理产品的前半部分，
			//另一个处理产品的后半部分，然后在ForkJoinPool中，使用invokeAll()方法执行它们
			int middle = (last+first)/2;
			System.out.println("Task: Pending tasks: "+getQueuedTaskCount());
			BaseTask t1 = new BaseTask(products, first, middle+1, increment);
			BaseTask t2 = new BaseTask(products, middle+1, last, increment);
			//执行每个任务所创建的子任务
			//这是一个同步调用，这个任务在继续（可能完成）它的执行之前，必须等待子任务的结束。
			//当任务正在等待它的子任务（结束）时，正在执行它的工作线程执行其他正在等待的任务。
			//在这种行为下，Fork/Join框架比Runnable和Callable对象本身提供一种更高效的任务管理
			invokeAll(t1, t2);
		}
	}
	
	private void updatePrices() {
		for (int i=first; i<last; i++) {
			Product p = products.get(i);
			p.setPrice(p.getPrice()*(1+increment));
		}
	}

}

class Product {
	private String name;
	private double price;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
}
