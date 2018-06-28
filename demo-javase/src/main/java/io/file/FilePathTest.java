package io.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/*
 *  使用JDK7的文件系统新API
 */
public class FilePathTest {

	public static void main(String[] args) {
		
		try {
			filesExample();
		} catch (Exception e) {
			e.printStackTrace();
		}
		filesVisitor();
		pathsExample();
	}
	
	//操作Files
	public static void filesExample() throws IOException {
		System.out.println("----------文件操作----------");
		Path p1 = Paths.get(System.getProperty("user.home"), "test.txt");
		
		writeFile(p1);
		readFile(p1);
		
		//判断文件的可读可写可执行
		boolean isRegularExecutableFile = Files.isReadable(p1) 
				&& Files.isWritable(p1) && Files.isExecutable(p1);
		if (isRegularExecutableFile) {
			Path p2 = Paths.get(System.getProperty("user.home"), "test1.txt");
			//copy可以复制文件,复制文件夹不会复制文件夹里边的文件。选项有以下三个:
			//COPY_ATTRIBUTES: 复制文件的相关属性，真正的完全复制
			//REPLACE_EXISTING: 如果目标路径已存在，则覆盖掉。如果复制的是符号链接，只复制链接本身。
			//NOFOLLOW_LINKS: 如果是符号链接，只复制链接本身，不复制链接指向的目标文件。
			Files.copy(p1, p2, StandardCopyOption.COPY_ATTRIBUTES);
			
			//如果数据量不多的情况下可以使用readAllBytes一次性读取
			byte[] bytes = Files.readAllBytes(p2);
			System.out.println("read all bytes: " + new String(bytes, "utf-8"));
			
			//对应的也有move方法用于移动文件,参数多了一个:
			//ATOMIC_MOVE：原子文件操作，即该操作不能被打断或者部分地执行，该操作被完全执行或者执行失败
			Path p3 = Paths.get(System.getProperty("user.dir"), "test.txt");
			Files.move(p2, p3, StandardCopyOption.REPLACE_EXISTING);
			
			//可以通过readAllLines逐行读取
			List<String> list = Files.readAllLines(p3, StandardCharsets.UTF_8);
			System.out.print("read all lines: ");
			list.stream().forEach(System.out::println);
			
			//删除文件
			Files.delete(p3);
		}
		
		//读取文件属性
		BasicFileAttributes attr = Files.readAttributes(p1, BasicFileAttributes.class);
		
		System.out.println("---------- file attributes----------");
		System.out.println("creationTime: " + attr.creationTime());
		System.out.println("lastAccessTime:" + attr.lastAccessTime());
		System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
		System.out.println("isDirectory:" + attr.isDirectory());
		System.out.println("isOther:" + attr.isOther());
		System.out.println("isRegularFile:" + attr.isRegularFile());
		System.out.println("isSymbolicLink:" + attr.isSymbolicLink());
		System.out.println("size:" + attr.size());
		
		//获取文件类型
		String fileType = Files.probeContentType(p1);
		System.out.println("file type: " + fileType);
		
		//File->Path可以使用file.toPath()方法
		//Path->File可以使用path.toFile()方法
		
		//删除已存在的文件
		Files.deleteIfExists(p1);
	}
	
	//使用newBufferedWriter写入数据
	public static void writeFile(Path path) {
		Charset charset= Charset.forName("utf-8");
		String s = "Hello World";
		try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
			writer.write(s, 0, s.length());
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//使用newBufferedReader读取数据
	public static void readFile(Path path) {
		Charset charset= Charset.forName("UTF-8");
		//try-with-resource写法，因为文件流(Stream)实现了java.lang.AutoCloseable接口，
		//因此这种使用方法可以在退出try块之后自动执行close方法，无需手动执行close方法了。
		try(BufferedReader reader = Files.newBufferedReader(path, charset)) {
			String line = null;
			while ((line = reader.readLine()) != null){
				System.out.println(line);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//文件夹遍历
	public static void filesIterable() {
		System.out.println("----------文件夹遍历----------");
		Path p1 = Paths.get(System.getProperty("user.dir")); 
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(p1)) {
			for (Path file : stream) {
				System.out.println(file.getFileName());
			}
		} catch(IOException | DirectoryIteratorException e) {
			e.printStackTrace();
		}
		System.out.println("----------后缀查找----------");
		//支持后缀查找
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(p1, "*.{xml,jar}")) {
			for (Path file : stream) {
				System.out.println(file.getFileName());
			}
		} catch(IOException | DirectoryIteratorException e) {
			e.printStackTrace();
		}
		System.out.println("----------查找所有文件夹--------");
		//自定义查找
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			public boolean accept(Path file) throws IOException {
				return Files.isDirectory(file);
			}
		};
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(p1, filter)) {
			for (Path file : stream) {
				System.out.println(file.getFileName());
			}
		} catch(IOException | DirectoryIteratorException e) {
			e.printStackTrace();
		}
	}
	
	//通过FileVisitor接口遍历文件
	public static void filesVisitor() {
		Path p1 = Paths.get(System.getProperty("user.dir"));
		try {
			Files.walkFileTree(p1, new SimpleFiles()).forEach(path -> {
				System.out.println(path.toString());
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//继承实现了FileVisitor接口的SimpleFileVisitor类
	//方法返回值有:
	//CONTINUE: 在preVisitDirectory中返回的话就会进入visitFile方法
	//TERMINATE: 终止遍历
	//SKIP_SUBTREE: 在preVisitDirectory中返回的话就会跳过子目录，接下来的两个方法将不会被触发
	//SKIP_SIBLINGS: 与上边类似，会跳过兄弟目录，不会触发后续两个方法
	static class SimpleFiles extends SimpleFileVisitor<Path> {
		
		//在访问目录之前触发 
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			System.out.println("pre: " + dir.toAbsolutePath().toString());
			return super.preVisitDirectory(dir, attrs);
		}
		
		//在目录中的文件被访问时触发。该文件的BasicFileAttributes会被传递到该方法中。
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (attrs.isRegularFile()) {
				System.out.println(file.getFileName() + " size： " + attrs.size());
			}
			return super.visitFile(file, attrs);
		}
		
		//在目录中的所有条目被访问之后触发 
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			System.out.println("post: " + dir.getParent());
			return super.postVisitDirectory(dir, exc);
		}
		
		//当文件无法被访问时触发 
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			System.out.println(file.toAbsolutePath().toString() + " Exception: " + exc.getMessage());
			return super.visitFileFailed(file, exc);
		}
	}
	
	// 操作Paths
	public static void pathsExample() {
		String currentDir = System.getProperty("user.dir");
		//通过静态方法来创建,get方法可以接多个参数
		Path p1 = Paths.get(currentDir);
		Path p2 = Paths.get(currentDir, ".", "pom.xml");
		
		System.out.println("root: " + p2.getRoot() 
			+ "\t parent: " + p2.getParent() 
			+ "\t fileName: " + p2.getFileName());
		
		//normalize方法可以清除路径中的一些冗余字符(当前目录的"."以及父级目录的"..")，
		//normalize不能正确的定位到预期文件/目录，需要定位的话要使用toRealPath
		System.out.println("normalize: " + p2.normalize().toString());
		
		//toUri方法可以将Path转换成一个浏览器可以打开的路径
		System.out.println("Uri: " + p2.normalize().toUri().toString());
		//toAbsolutePath方法将Path转换成一个绝对路径,默认情况下是用的当前工作目录
		System.out.println("AbsolutePath: " + Paths.get("pom.xml").toAbsolutePath());
		//toRealPath方法返回存在文件的真实路径,包含了normalize功能
		try {
			System.out.println("RealPath: " + p2.toRealPath(LinkOption.NOFOLLOW_LINKS));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//resolve方法用来连接路径
		System.out.println("resolve: " + p1.resolve("pom.xml"));
		
		//relativize方法用以返回两个路径之间的相对路径,它会先从第一个路径的上一个层级开始往下寻找
		System.out.println("relativize: " + p2.relativize(p1));
		
		//Path还有equals,startsWith,endsWith这些方法，跟String的功能一样
	}
}
