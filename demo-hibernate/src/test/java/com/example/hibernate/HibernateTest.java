package com.example.hibernate;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HibernateTest {
	private Session session;

	@Before
	public void before() {
		// 使用getCurrentSession方法对于crud操作必须添加事务，openSession需手动添加事物及关闭session
		session = HibernateUtil.getSessionFactory().getCurrentSession();
	}
	@After
	public void after() {
		session.close();
	}

	@Test
	public void testInsert() {
		Student s = new Student();
		s.setSname("b");
		s.setAge(14);
		session.beginTransaction();
		session.save(s);
		session.getTransaction().commit();
	}

	@Test
	public void testUpdate1() {
		session.beginTransaction();
		Student s = (Student) session.get(Student.class, 1);
		if (s != null) {
			System.out.println(s.getAge());
			s.setAge(12);
		}
		session.getTransaction().commit();
	}

	@Test
	public void testUpdate2() {
		String hql = "update Student s set s.age = :age where s.sname=:name";
		session.beginTransaction();
		Query q = session.createQuery(hql);
		q.setParameter("age", 15);
		q.setParameter("name", "a");
		int i = q.executeUpdate();
		System.out.println(i);
		session.getTransaction().commit();
	}

	@Test
	public void testDelete() {
		Transaction t = session.beginTransaction();
		Student s = (Student) session.get(Student.class, 1);
		session.delete(s);
		t.commit();
	}
	
	//查询所有字段
	@Test
	public void testSelectAllFields() {
		String hql = "from Student";      
		Query query = session.createQuery(hql);      
		@SuppressWarnings("unchecked")
		List<Student> students = query.list();      
		for(Student student : students) {      
		  System.out.println(student.getSname() + " " + student.getSid()+ " " + student.getAge());     
		}
	}
	
	//查询单个字段
	@Test
	public void testSelectOneField() {
		String hql = "select sname from Student";      
		Query query = session.createQuery(hql);
		@SuppressWarnings("unchecked")
		List<String> list = query.list();      
		for(String str : list) {      
		   System.out.println(str);      
		}
	}
	
	//查询多个字段
	@Test
	public void testSelectSomeFields() {
		String hql = "select sname,age from Student";      
		Query query = session.createQuery(hql);
		//默认查询出来的list里存放的是一个Object数组      
		@SuppressWarnings("unchecked")
		List<Object[]> list = query.list();      
		for(Object[] object : list) {      
		    String sname = (String)object[0];      
		    int age = (int)object[1];      
		 System.out.println(sname + " : " + age);      
		}
	}
	
	//默认查询结果以List形式返回
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testSelectResultAsList() {
		//查询其中几个字段,添加new list(),注意list里的l是小写的,也不需要导入包。
		//这样通过query.list()出来的list里存放的不再是默认的Object数组了，而是List集合了  
		String hql = "select new list(sname,age) from Student";   
		Query query = session.createQuery(hql);    
		List<List> list = query.list();   
		for(List user : list) {   
		  String sname = (String)user.get(0);   
		  int age = (int)user.get(1);   
		  System.out.println(sname + " : " + age);   
		}
	}
	
	//默认查询结果以Map形式返回
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testSelectResultAsMap() {
		//查询其中几个字段,添加new map()
		String hql = "select new map(sname,age) from Student";   
		Query query = session.createQuery(hql);    
		List<Map> list = query.list();   
		for(Map user : list) {
		  //一条记录里所有的字段值都是map里的一个元素,key是字符串0,1,2,3....，value是字段值      
		  //如果将hql改为："select new map(sname as sname,age as age) from Student",那么key将不是字符串0,1,2...了，而是"sname","age"了    
		  String sname = (String)user.get("0");   
		  int age = (int)user.get("1");   
		  System.out.println(sname + " : " + age);   
		}
	}
	
	//默认查询结果以自定义类返回
	@SuppressWarnings("unchecked")
	@Test
	public void testSelectResultAsObject() {
		//必须加包名，且自定义类必须要有相应的带参构造函数
		String hql = " select new  com.example.hibernate.Student(sname,age) from Student";
		Query query = session.createQuery(hql); 
		List<Student> students = query.list();
		for (Student student: students) {
			System.out.println(student.getSname() + " : " + student.getAge());
		}
	}
	
	//条件查询
	@Test
	public void testSelect1() {
		String hql = "from Student where sname = ? and age = ?"; 
		Query query = session.createQuery(hql);
		query.setString(0, "a");
		query.setInteger(1, 14);
		Student s = (Student) query.uniqueResult();
		System.out.println(s.getSname() + " " + s.getAge());
	}
	
	//条件查询 自定义参数名
	@Test
	public void testSelect2() {
		String hql = "from Student s where s.sname = :name";
		Query q = session.createQuery(hql);
		//对于某些参数类型 setParameter()方法可以更具参数值的Java类型，
		//猜测出对应的映射类型，因此这时不需要显示写出映射类型
		//但是对于一些类型就必须写明映射类型，比如 java.util.Date类型，
		//因为它会对应Hibernate的多种映射类型，比如Hibernate.DATA或者 Hibernate.TIMESTAMP
		q.setParameter("name", "b");
		Student s = (Student) q.uniqueResult();
		System.out.println(s.getSname() + " " + s.getAge());
	}
	
	//数组条件查询
	@SuppressWarnings("unchecked")
	@Test
	public void testSelectList() {
		String hql = "from Student s where s.sname in (:slist)";
		Query query = session.createQuery(hql);
		//a可以是数组,也可以是List
		query.setParameterList("slist", Arrays.asList("a","b"));
		List<Student> students = query.list();
		for (Student student: students) {
			System.out.println(student.getSname() + " : " + student.getAge());
		}
	}
	
	//获取最大值
	@Test
	public void testSelectMax() {
		//hql一定要加上别名
		String hql = "select max(s.sid) from Student s";
		int max1 = ((Long)session.createQuery(hql).uniqueResult()).intValue();
		//criteria
		int max2 = ((Long)session.createCriteria(Student.class)
				.setProjection( Projections.projectionList().add(Projections.max("sid" ) ) ).uniqueResult()).intValue();
		Assert.assertEquals(max1, max2);
	}
	
	//Criteria查询
	@SuppressWarnings("rawtypes")
	@Test
	public void testSelectCriteria() {
		List list1 = session.createCriteria(Student.class)
		    .add( Restrictions.in("sname", new String[] { "a", "b", "c" }))
		    .add( Restrictions.or(
		        Restrictions.eq("age", new Integer(11)),
		        Restrictions.isNull("age")
		) ).list();
		System.out.println(list1.size());
		
		//Property实例是获得一个条件的另外一种途径。通过调用Property.forName()创建一个Property。
		Property age = Property.forName("age");
		List list2 = session.createCriteria(Student.class)
		    .add( Property.forName("name").in( new String[] { "a", "b", "c" }))
		    .add( Restrictions.disjunction().add(age.isNull())
		).list();
		System.out.println(list2.size());
	}
	
	//Criteria排序
	@Test
	public void testSelectCriteriaOrder() {
		Criteria c = session.createCriteria(Student.class).addOrder(Order.asc("age")).setMaxResults(10);
		System.out.println(c.list().size());
	}
	
	//SQL查询
	@SuppressWarnings("rawtypes")
	@Test
	public void testSelectBySQL() {
		//返回一个Object数组(Object[])组成的List，数组每个元素都是student表的一个字段值。
		//Hibernate会使用ResultSetMetadata来判定返回的标量值的实际顺序和类型。
		session.createSQLQuery("select sname, age from student").list();
		//返回实体类
		session.createSQLQuery("select * from student").addEntity(Student.class).list();
		//参数查询
		Query q1 = session.createSQLQuery("select sname, age from student where age = ?").addEntity(Student.class);
		List list1 = q1.setInteger(0, 11).list();
		System.out.println(list1.size());
		Query q2 = session.createSQLQuery("select sname, age from student where naem like :name").addEntity(Student.class);
		List list2 = q2.setString("sname", "a%").list();
		System.out.println(list2.size());
	}
	
}
