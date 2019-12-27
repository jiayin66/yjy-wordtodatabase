package com.yjy.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjy.model.ModelExcel;
import com.yjy.util.ExcelTemplateExporter;

import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class WordToDatabaseService {
	@Autowired
	private ExcelTemplateExporter excelTemplateExporter;
	public String readWordToDatabase(MultipartFile text, HttpServletResponse response,String tableName) {
		//1.按行解析，拿到每行集合
		List<String> txtRecordList=new ArrayList<String>();	
		try {
				InputStreamReader is = new InputStreamReader(text.getInputStream());
				BufferedReader bf = new BufferedReader(is);
				String readLine = bf.readLine();
				 while (readLine != null) {  
					 if(!StringUtils.isEmpty(readLine)) {
						 txtRecordList.add(readLine);
					 }
					 readLine = bf.readLine(); // 一次读入一行数据  
		            }  
			} catch (Exception e) {
				System.out.println("错误原因："+e.getMessage());
				e.printStackTrace();
				throw new RuntimeException("读取txt数据失败");
			}
		//2循环数据拼接sql
		StringBuffer sb=new StringBuffer();
		sb.append("create table \""+tableName+"\"(");
		
		int index=1;
		/**
		 * create table "Table_1" 
			(
			   ID                   VHARCHAR2(64)        not null,
			   NAME                 VHARCHAR2(64),
			   constraint PK_TABLE_1 primary key (ID)
			);
		 */
		for(String str:txtRecordList) {
			String[] split = str.split("\\s");
			//这个是打印效果，看是否拆分正确
			/*for(int i=0;i<split.length;i++) {
				System.out.println(split[i]);
			}*/
			if(index==1) {
				if(!"ID".equals(split[1])) {
					sb.append("ID VARCHAR2(64) not null,");
				}
			}
			sb.append(split[1]+" "+split[2]+",");
			
			if(index==txtRecordList.size()) {
				sb.append(" constraint PK_"+tableName+" primary key (ID)");
			}
			index ++;
		}
		sb.append(");\n");
		for(String str:txtRecordList) {
			String[] split = str.split("[\\s ]+");

			sb.append("comment on column \""+tableName+"\"."+split[1]+" is \'"+split[0]);
			try {
				sb.append(split[3]);
			} catch (Exception e) {
				//第四列有数据就追加没数据不追加
			}finally {
				sb.append("\';\n");
			}
		}
		return sb.toString();
	}

	public String readDatabaseToExcel(String text, HttpServletResponse response) {
		List<ModelExcel> result=new ArrayList<ModelExcel>();
		//根据分号拆分
		String[] split = text.split(";");
		//处理备注
		Map<String,String> columnRemark=new HashMap<String,String>();
		for(int i=0;i<split.length;i++) {
			if(i==0) {
				continue;
			}
			String comment=split[i];
			//过滤掉表的那个
			if(comment.indexOf(".")==-1) {
				continue;
			}
			String columnName = comment.substring(comment.indexOf(".")+1, comment.indexOf("is")).trim();
			String remark = comment.substring(comment.indexOf("is")+2);
			remark=remark.replaceAll("\'", "");
			columnRemark.put(columnName.trim(), remark);
		}
		//处理对象
		String table = split[0];
		String[] splitInner = table.split(",(\n|\r\n)");
		int i=1;
		for(String str:splitInner) {
			str=str.replaceAll(" BYTE", "");
			String[] inerStr = str.trim().split("[ ]+");
			String columName=inerStr[0].trim();
			ModelExcel modelExcel = new ModelExcel(i,columName,inerStr[1]);
			if(columnRemark.containsKey(columName)) {
				modelExcel.setName(columnRemark.get(columName));
			}
			result.add(modelExcel);
			i++;
		}
		System.out.println(JSON.toJSONString(result));
		excelTemplateExporter.exportExcel(result, "数据库模型", "数据库模型", ModelExcel.class, "数据库模型.xls", response);
		return "true";
	}

}
