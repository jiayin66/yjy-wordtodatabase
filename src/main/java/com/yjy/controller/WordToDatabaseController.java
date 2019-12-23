package com.yjy.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yjy.service.WordToDatabaseService;
@RestController
@RequestMapping("word")
public class WordToDatabaseController {
	@Autowired
	private WordToDatabaseService wordToDatabaseService;
	
	@PostMapping("/text")
	public String readWordToDatabase(@RequestParam("text") MultipartFile text,HttpServletResponse response,@RequestParam("tableName") String tableName) {
		return wordToDatabaseService.readWordToDatabase(text,response,tableName);
	}
}
