package com.example.spring.mvc.file;

import java.io.FileInputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/*
 * 测试MVC文件上传下载功能
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { FileConfig.class })
public class FileControllerTest {
    @Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;

    @Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }
    
    // 测试文件上传
    @Test
	public void testPutFile() throws Exception {
		String uploadPath = "C:/test1.xls";
		MockMultipartFile file = new MockMultipartFile("file", new FileInputStream(uploadPath));
		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/upload").file(file))
			.andExpect(MockMvcResultMatchers.status().isOk());
	}
    
    // 测试文件下载
    @Test
	public void testGetFile() throws Exception {
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/files/{fileId}", "1"))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn();
		String content = mvcResult.getResponse().getContentAsString();
		System.out.println(content);
	}
}
