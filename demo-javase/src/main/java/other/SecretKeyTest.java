package other;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
 * 一些加密操作
 * 
 * 常用的对称加密算法有DES/3DES/AES
 * 
 * DES(Data Encryption Standard), 数据加密算法, 已被破解
 * 参数有Key: 8字节共64位, 密钥; Data: 需要加密/解密的数据; Mode: 工作方式, 加密/解密
 * 
 * 3DES(Triple Data Encryption Algorithm), 三重数据加密算法, 
 * 通过增加DES的密钥长度来避免类似的攻击
 * 
 * AES(Advanced Encryption Standard), 高级加密标准。目前最安全
 * 
 * 5种分组模式:
 * EBC(电子密码本模式) 
 *   - 优点: 简单/有利于并行计算/误差不会被传送  
 *   - 缺点: 不能隐藏明文模式/可能对明文进行主动攻击
 * CBC(密码分组链接模式) 
 *   - 优点: 不容易主动攻击/适合传输长度长的报文/SSL、IPSec的标准
 *   - 缺点: 不利于并行计算/误差传递/需要初始化向量IV
 * CFB(密码发反馈模式） 
 *   - 优点: 隐藏明文模式/分组密码转化为流模式/可以及时加密传送小于分组的数据
 *   - 缺点: 不利于并行计算/误差传递/唯一的IV
 * OFB(输出反馈模式) 
 *   - 优点: 跟CFB一样
 *   - 缺点: 不利于并行计算/误差传递/明文容易被主动攻击
 * CTR(计数模式): 简单/安全/无填充 
 *   
 * 3种填充模式: 
 *   NoPadding(不填充), 
 *   ZerosPadding(全部填充为0的字节), 
 *   PKCS5Padding(每个填充的字节都记录了填充的总字节数)
 * 
 */
public class SecretKeyTest {
	
	private final static String KEY_ALGORITHM_DES = "DES";
	private final static String KEY_ALGORITHM_3DES = "DESede";
	private final static String KEY_ALGORITHM_AES = "AES";
	//算法名称/加密模式/填充方式 
	private final static String MODE_DES = "DES/ECB/PKCS5Padding";
	private final static String MODE_3DES = "DESede/CBC/PKCS5Padding";
	private final static String MODE_AES = "AES/ECB/PKCS5Padding";
	
	public static void main(String[] args) throws Exception {
		String str = "HelloWorld";
		System.out.println("origin: " + str);
		
		System.out.println("----------DES加密解密----------");
		String desKey = "ABCDEFG012345678";
		String desEncryptStr = encryptWithDES(str, desKey);
		System.out.println("DES Encrypt: " + desEncryptStr);
		String desDecryptStr = decryptWithDES(desEncryptStr, desKey);
		System.out.println("DES Decrypt: " + desDecryptStr);
		
		System.out.println("----------3DES加密解密----------");
		String desedeKey = "ABCDEFG0123456789abcdefg";
		//CBC模式需要添加向量，作用跟盐(salt)差不多，添加密码复杂度
		String iv = "01234567";
		String desedeEncryptStr = encryptWith3DES(str, desedeKey, iv);
		System.out.println("3DES Encrypt: " + desedeEncryptStr);
		String desedeDesDecryptStr = decryptWith3DES(desedeEncryptStr, desedeKey, iv);
		System.out.println("3DES Decrypt: " + desedeDesDecryptStr);
		
		System.out.println("----------AES加密解密----------");
		//使用AES-128-ECB加密模式，key需要为16位
		String aesKey = "0123456789abcdef";
		String aesEncryptStr = encryptWithAES(str, aesKey);
		System.out.println("AES Encrypt: " + aesEncryptStr);
		String aesDecryptStr = decryptWithAES(aesEncryptStr, aesKey);
		System.out.println("AES Decrypt: " + aesDecryptStr);
	}
	
	public static String encryptWithDES(String data, String key) throws Exception {
	    Cipher cipher = Cipher.getInstance(MODE_DES);
		cipher.init(Cipher.ENCRYPT_MODE, generateDESKey(key), new SecureRandom());
		byte[] bytes = cipher.doFinal(data.getBytes("utf-8"));
		//使用JDK自带的Base64工具类编码
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	public static String decryptWithDES(String data, String key) throws Exception {
	    Cipher cipher = Cipher.getInstance(MODE_DES);
	    cipher.init(Cipher.DECRYPT_MODE, generateDESKey(key));
	    //使用JDK自带的Base64工具类解码
	    byte[] bytes = Base64.getDecoder().decode(data);
	    return new String(cipher.doFinal(bytes), "utf-8");
	}
	
	private static SecretKey generateDESKey(String key) throws Exception {
		DESKeySpec keySpec = new DESKeySpec(key.getBytes("utf-8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM_DES);
	    SecretKey secureKey = keyFactory.generateSecret(keySpec);
	    return secureKey;
	}
	
	public static String encryptWith3DES(String data, String key, String iv) throws Exception {
		Cipher cipher = Cipher.getInstance(MODE_3DES);
		IvParameterSpec ivParaSepc = new IvParameterSpec(iv.getBytes("utf-8"));
		cipher.init(Cipher.ENCRYPT_MODE, gengerate3DESKey(key), ivParaSepc);
		byte[] bytes = cipher.doFinal(data.getBytes("utf-8"));
		//使用JDK自带的Base64工具类编码
		return Base64.getEncoder().encodeToString(bytes);
	}
	public static String decryptWith3DES(String data, String key, String iv) throws Exception {
		Cipher cipher = Cipher.getInstance(MODE_3DES);
		IvParameterSpec ivParaSepc = new IvParameterSpec(iv.getBytes("utf-8"));
	    cipher.init(Cipher.DECRYPT_MODE, gengerate3DESKey(key), ivParaSepc);
	    //使用JDK自带的Base64工具类解码
	    byte[] bytes = Base64.getDecoder().decode(data);
	    return new String(cipher.doFinal(bytes), "utf-8");
	}
	
	private static SecretKey gengerate3DESKey(String key) throws Exception {
		DESedeKeySpec keySpec = new DESedeKeySpec(key.getBytes("utf-8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM_3DES);
	    SecretKey secureKey = keyFactory.generateSecret(keySpec);
	    return secureKey;
	}
	
	//使用AES算法加密
	public static String encryptWithAES(String data, String key) throws Exception {
		SecretKeySpec spec = new SecretKeySpec(key.getBytes("utf-8"), KEY_ALGORITHM_AES);
		Cipher cipher = Cipher.getInstance(MODE_AES);
		cipher.init(Cipher.ENCRYPT_MODE, spec);
		byte[] bytes  = cipher.doFinal(data.getBytes("utf-8"));
		//使用JDK自带的Base64工具类编码
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	public static String decryptWithAES(String data, String key) throws Exception {
		SecretKeySpec spec = new SecretKeySpec(key.getBytes("utf-8"), KEY_ALGORITHM_AES);
		Cipher cipher = Cipher.getInstance(MODE_AES);
		cipher.init(Cipher.DECRYPT_MODE, spec);
		//使用JDK自带的Base64工具类解码
		byte[] bytes = Base64.getDecoder().decode(data);
		return new String(cipher.doFinal(bytes), "utf-8");
	}

}
