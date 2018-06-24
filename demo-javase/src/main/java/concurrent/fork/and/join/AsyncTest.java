package concurrent.fork.and.join;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/*
 * fork/join异步操作
 * 
 * 调用同步方法invokeAll()的时候任务会被阻塞，直到提交给池的任务完成它的执行。
 * 调用异步方法fork()时，这个任务将继续它的执行，
 * 所以ForkJoinPool类不能使用work-stealing算法来提高应用程序的性能。
 * 在这种情况下，只有当你调用join()或get()方法来等待任务的完成时，
 * ForkJoinPool才能使用work-stealing算法
 */

/*
 * 在一个文件夹及其子文件夹内查找确定扩展名的文件
 * 
 * 对于文件夹里的每个子文件夹，它将以异步的方式提交一个新的任务给ForkJoinPool类。
 * 对于文件夹里的每个文件，任务将检查文件的扩展名，如果它被处理，并把它添加到结果列表。
 */

public class AsyncTest {
	
	public static void main(String[] args) {
		ForkJoinPool pool = new ForkJoinPool();
		FolderProcessor fp1 = new FolderProcessor("C:\\Windows", "txt");
		FolderProcessor fp2 = new FolderProcessor("C:\\Program Files", "txt");
		FolderProcessor fp3 = new FolderProcessor("C:\\Users", "txt");
		//ForkJoinPool类同时允许任务的执行以异步的方式
		pool.execute(fp1);
		pool.execute(fp2);
		pool.execute(fp3);
		do {
			System.out.println("--------------------------------------------------");
			System.out.println("Main: Active Thread: " + pool.getActiveThreadCount());
			System.out.println("Main: Thread steal: " + pool.getStealCount());
			System.out.println("Main: Parallelism: " + pool.getParallelism());
			System.out.println("Main: Task count: " + pool.getQueuedTaskCount());
			System.out.println("--------------------------------------------------");
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while((!fp1.isDone())||(!fp2.isDone())||(!fp3.isDone()));
		pool.shutdown();
		
		System.out.println("Windows found files: "+fp1.join().size());
		System.out.println("Program Files found files: "+fp2.join().size());
		System.out.println("Users found files: "+fp3.join().size());
	}
}

/*
 * 如果任务找到一个文件夹，它创建另一个Task对象来处理这个文件夹，并使用fork()方法把它（Task对象）提交给池。
 * 这个方法提交给池的任务将被执行，如果池中有空闲的工作线程或池可以创建一个新的工作线程。
 * 这个方法会立即返回，所以这个任务可以继续处理文件夹的内容。
 * 对于每个文件，任务将它的扩展与所想要查找的（扩展）进行比较，如果它们相等，将文件名添加到结果数列。
 * 
 * 一旦这个任务处理完指定文件夹的所有内容，它将使用join()方法等待已提交到池的所有任务的结束。
 * 这个方法在一个任务等待其执行结束时调用，并返回compute()方法返回的值。
 * 这个任务将它自己发送的所有任务的结果和它自己的结果分组，并返回作为compute()方法的一个返回值的数组。
 */

class FolderProcessor extends RecursiveTask<List<String>> {

	private static final long serialVersionUID = 1L;

	//存储任务将要处理的文件夹的全路径
	private String path;
	//存储任务将要查找的文件的扩展名
	private String extension;
	
	public FolderProcessor(String path, String extension) {
		super();
		this.path = path;
		this.extension = extension;
	}

	@Override
	protected List<String> compute() {
		//保存存储在文件夹中的文件
		List<String> list = new ArrayList<>();
		//保存将要处理存储在文件夹内的子文件夹的子任务
		List<FolderProcessor> tasks = new ArrayList<>();
		//获取文件夹的内容
		File file = new File(path);
		File content[] = file.listFiles();
		//对于文件夹里的每个元素，如果是子文件夹，则创建一个新的FolderProcessor对象，
		//并使用fork()方法异步地执行它
		if (content != null) {
			for (int i=0; i<content.length; i++) {
				if (content[i].isDirectory()) {
					FolderProcessor task = new FolderProcessor(content[i].getAbsolutePath(), extension);
					task.fork();
					tasks.add(task);
				} else {
					//比较传入参数的文件名的结束扩展是否是你想要查找的
					if (content[i].getName().endsWith(extension)) {
						list.add(content[i].getAbsolutePath());
					}
				}
			}
		}
		if (tasks.size()>50) {
			System.out.println(file.getAbsolutePath()+": "+tasks.size()+" tasks");
		}
		addResultsFromTasks(list, tasks);
		return list;
	}
	
	private void addResultsFromTasks(List<String> list, List<FolderProcessor> tasks) {
		//对于保存在tasks数列中的每个任务，调用join()方法，这将等待任务执行的完成，并且返回任务的结果。
		//使用addAll()方法将这个结果添加到字符串数列
		for (FolderProcessor item: tasks) {
			//join()方法不能被中断,中断就抛出InterruptedException异常
			//与get的区别: 
			//如果任务抛出任何未受检异常，get()方法将返回一个ExecutionException异常，
			//而join()方法将返回一个RuntimeException异常
			list.addAll(item.join());
		}
	}

}
