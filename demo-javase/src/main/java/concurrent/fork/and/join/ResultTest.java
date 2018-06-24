package concurrent.fork.and.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/*
 * fork/join 返回结果
 * 
 * 在文档中查找单词的应用程序。你将会实现以下两种任务类型：
 * 一个文档任务，将在文档中的行集合中查找一个单词。
 * 一个行任务，将在文档的一部分数据中查找一个单词。
 * 所有任务将返回单词在文档的一部分中或行中出现的次数
 * 
 */

public class ResultTest {

	public static void main(String[] args) {
		DocumentMock mock=new DocumentMock();
		String[][] document = mock.generateDocument(100, 1000, "the");
		//更新整个文档的产品
		DocumentTask task = new DocumentTask(document, 0, 100, "the");
		ForkJoinPool pool = new ForkJoinPool();
		pool.execute(task);
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
		} while(!task.isDone());
		pool.shutdown();
		//awaitTermination()方法等待任务的结束
		try {
			pool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//打印单词在文档中出现的次数。检查这个数是否与DocumentMock类中写入的数一样
		try {
			System.out.println("Main: the word appears: "+task.get()+" in the document");
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

}

// 生成一些数据
class DocumentMock {
	private String words[]={"the","hello","goodbye","packt", "java",
			"thread","pool","random","class","main"};
	
	/*
	 * params: 行数、每行的单词数
	 * return: 表示将要查找的单词的二维数组
	 */
	public String[][] generateDocument(int numLines, int numWords,String word){
		int counter = 0;
		String document[][] = new String[numLines][numWords];
		Random r = new Random();
		//存储在每个位置的字符串是单词数组的随机位置，
		//统计这个程序将要在生成的数组中查找的单词出现的次数
		for (int i=0; i<numLines; i++) {
			for (int j=0; j<numWords; j++) {
				int index = r.nextInt(words.length);
				document[i][j] = words[index];
				if (document[i][j].equals(word)) {
					counter++;
				}
			}
		}
		System.out.println("DocumentMock: The words appears: "+counter);
		return document;
	}
	
}

/*
 * 这个类的任务将处理由start和end属性决定的文档中的行组。
 * 如果这个行组的大小小于10，它为每行创建LineTask对象，
 * 并且当它们完成它们的执行时，它合计这些任务的结果，并返回这个合计值。
 * 如果这个任务要处理的行组大小不小于10，它将这个组分成两个并创建两个DocumentTask对象来处理这些新组。
 * 当这些任务完成它们的执行时，这个任务合计它们的结果，并返回这个合计值
 * 
 * 统计单词在一组行中出现的次数的任务
 * RecursiveTask可以返回一个执行结果
 */
class DocumentTask extends RecursiveTask<Integer> {
	
	private static final long serialVersionUID = 1L;
	private String document[][];
	private int start, end;
	private String word;
	
	public DocumentTask(String[][] document, int start, int end, String word) {
		super();
		this.document = document;
		this.start = start;
		this.end = end;
		this.word = word;
	}

	@Override
	protected Integer compute() {
		Integer result = 0;
		if (end-start<10) {
			result = processLines(document, start, end, word);
		} else {
			int mid = (start+end)/2;
			DocumentTask t1 = new DocumentTask(document, start, mid, word);
			DocumentTask t2 = new DocumentTask(document, mid, end, word);
			invokeAll(t1, t2);
			try {
				//获取任务返回值
				result = t1.get()+t2.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private Integer processLines(String[][] document, int start, int end, String word) {
		List<LineTask> tasks = new ArrayList<>();
		for (int i=start; i<end; i++) {
			//LineTask对象来处理整行，并且将它们存储在任务数列中。
			LineTask task = new LineTask(document[i], 0, document[i].length, word);
			tasks.add(task);
		}
		//执行所有任务
		invokeAll(tasks);
		Integer result = 0;
		//合计所有这些任务返回的值
		for (LineTask task:tasks) {
			try {
				result += task.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}

/*
 * 这个类的任务将处理文档中的一行的单词组。
 * 如果这个单词组小于10，这个任务直接在这个单词组中查找单词，并且返回这个单词出现的次数。
 * 否则，它将这个单词组分成两个并创建两个LineTask对象来处理。
 * 当这些任务完成它们的执行，这个任务合计这些任务的结果并返回这个合计值
 */
class LineTask extends RecursiveTask<Integer> {

	private static final long serialVersionUID = 1L;
	
	private String line[];
	private int start, end;
	private String word;
	
	public LineTask(String[] line, int start, int end, String word) {
		super();
		this.line = line;
		this.start = start;
		this.end = end;
		this.word = word;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.RecursiveTask#compute()
	 * 执行一个任务，并且返回一个结果
	 * 并且当join()方法被调用时，将这个对象作为任务的结果返回
	 */
	@Override
	protected Integer compute() {
		Integer result = null;
		if (end-start<10) {
			result = count(line, start, end, word);
		} else {
			int mid = (start+end)/2;
			LineTask t1 = new LineTask(line, start, mid, word);
			LineTask t2 = new LineTask(line, mid, end, word);
			invokeAll(t1, t2);
			try {
				result = t1.get()+t2.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private Integer count(String[] line, int start, int end, String word) {
		//比较这个任务将要查找的word属性中的在start和end属性之间的位置的单词，
		//如果它们相等，则增加count变量
		int counter = 0;
		for (int i=start; i<end; i++) {
			if (line[i].equals(word)) {
				counter++;
			}
		}
		//为了显示示例的执行，令任务睡眠10毫秒
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return counter;
	}
	
}
