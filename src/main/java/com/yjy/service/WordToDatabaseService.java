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
		//1.���н������õ�ÿ�м���
		List<String> txtRecordList=new ArrayList<String>();	
		try {
				InputStreamReader is = new InputStreamReader(text.getInputStream());
				BufferedReader bf = new BufferedReader(is);
				String readLine = bf.readLine();
				 while (readLine != null) {  
					 if(!StringUtils.isEmpty(readLine)) {
						 txtRecordList.add(readLine);
					 }
					 readLine = bf.readLine(); // һ�ζ���һ������  
		            }  
			} catch (Exception e) {
				System.out.println("����ԭ��"+e.getMessage());
				e.printStackTrace();
				throw new RuntimeException("��ȡtxt����ʧ��");
			}
		//2ѭ������ƴ��sql
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
			//����Ǵ�ӡЧ�������Ƿ�����ȷ
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
				//�����������ݾ�׷��û���ݲ�׷��
			}finally {
				sb.append("\';\n");
			}
		}
		return sb.toString();
	}

	public String readDatabaseToExcel(String text, HttpServletResponse response) {
		List<ModelExcel> result=new ArrayList<ModelExcel>();
		//���ݷֺŲ��
		String[] split = text.split(";");
		//����ע
		Map<String,String> columnRemark=new HashMap<String,String>();
		for(int i=0;i<split.length;i++) {
			if(i==0) {
				continue;
			}
			String comment=split[i];
			//���˵�����Ǹ�
			if(comment.indexOf(".")==-1) {
				continue;
			}
			String columnName = comment.substring(comment.indexOf(".")+1, comment.indexOf("is")).trim();
			String remark = comment.substring(comment.indexOf("is")+2);
			remark=remark.replaceAll("\'", "");
			columnRemark.put(columnName.trim(), remark);
		}
		//�������
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
		excelTemplateExporter.exportExcel(result, "���ݿ�ģ��", "���ݿ�ģ��", ModelExcel.class, "���ݿ�ģ��.xls", response);
		return "true";
	}

}
