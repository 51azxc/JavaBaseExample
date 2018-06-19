package com.example.servlet.test;

import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.example.service.AlphabetService;

public class TestService {
	private AlphabetService alphabetService;

	@Before
	public void setUp() throws Exception {
		alphabetService = new AlphabetService();
	}

	@Test
	public void testGetList() throws Exception {
		Assert.assertTrue(alphabetService.getAlphabetList().size() == 26);
		Assert.assertThat(alphabetService.getAlphabetList(), CoreMatchers.hasItems("A"));
	}

	@Test
	public void testGetMap() throws Exception {
		Map<String, String> map = alphabetService.getAlphabetMap();
		for(String s : map.keySet()){
			System.out.println(s+"->"+map.get(s));
		}
	}
}
