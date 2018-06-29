package com.example.thirdparty.db;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Sorts.descending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

/*
 * MongoDB java driver 简单使用
 */

public class MongoTest {
	private MongoClient client;
	private MongoDatabase md;
	private MongoCollection<Document> coll;
	
	@Before
	public void setUp(){
		client = new MongoClient("localhost", 27017);
		//获取数据库，没有则自动创建
		md = client.getDatabase("test");
		//获取集合，没有则自动创建
		coll = md.getCollection("teachers");
	}
	
	@After
	public void setDown(){
		client.close();
	}
	
	@Test
	public void testInsert(){
		List<Document> teachers = new ArrayList<Document>();
		for(int i=1; i<=9; i++){
			Document teacher = new Document();
			teacher.put("tno", "no"+i);
			teacher.put("tname", "teacher"+i);
			teacher.put("age", i+20);
			
			teachers.add(teacher);
		}
		coll.insertMany(teachers);
	}
	
	@Test
	public void testSelect(){
		//获取集合数据总数
		System.out.println("count: "+coll.count());
		System.out.println("----------查询所有--------------");
		for(Document teacher: coll.find()){
			System.out.println(teacher.toJson());
		}
		System.out.println("-----------根据条件查询所需记录-------------");
		FindIterable<Document> docs = coll.find(or(gte("age", 25),eq("tno", "no2")));
		MongoCursor<Document> cursor = docs.iterator();
		while(cursor.hasNext()){
			System.out.println(cursor.next().toJson());
		}
		cursor.close();
		
		System.out.println("-----------根据关键字降序排序-------------");
		Document doc1 = coll.find(exists("tno")).sort(descending("tno")).first();
		System.out.println(doc1.toJson());
	}
	
	@Test
	public void testUpdate(){
		Document doc = coll.find(new Document("tno","no1")).first();
		System.out.println("before update: "+doc.toJson());
		//更新一条记录
		coll.updateOne( eq("tno", "no1"), new Document("$set", new Document("tname","t111") ) );
		doc = coll.find(new Document("tno","no1")).first();
		System.out.println("after update: "+doc.toJson());
		
		//更新多条记录
		UpdateResult updateResult = coll.updateMany(lt("age",25), new Document("$inc", new Document("age",10)));
		System.out.println(updateResult.getModifiedCount());
	}
	
	@Test
	public void testDelete(){
		//删除一条记录
		coll.deleteOne(eq("tno", "no1"));
		//删除多条记录
		DeleteResult deleteResult = coll.deleteMany(gte("age", 30));
		System.out.println(deleteResult.getDeletedCount());
	}
}
