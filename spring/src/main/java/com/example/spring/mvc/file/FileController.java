package com.example.spring.mvc.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/*
 * Spring MVC 文件上传与下载 
 */

@Controller
public class FileController {

    @Autowired
	private Environment env;

    // 文件上传
    @RequestMapping(value="/upload", method=RequestMethod.POST)
	public void putFile(@RequestParam("file")MultipartFile multipartFile) {
        try {
            String filePath = Paths.get(env.getRequiredProperty("tmp.dir"), 
            		multipartFile.getOriginalFilename())
            	.toString();
            File file = new File(filePath);
            multipartFile.transferTo(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //文件下载
    @RequestMapping(value="/files/{fileId}/contents", method=RequestMethod.GET)
    public void getFile(@PathVariable String fileId,  HttpServletResponse response) {
        String filePath = "C:/"+fileId+".txt";
        Path path = Paths.get(filePath);
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            try {
                File file = path.toFile();
                response.setContentType("application/octet-stream");
                response.setContentLength((int)file.length());
                response.setHeader("Content-Disposition", "attachment;filename=\"" + file.getName() + "\"");
                InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                FileCopyUtils.copy(inputStream, response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
