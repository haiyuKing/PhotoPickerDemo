package com.why.project.photopickerdemo.bean;

/**
 * Created by HaiyuKing
 * Used 照片Bean
 */

public class PictureBean {
	//图片本地路径
	private String picPath = "";
	//图片本地名称
	private String picName = "";

	public String getPicPath() {
		return picPath;
	}

	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}

	public String getPicName() {
		return picName;
	}

	public void setPicName(String picName) {
		this.picName = picName;
	}
}
