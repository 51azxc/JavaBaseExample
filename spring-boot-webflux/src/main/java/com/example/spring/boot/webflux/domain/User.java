package com.example.spring.boot.webflux.domain;

public class User {
	private Integer id;
	private String username;
	private String version;
	
	public User() {	}
	
	public User(Integer id, String username) {
		this.id = id;
		this.username = username;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}
