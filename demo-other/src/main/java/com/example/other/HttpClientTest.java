package com.example.other;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpClientTest {
	
	/*
	 * 使用HttpURLConnection连接
	 * 1. HttpURLConnection的`connect()`函数只建立了一个与服务器的tcp连接，并没有实际发送http请求。
	 *    getInputStream()这个函数里面才正式发送出去(post/get)
     * 2. 在用POST方式发送URL请求时，URL请求参数的设定顺序非常重要，对connection对象的一切配置（setXXX）都必须要在connect()函数执行之前完成。'
     *    outputStream的写必须要在inputStream的读之前。这些顺序实际上是由http请求的格式决定的。
     *    如果inputStream读操作在outputStream的写操作之前，会抛出异常：java.net.ProtocolException
     * 3. http请求实际上由两部分组成，一个是http头，所有关于此次http请求的配置都在http头里面定义，
     *    一个是正文content。connect()函数会根据HttpURLConnection对象的配置值生成http头部信息，
     *    因此在调用connect函数之前，就必须把所有的配置准备好。
     * 4. 在http头后面紧跟着的是http请求的正文，正文的内容是通过outputStream流写入的，实际上outputStream不是一个网络流，
     *    充其量是个字符串流，往里面写入的东西不会立即发送到网络，而是存在于内存缓冲区中，待outputStream流关闭时，
     *    根据输入的内容生成http正文。至此，http请求的东西已经全部准备就绪。在`getInputStream()`函数调用的时候，
     *    就会把准备好的http请求正式发送到服务器了，然后返回一个输入流，用于读取服务器对于此次http请求的返回信息。
     *    由于http请求在getInputStream的时候已经发送出去了（包括http头和正文），
     *    因此在`getInputStream()`函数之后对connection对象进行设置（对http头的信息进行修改）或者写入outputStream（对正文进行修改）都是没有意义的了，
     *    执行这些操作会导致异常的发生。
	 */
	public static String useHttpURLConnection(String urlStr, String data){
		
		//连接主机的超时时间ms
		System.setProperty("sun.net.client.defaultConnectTimeout", "60000"); 
		//从主机读取数据的超时时间ms
		System.setProperty("sun.net.client.defaultReadTimeout", "60000"); 
		
		String res = "";
		BufferedWriter bw = null;
		BufferedReader br = null;
		HttpURLConnection httpUrlConnection = null;
		try{
			URL url = new URL(urlStr);
			
			URLConnection rulConnection = url.openConnection();
			// 此处的urlConnection对象实际上是根据URL的请求协议(此处是http)生成的URLConnection类的子类HttpURLConnection,
			// 故此处最好将其转化为HttpURLConnection类型的对象,以便用到HttpURLConnection更多的API.如下:
			httpUrlConnection = (HttpURLConnection) rulConnection;
			
			// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在http正文内，因此需要设为true, 默认false
			httpUrlConnection.setDoOutput(true);
			
			// 设置是否从httpUrlConnection读入，默认true
			httpUrlConnection.setDoInput(true);
			
			// Post 请求不能使用缓存
			httpUrlConnection.setUseCaches(false);
			
			// 设定传送的内容类型是json格式的数据文件
			//(如果不设此项,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
			httpUrlConnection.setRequestProperty("Content-type", "application/json");
			
			//设置连接主机超时（单位：毫秒）
			httpUrlConnection.setConnectTimeout(30000);
			//设置从主机读取数据超时（单位：毫秒）
			httpUrlConnection.setReadTimeout(30000);
			
			// 设定请求的方法为"POST"，默认是GET
			httpUrlConnection.setRequestMethod("POST");
			
			// 连接，从上述url.openConnection()至此的配置必须要在connect之前完成
			//httpUrlConnection.connect();
			
			// 此处getOutputStream会隐含的进行connect,即不调用connect方法也可
			bw = new BufferedWriter(new OutputStreamWriter(httpUrlConnection.getOutputStream(),"utf-8"));
			// 向对象输出流写出数据，这些数据将存到内存缓冲区中
			bw.write(data);
			bw.flush();
			
			// 调用HttpURLConnection连接对象的getInputStream()函数,将内存缓冲区中封装好的完整的HTTP请求电文发送到服务端。
			// 实际发送请求的代码段就是httpConn.getInputStream()方法。此已调用,本次HTTP请求已结束
			br = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(),"utf-8"));
			// 上边的httpConn.getInputStream()方法已调用,本次HTTP请求已结束
			
			char[] c = new char[1024];
			StringBuffer sb = new StringBuffer();
			int len;
			while((len = br.read(c))>0){
				sb.append(c,0,len);
			}
			res = sb.toString();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if(bw!=null) bw.close();
				if(br!=null) br.close();
				httpUrlConnection.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	/*
	 * 使用HttpClient完成POST请求
	 */
	public static String useHttpClientPost(String url) {
		String res = "";
		CloseableHttpClient client = HttpClients.createDefault();
		try{
			RequestConfig config = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();
			HttpPost request = new HttpPost(url);
			request.setConfig(config);
			//提交json格式数据
			//request.addHeader("content-type", "application/json");
			//request.setEntity(new StringEntity(data,"utf-8"));
		    //传统表单提交数据
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();  
		    formparams.add(new BasicNameValuePair("username", "aaa"));
		    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");  
		    request.setEntity(entity);
			HttpResponse response = client.execute(request);
			res = EntityUtils.toString(response.getEntity(),"utf-8");
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	
	
}
