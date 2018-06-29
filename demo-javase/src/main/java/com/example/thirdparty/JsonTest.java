package com.example.thirdparty;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;
import com.google.gson.reflect.TypeToken;

/*
 * 操作JSON
 */
public class JsonTest {
	public static void main(String[] args) {
		useGson();
	}

	/*
	 * 使用Gson将对象转换成json字符串
	 */
	public static void useGson() {
		GsonUser user = new GsonUser();
		user.setUsername("a");
		user.setPassword("b");
		user.setStatus("0");

		Gson g1 = new Gson();
		System.out.println(g1.toJson(user));
		// result: {"user":"a","password":"b","status":"0"}

		Gson g2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation() // 没有标注@Expose注解的属性将不被导出
				.create();
		System.out.println(g2.toJson(user));
		// result:{"user":"a","password":"b"}
		
		// Gson初始化设置其他属性
		Gson g3 = new GsonBuilder()
		  // 支持Map的key为复杂对象的形式
		  .enableComplexMapKeySerialization()
		  // 当需要序列化的值为空时，采用null映射，否则会把该字段省略
		  .serializeNulls() 
		  // 转换日期格式
		  .setDateFormat("yyyy-MM-dd HH:mm:ss") 
		  // 段首字母大写,如使用@SerializedName注解的不会生效
		  .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
		  // 对json结果格式化输出
		  .setPrettyPrinting()
		  // 对于标注@Since注解的字段来决定版本是否对该字段进行序列化/反序列化，
		  // 对于标注@Until注解的字段来决定当前版本是否删除该字段
		  .setVersion(1.2)
		.create();
		System.out.println(g3.toJson(user));
	}

	/*
	 * Gson对Map的转换
	 */
	public static void mapToGson(String s) {
		Type type = new TypeToken<Map<String, GsonUser>>() {
		}.getType();
		Map<String, GsonUser> m = new Gson().fromJson(s, type);
		for (Map.Entry<String, GsonUser> me : m.entrySet()) {
			System.out.println(me.getKey() + " : " + me.getValue().getUsername());
		}
	}

	static class GsonUser {
		@Expose
		@SerializedName("user") // 重命名属性
		private String username;
		@Expose(serialize = true, deserialize = false) // 序列化时使用，反序列化不使用
		private String password;
		@Since(1.1) // 根据版本来决定是否序列化/反序列化
		private String status;

		public String getUsername() {
			return this.username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public void setStatus(String status) {
			this.status = status;
		}

	}
}
