package com.why.project.photopickerdemo.utils;

import android.text.TextUtils;

import java.io.File;

/**
 * Created by HaiyuKing
 * Used
 */

public class Globals {

	/**PhotoPicker*/
	public static final int CHOOSE_PIC_REQUEST_CODE = 5;

	//获取文件名称
	public static String getFileName(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return filePath;
		}

		int filePosi = filePath.lastIndexOf(File.separator);
		return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
	}
}
