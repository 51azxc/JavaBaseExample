package other;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/*
 * Base64编码相关操作
 */

public class Base64Test {

	public static void main(String[] args) {
		
	}
	
	//文件流 -> base64
	public static String toBase64(String path) throws Exception{
		String res = "";
		InputStream in = new FileInputStream(path);
		// in.available()返回文件的字节长度
		byte[] bytes = new byte[in.available()];
		// 将文件中的内容读入到数组中
		in.read(bytes);
		//将字节流数组转换为字符串
		res = Base64Util.encode(bytes);
		in.close();
		return res;
	}
	//将图片转成Base64编码
	public static String useImageIOtoBase64(String path) throws Exception{
		String res = "";
		BufferedImage bi = ImageIO.read(new File(path));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bi, "jpg", baos);
		byte[] bytes = baos.toByteArray();
		String result = Base64Util.encode(bytes).trim();
		//去掉得到的base64编码的换行符号
		Pattern p = Pattern.compile("\\s*|\t|\r|\n"); 
		Matcher m = p.matcher(result);
		res = m.replaceAll("");
		return res;
	}
	
	//Base64 -> 文件
	public static void fromBase64(String str,String path) throws Exception{
		byte[] bytes = Base64Util.decode(str);
		FileOutputStream out = new FileOutputStream(path);
		out.write(bytes);
		out.flush();
		out.close();
	}
}

class Base64Util {
	private final static char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
			.toCharArray();

	private static int[] toInt = new int[128];

	static {
		for (int i = 0; i < ALPHABET.length; i++) {
			toInt[ALPHABET[i]] = i;
		}
	}

	public static String encode(byte[] buf) {
		int size = buf.length;
		char[] ar = new char[((size + 2) / 3) * 4];
		int a = 0;
		int i = 0;
		while (i < size) {
			byte b0 = buf[i++];
			byte b1 = (i < size) ? buf[i++] : 0;
			byte b2 = (i < size) ? buf[i++] : 0;

			int mask = 0x3F;
			ar[a++] = ALPHABET[(b0 >> 2) & mask];
			ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
			ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
			ar[a++] = ALPHABET[b2 & mask];
		}
		switch (size % 3) {
		case 1:
			ar[--a] = '=';
		case 2:
			ar[--a] = '=';
		}
		return new String(ar);
	}

	public static byte[] decode(String s) {
		int delta = s.endsWith("==") ? 2 : s.endsWith("=") ? 1 : 0;
		byte[] buffer = new byte[s.length() * 3 / 4 - delta];
		int mask = 0xFF;
		int index = 0;
		for (int i = 0; i < s.length(); i += 4) {
			int c0 = toInt[s.charAt(i)];
			int c1 = toInt[s.charAt(i + 1)];
			buffer[index++] = (byte) (((c0 << 2) | (c1 >> 4)) & mask);
			if (index >= buffer.length) {
				return buffer;
			}
			int c2 = toInt[s.charAt(i + 2)];
			buffer[index++] = (byte) (((c1 << 4) | (c2 >> 2)) & mask);
			if (index >= buffer.length) {
				return buffer;
			}
			int c3 = toInt[s.charAt(i + 3)];
			buffer[index++] = (byte) (((c2 << 6) | c3) & mask);
		}
		return buffer;
	}

}
