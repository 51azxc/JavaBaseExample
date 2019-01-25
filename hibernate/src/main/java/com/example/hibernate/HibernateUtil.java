package com.example.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {
	private static SessionFactory sessionFactory = buildSessionFactory();

	private static SessionFactory buildSessionFactory() {
		try {
			// 读取Hibernate的配置文件 hibernate.cfg.xml文件
			Configuration cfg = new Configuration().configure();
			// 添加实体类
			cfg.addAnnotatedClass(Student.class);
			ServiceRegistry sr = new StandardServiceRegistryBuilder()
					.applySettings(cfg.getProperties())
					.build();
			return cfg.buildSessionFactory(sr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}

/*
 * hibernate.cfg.xml配置文件，位于src目录下
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC "-//Hibernate/Hibernate Configuration DTD 3.0//EN" 
 "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd"> 
<hibernate-configuration>
	<session-factory>
	    <property name="hibernate.connection.provider_class">org.hibernate.c3p0.internal.C3P0ConnectionProvider</property>
		<property name="hibernate.c3p0.min_size">2</property>
		<property name="hibernate.c3p0.max_size">20</property>
		<property name="hibernate.c3p0.timeout">120</property>
		<property name="hibernate.c3p0.max_statements">10</property>
		<property name="hibernate.c3p0.idle_test_period">300</property>
		<property name="hibernate.c3p0.acquire_increment">2</property>
        
        <property name="connection.url">jdbc:mysql://localhost:3306/test?useUnicode=true&amp;
        	characterEncoding=UTF-8</property>
        <property name="connection.username">root</property>
        <property name="connection.password">root</property>
        <property name="connection.driver_class">com.mysql.jdbc.Driver</property>
	    
        <property name="show_sql">true</property>  
        <property name="format_sql">true</property>
        <property name="dialect">org.hibernate.dialect.MySQL5Dialect</property>  
        <property name="hibernate.current_session_context_class">thread</property>  
        <property name="hbm2ddl.auto">update</property>  
	</session-factory>    
</hibernate-configuration>
*/
