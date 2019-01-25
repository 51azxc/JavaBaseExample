package com.example.spring.mvc.interceptor.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

@Controller
public class MyController {
	
	@Autowired
	private Gson gson;
	
	@RequestMapping(value="/test",method=RequestMethod.GET,produces="text/html;charset=UTF-8")
	public String test(HttpServletRequest request,HttpServletResponse response){
		Map<String,String> map = new HashMap<String,String>();
		map.put("say", "hello");
		return gson.toJson(map);
	}
}
