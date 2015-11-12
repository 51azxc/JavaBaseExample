package com.test.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlphabetService {
	public List<String> getAlphabetList(){
		List<String> alphabet = new ArrayList<String>();
		for(int i=(int)'A';i<'A'+26;i++){
			alphabet.add(String.valueOf((char)i));
		}
		return alphabet;
	}
	
	public Map<String, String> getAlphabetMap(){
		Map<String, String> map = new HashMap<String, String>();
		for(int i=(int)'A';i<'A'+26;i++){
			map.put(String.valueOf(i), String.valueOf((char)i));
		}
		return map;
	}
}
