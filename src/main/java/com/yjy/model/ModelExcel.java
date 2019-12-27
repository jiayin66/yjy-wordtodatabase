package com.yjy.model;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelExcel {
	public ModelExcel(int i,String columnName, String columnType) {
		this.columnName=columnName;
		this.columnType=columnType;
		this.id=i+"";
	}
	@Excel(name = "ÐòºÅ", orderNum = "0",width = 15)
	private String id;
	@Excel(name = "×Ö¶Î½âÊÍ", orderNum = "0",width = 15)
	private String name;
	@Excel(name = "×Ö¶ÎÓ¢ÎÄÃû", orderNum = "1",width = 15)
	private String columnName;
	@Excel(name = "×Ö¶Î³¤¶È", orderNum = "2",width = 15)
	private String columnType;
	@Excel(name = "±¸×¢", orderNum = "3",width = 15)
	private String remark;

	public ModelExcel() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
