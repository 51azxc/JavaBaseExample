package com.example.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="student")
public class Student {

	private int sid;
	private String sname;
	private int age;
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public int getSid() {
		return sid;
	}
	@Column(name="sname",length=50)
	public String getSname() {
		return sname;
	}
	public int getAge() {
		return age;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	public void setSname(String sname) {
		this.sname = sname;
	}
	public void setAge(int age) {
		this.age = age;
	}
}
