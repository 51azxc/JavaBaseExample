package other;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/*
 * 操作Properteis文件
 */
public class PropertiesTest {

	//读取方法一
	public static void readProperties1() throws IOException{
		InputStream in = new BufferedInputStream(new FileInputStream("src/test.properties"));
		// 1. 使用class变量的getResourceAsStream()方法,这里需要注意包名路径
		//InputStream in = PropertyTest.class.getResourceAsStream("../../../test.properties");
		// 2. 使用class.getClassLoader()所得到的java.lang.ClassLoader的getResourceAsStream()方法
		//InputStream in = PropertyTest.class.getClassLoader().getResourceAsStream("test.properties");
		// 3. 使用java.lang.ClassLoader类的getSystemResourceAsStream()静态方法
		//InputStream in = ClassLoader.getSystemResourceAsStream("test.properties");
		// 4. 如果在Servlet中可以使用javax.servlet.ServletContext的getResourceAsStream()方法
		//InputStream in = context.getResourceAsStream("test.properties");
		Properties prop = new Properties();
		prop.load(in);
		System.out.println(prop.getProperty("test"));
		in.close();
	}
	//读取方法二
	public static void readProperties2(){
		// 1. 使用java.util.ResourceBundle类的getBundle()方法,这里的文件放在src目录下，如果放在其他包内，写法跟类一样，即com.test
		ResourceBundle rb = ResourceBundle.getBundle("test", Locale.getDefault());
		// 2. 使用java.util.PropertyResourceBundle类的构造函数
		//InputStream in = new BufferedInputStream(new FileInputStream("src/test.properties"));
		//ResourceBundle rb = new PropertyResourceBundle(in);
		System.out.println(rb.getString("test"));
	}
	//写入方法
	public static void writeProperties() throws IOException{
		InputStream in = new BufferedInputStream(new FileInputStream("src/test.properties"));
		Properties prop = new Properties();
		prop.load(in);
		in.close();
		
		prop.setProperty("test", "Hello World");
		OutputStream os = new FileOutputStream("src/test.properties");
		prop.store(os, "test write");
		os.close();
		System.out.println("after: "+prop.getProperty("test"));
	}
}
