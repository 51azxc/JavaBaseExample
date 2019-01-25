package other;

import java.io.File;

/*
 * java一些零碎的小知识点
 */
public class OtherTest {

	/*
	 * 传递Class类型
	 */
	public void passClass(Class<?> clazz) {
		System.out.println(clazz.getClass().getName());
	}
	
	/*
	 * 利用类名获取
	 */
	public void passClass(String className) throws ClassNotFoundException {
		System.out.println(Class.forName(className).getClass().getName());
	}
	
	/*
	 * 解决split取到空值的问题
	 */
	public void split() {
		String str = "|1|2|3|4|";
		//length: 6
		System.out.println(str.split("\\|",-1).length);
	}
	
	/*
	 * 获取当前路径
	 */
	public void getPath() {
		// 1. 通过user.dir来获取
		/*
		 * System.getProperty()部分参数说明
		 * | file.separator | 文件目录分割符 | 
         * | java.home | jdk所在目录 | 
         * | java.io.tmpdir | 临时文件目录 | 
         * | java.library.path | 系统path参数 |
         * | java.version  | java 版本 |
         * | line.separator | 系统默认换行符 |
         * | path.separator | 路径分隔符 |
         * | os.name | 系统名 |
         * | os.arch | 系统指令集 |
         * | os.version | 系统内核版本 |
         * | user.name | 当前用户名 |
         * | user.home | 当前用户目录 |
         * | user.dir | 当前工作空间路径 |
		 */
		System.out.println(System.getProperty("user.dir"));
		
		// 2. 利用File的函数获取
		File f = new File("");
		try{
		    //这里两者一样，如果上面File("")的路径为..，则此函数获取的为当前目录的父目录路径，下面的函数则还是当前目录
			System.out.println(f.getCanonicalPath());
			System.out.println(f.getAbsolutePath());
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// 3. 获取编译后的classes文件所在的绝对路径
		System.out.println(OtherTest.class.getResource(""));
	}
}
