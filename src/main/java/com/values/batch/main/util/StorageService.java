package com.values.batch.main.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class StorageService {
	@Value("${spring.servlet.multipart.location}")
    private String uploadPath;
	
	/**
	 * 파일 업로드
	 * @param file
	 */
	public Map<String, Object> store(MultipartFile file) {
		Map<String, Object> result = new HashMap<String, Object>();
		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSS");
		Date time = new Date();
		String forTime = format.format(time);
		
		int pos = filename.lastIndexOf(".");
		String ext = filename.substring(pos + 1);
		
		try {
			InputStream inputStream = file.getInputStream();
			Files.copy(inputStream, getPath().resolve(forTime + "." + ext),
					StandardCopyOption.REPLACE_EXISTING);
			result.put("fileName", forTime + "." + ext);
			result.put("orgFileName", filename);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * 파일 다운로드 
	 * @param filename
	 * @return
	 */
	public Resource loadAsResource(String filename) {
		try {
			Path file = getPath().resolve(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 파일 삭제
	 * @param filename
	 * @return
	 */
	public boolean deleteAsResource(String filename) {
		boolean result = true;
		try {
			Path file = getPath().resolve(filename);
			
			File deleteFile = new File(file.toUri());
			if (deleteFile.exists()) {
				result = deleteFile.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * 패스 객체 반환
	 * @return
	 */
	private Path getPath() {
		return Paths.get(uploadPath);
	}
}
