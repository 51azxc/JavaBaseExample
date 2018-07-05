package com.example.dl4j.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class DataUtilities {

	//如果没有数据就下载一个数据包到本地文件
	public static boolean downloadFile(String remoteUrl, String localPath) throws IOException {
		boolean downloaded = false;
		if (remoteUrl == null || localPath == null) {
			return downloaded;
		}
		Path file = Paths.get(localPath);
		if (Files.notExists(file, LinkOption.NOFOLLOW_LINKS)) {
			Files.createDirectory(file.getParent());
			
			CloseableHttpClient client = HttpClients.createDefault();
			try (CloseableHttpResponse resp = client.execute(new HttpGet(remoteUrl))) {
				HttpEntity entity = resp.getEntity();
				if (entity != null) {
					try (OutputStream output = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW)) {
						entity.writeTo(output);
						output.flush();
					}
				}
			}
			downloaded = true;
		}
		if (Files.notExists(file, LinkOption.NOFOLLOW_LINKS)) {
			 throw new IOException("File doesn't exist: " + localPath);
		}
		return downloaded;
	}
	
	//解压"tar.gz"文件到指定文件夹
	public static void extractTarGz(String inputPath, String outputPath) throws IOException {
	    if (inputPath == null || outputPath == null)
	      return;
	    final int bufferSize = 4096;
	    if (!outputPath.endsWith("" + File.separatorChar))
	      outputPath = outputPath + File.separatorChar;
	    try (TarArchiveInputStream tais = new TarArchiveInputStream(
	        new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(inputPath))))) {
	      TarArchiveEntry entry;
	      while ((entry = (TarArchiveEntry) tais.getNextEntry()) != null) {
	        if (entry.isDirectory()) {
	          new File(outputPath + entry.getName()).mkdirs();
	        } else {
	          int count;
	          byte data[] = new byte[bufferSize];
	          FileOutputStream fos = new FileOutputStream(outputPath + entry.getName());
	          BufferedOutputStream dest = new BufferedOutputStream(fos, bufferSize);
	          while ((count = tais.read(data, 0, bufferSize)) != -1) {
	            dest.write(data, 0, count);
	          }
	          dest.close();
	        }
	      }
	    }
	  }
	
}
