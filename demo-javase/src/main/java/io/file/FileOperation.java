package io.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/*
 * 使用IO/NIO对文件进行操作
 * 
 * 传统IO是基于字节流和字符流进行操作。它是面向流(Stream)的，这意味着它必须一次性读完流中所有的数据
 * NIO是基于Channel(通道)和Buffer(缓冲区)进行操作。它是面向缓冲区(Buffer)的，缓冲区中可以操作数据的选择性多一些，
 * 比如可以读取一部分，同时接着写入一部分，可以通过移动指针来确定操作数据的大小
 * 
 * Channel与Stream类似，不同点是Stream是单向的，Channel是双向的，也就是可读可写。
 * 
 * Buffer包含的主要变量元素:
 *   - capacity: 缓冲区数组的总长度
 *   - position: 下一个要操作的数据元素的位置
 *   - limit: 缓冲区数组中不可操作的下一个元素的位置(limit<=capacity)
 *   - mark: 用于记录当前position的前一个位置(Default: -1)
 * 
 * 例如通过ByteBuffer buffer = ByteBuffer.allocate(1024)这样可以创建一块长度为1024个字节的Buffer。
 * 这时position的位置为0，表示可以从0开始读取或者写入数据，而limit/capacity的位置则为1023。
 * 然后通过buffer.put(128)方法写入128个字节数据，这时position则到了128这个位置，
 * 接下来调用buffer.flip()方法，position则又回到了0这个位置，而limit则标识到了128。
 * 然后就可以使用channel.write(buffer)将buffer前边的(0-127)这些字节数据写入到channel中了。
 *   
 */
public class FileOperation {

	public final static String FILE_PATH = System.getProperty("user.home") + "/test.txt";
	
	public static void main(String[] args) {
		File file = new File(FILE_PATH);
		
		System.out.println("file path: " + FILE_PATH);
		System.out.println("----------IO----------");
		writeFileByIO(file);
		readFileByIO();
		System.out.println("----------NIO----------");
		writeFileByNIO(file);
		readFileByNIO();
		
		//删除文件
		if (file.exists()) {
			file.delete();
		}
	}
	
	//使用OutputStream写入文件
	public static void writeFileByIO(File file) {
		OutputStream output = null;
		try {
			//文件存在则追加写入
			output = new BufferedOutputStream(new FileOutputStream(file, file.exists()));
			byte[] bytes = "传统IO写入\r\n".getBytes("utf-8");
			output.write(bytes, 0, bytes.length);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//使用InputStream读取文件内容
	public static void readFileByIO() {
		InputStream input = null;
        try {
        	input = new BufferedInputStream(new FileInputStream(FILE_PATH));
            byte[] bytes = new byte[1024];
            int bytesRead;
            StringBuilder sb = new StringBuilder();
            while (((bytesRead = input.read(bytes)) != -1)) {
            	sb.append(new String(bytes, 0, bytesRead));
            }
            System.out.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input!=null) {
                	input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

	//使用FileChannel及ByteBuffer写入文件
	public static void writeFileByNIO(File file) {
		FileOutputStream output = null;
		FileChannel channel = null;
		try {
			output = new FileOutputStream(file, file.exists());
			//从IO流中获取通道
			channel = output.getChannel();
			//分配空间
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			byte[] bytes = "NIO写入\r\n".getBytes("utf-8");
			buffer.put(bytes);
			//flip方法将缓冲区limit设置成当前position位置，将position位置设置为0，
			//这样就可以开始获取缓存区中写入的数据
			buffer.flip();
			//遍历缓冲区中的数据，并写入到通道中
			while(buffer.hasRemaining()) {
				channel.write(buffer);
			}
			//clear方法将缓冲区position设置为0，limit设置成capacity,
			//这意味着清空缓冲区，下一次将从0开始写数据
			buffer.clear();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (channel != null) {
					channel.close();
				}
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//使用FileChannel及ByteBuffer读取文件内容
	public static void readFileByNIO() {
		FileInputStream input = null;
		FileChannel channel = null;
		//解决中文乱码问题
		Charset charset = Charset.forName("utf-8");  
        CharsetDecoder decoder = charset.newDecoder();  
		try {
			input = new FileInputStream(FILE_PATH);
			channel = input.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			//处理中文问题，没有可以不用写
			CharBuffer charBuffer = CharBuffer.allocate(1024);
			StringBuilder sb = new StringBuilder();
			//从通道中读取数据并写入到缓冲区中
			while((channel.read(buffer)) != -1) {
				//确定读取范围
				buffer.flip();
				//处理中文问题
				decoder.decode(buffer, charBuffer, false);
				charBuffer.flip();
				while(charBuffer.hasRemaining()) {
					//获取内容
					sb.append((char)charBuffer.get());
				}
				//compact与clear不同的是，clear会直接丢弃未读取的数据，
				//而compact会将缓冲区中未读取的数据复制到缓冲区起始处,
				//position则会到最后一个未读元素后边。limit跟capacity一样
				//这样开始写数据就不会覆盖掉未读数据
				buffer.compact();
				charBuffer.compact();
			}
			System.out.println(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (channel != null) {
					channel.close();
				}
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
