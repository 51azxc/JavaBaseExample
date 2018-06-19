package com.example.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.SortingParams;

/*
 * jedis简单使用
 */

public class JedisTest {
	private JedisPool pool;
	private Jedis jedis;

	@Before
	public void setUp() throws Exception{
		pool = new JedisPool("127.0.0.1");
		jedis = pool.getResource();
	}
	
	@After
	public void finish() throws Exception{
		/* 清空数据 */
		jedis.flushDB();
	}
	
	@Test
	public void testString() throws Exception{
		/* ---基本操作--- */
		Assert.assertEquals("存数据", "OK", jedis.set("name", "a"));
		
		Assert.assertEquals("取数据", "a", jedis.get("name"));
		
		Assert.assertEquals("删数据, 返回删除数据个数", new Long(1), jedis.del("name"));
		
		jedis.setnx("name", "aa");
		Assert.assertEquals("键值不存在则存入", "aa", jedis.get("name"));
		
		Assert.assertEquals("追加数据", new Long(6), jedis.append("name", "1111"));
		
		//设定有效期
		jedis.setex("name", 1, "b");
		Assert.assertEquals("b", jedis.get("name"));
		TimeUnit.MILLISECONDS.sleep(1100);
		Assert.assertNull("有效期", jedis.get("name"));
		
		//按key,value方式存取多个map
		jedis.mset("name","abc","age","111");
		Assert.assertEquals(Arrays.asList("abc", "111"), jedis.mget("name", "age"));
		Assert.assertEquals("按照key截取value的值", "bc", jedis.getrange("name", 1, 2));
	}
	
	@Test
	public void testMap() throws Exception{
		/* ---操作Map--- */
		Map<String,String> map = new HashMap<String, String>();
		map.put("name", "a");
		map.put("age", "11");
		jedis.hmset("student", map);
		//第一个参数为key,后面至少跟一个参数为map的key,可以跟多个key
		Assert.assertEquals(Arrays.asList("a", "11"), jedis.hmget("student", "name", "age"));
		
		Assert.assertTrue("删除map中的某个值", jedis.hdel("student", "age") == 1);
		
		Assert.assertEquals("返回存放个数", new Long(1), jedis.hlen("student"));
		
		Assert.assertTrue("是否存在", jedis.exists("student"));
		
		HashSet<String> nameSet = new HashSet<String>(map.keySet());
		nameSet.remove("age");
		Assert.assertEquals("返回map中的所有键值", nameSet, jedis.hkeys("student"));
		
		Assert.assertEquals("返回map中的所有值", Arrays.asList("a"), jedis.hvals("student"));
		
		map.remove("age");
		Assert.assertEquals("返回所有map", map, jedis.hgetAll("student"));
	}
	
	@Test
	public void testList() throws Exception{
		/* ---操作列表list--- */
		
		//开始之前先删除所有相关数据
		jedis.del("students");
		//新数据放在左边
		jedis.lpush("students", "1");
		jedis.lpush("students", "2");
		jedis.lpush("students", "3");
		//排序,只对数字有效
		SortingParams sp = new SortingParams();
		sp.desc();
		sp.limit(0, 1);
		Assert.assertEquals(Arrays.asList("3"), jedis.sort("students", sp));
		
		//新数据放在右边
		jedis.rpush("students", "s4");
		//按key取出列表数据，最后一个参数-1表示所有数据
		Assert.assertEquals(Arrays.asList("3", "2", "1", "s4"), jedis.lrange("students", 0, -1));
		Assert.assertEquals("获取数组长度", new Long(4), jedis.llen("students"));
		Assert.assertEquals("截取指定区间的数据", Arrays.asList("3", "2"), jedis.lrange("students", 0, 1));
		Assert.assertEquals("指定下标修改,返回修改状态值", "OK", jedis.lset("students", 1, "123"));
		Assert.assertEquals("获取指定下标的值", "123", jedis.lindex("students", 1));
		Assert.assertEquals("删除指定下标的值,返回受影响的值个数", new Long(1), jedis.lrem("students", 1, "123"));
		Assert.assertEquals("删除指定区间以外的值", "OK", jedis.ltrim("students", 0, 1));
		//列表出栈
		Assert.assertEquals("列表出栈", "3", jedis.lpop("students"));
		Assert.assertEquals("1", jedis.lpop("students"));
		Assert.assertNull(jedis.lpop("students"));
	}
	
	@Test
	public void testSet() throws Exception{
		/* ---操作列表set--- */
		//存入数据
		jedis.sadd("ss", "aa");
		jedis.sadd("ss", "bb");
		jedis.sadd("ss", "cc");
		jedis.sadd("ss", "dd");
		jedis.sadd("ss", "ee");
		//移除数据
		jedis.srem("ss", "ee");
		Assert.assertFalse("判断元素是否存在于set中", jedis.sismember("ss", "ee"));
		Assert.assertEquals("返回元素个数", new Long(4), jedis.scard("ss"));
		
		//遍历
		System.out.println(jedis.smembers("ss"));
		//随机返回集合中一个元素,但不会删除这个元素
		System.out.println(jedis.srandmember("ss"));
		//随机出栈一个元素
		System.out.println(jedis.spop("ss"));
		
		jedis.sadd("st", "aa");
		jedis.sadd("st", "bbb");
		jedis.sadd("st", "dd");
		jedis.sadd("st", "ee");
		//交集
		System.out.println(jedis.sinter("ss","st"));
		//并集
		System.out.println(jedis.sunion("ss","st"));
		//差集
		System.out.println(jedis.sdiff("ss","st"));
		
		jedis.zadd("zs", 30, "a1");
		jedis.zadd("zs", 10, "a2");
		jedis.zadd("zs", 20, "a3");
		//根据中间的参数排序集合
		System.out.println(jedis.zrange("zs", 0, -1));
		System.out.println(jedis.zrevrange("zs", 0, -1));
	}
	
	@Test
	public void testObject() throws Exception {
		Student student1 = new Student("a", 12);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(student1);
		
		byte[] bytes = baos.toByteArray();
		
		//方法一 存储为string
		String str = Base64.encodeBase64String(bytes);
		//存储序列化对象
		Assert.assertEquals("OK", jedis.set("student", str));
		bytes = Base64.decodeBase64(jedis.get("student"));
		
		//方法二 直接存byte数组
		byte[] strBytes = "student".getBytes("UTF-8");
		Assert.assertEquals("OK", jedis.set(strBytes, bytes));
		Assert.assertNotNull(bytes = jedis.get(strBytes));
		
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream in = new ObjectInputStream(bais);
		Student student2 = (Student) in.readObject();
		
		Assert.assertEquals(student1.getName(), student2.getName());
	}
	
	@Test
	public void testSubscribe() throws Exception {
		//发布/订阅模型
		ExecutorService service = Executors.newFixedThreadPool(2);
		CountDownLatch publicLatch = new CountDownLatch(1);
		CountDownLatch subscribeLatch = new CountDownLatch(1);
		
		service.execute(new Runnable() {
			@Override
			public void run() {
				try {
					publicLatch.await();
					TimeUnit.SECONDS.sleep(1);
					jedis.publish("test", "Hello World!");
					TimeUnit.SECONDS.sleep(1);
					jedis.publish("test", "Hello");
					TimeUnit.SECONDS.sleep(1);
					jedis.publish("test", "quit");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		final JedisPubSub jedisPubSub = new JedisPubSub() {
			@Override
			public void onSubscribe(String channel, int subscribedChannels) {
				System.out.println("subscribe");
			}
			@Override
			public void onUnsubscribe(String channel, int subscribedChannels) {
				System.out.println("unsubscribe");
			}
			@Override
			public void onMessage(String channel, String message) {
				System.out.println("received: " + message);
				if ("quit".equalsIgnoreCase(message)) {
					this.unsubscribe();
					subscribeLatch.countDown();
				}
			}
		};
		
		service.execute(new Runnable() {
			@Override
			public void run() {
				Jedis subJedis = new Jedis("127.0.0.1");
				subJedis.subscribe(jedisPubSub, "test");
				subJedis.close();
			}
		});
		
		publicLatch.countDown();
		subscribeLatch.await();
		service.shutdown();
	}
}

class Student implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private int age;
	
	public Student(String name, int age) {
		this.name = name;
		this.age = age;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
}
