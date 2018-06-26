package other;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
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
	
	public static void main(String[] args) throws Exception {
		System.out.println("----------AES加密解密----------");
		String str = "HelloWorld";
		//使用AES-128-ECB加密模式，key需要为16位
		String aesKey = "0123456789abcdef";
		String aesEncryptStr = useAESEncrypt(str, aesKey);
		System.out.println("DES Encrypt: " + aesEncryptStr);
		String aesDecryptStr = useAESDecrypt(aesEncryptStr, aesKey);
		System.out.println("DES Decrypt: " + aesDecryptStr);
	}
	
	public static String useDESEncrypt(String data, String key) throws Exception {
		final DESKeySpec desKey = new DESKeySpec(key.getBytes("utf-8"));
		final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
	    final SecretKey securekey = keyFactory.generateSecret(desKey);
	    Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, securekey);
		return Base64Util.encode(cipher.doFinal(data.getBytes("utf-8")));
	}
	
	//使用AES算法加密
	public static String useAESEncrypt(String data, String key) throws Exception {
		SecretKeySpec spec = new SecretKeySpec(key.getBytes("utf-8"), "AES");
		//算法名称/加密模式/填充方式 
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, spec);
		byte[] bytes  = cipher.doFinal(data.getBytes("utf-8"));
		//base64编码
		return Base64Util.encode(bytes);
	}
	
	public static String useAESDecrypt(String data, String key) throws Exception {
		SecretKeySpec spec = new SecretKeySpec(key.getBytes("utf-8"), "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, spec);
		//base64解码
		byte[] bytes = Base64Util.decode(data);
		return new String(cipher.doFinal(bytes), "utf-8");
	}

}
