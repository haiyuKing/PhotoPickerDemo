package com.why.project.photopickerdemo;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.why.project.photopickerdemo.bean.PictureBean;
import com.why.project.photopickerdemo.utils.Globals;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.fragment.NewPhotoPickerFragment;

/**
 * Created by HaiyuKing
 * Used 选择图片对话框
 */

public class SelectPictureDialog extends DialogFragment {
	private static final String TAG = SelectPictureDialog.class.getSimpleName();

	/**View实例*/
	private View myView;
	/**context实例*/
	private Context mContext;
	/**标记：用来代表是从哪个界面打开的这个对话框*/
	private String mTag;

	/**完成文本*/
	private TextView mConfirmTv;

	public static final String EXTRA_MAX_COUNT = "maxCount";
	public static final int DEFAULT_MAX_COUNT = 1;//默认只能选择一个图片

	private boolean previewEnabled = true;//是否可预览
	private boolean showGif = false;//是否显示gif
	private int columnNumber = 3;//一行显示多少列
	private int maxCount = 1;//最大可选择的数目
	private Boolean fullscreen = false;//是否全屏展现预览界面的标志

	private FragmentManager fragmentManager;//碎片管理器
	private NewPhotoPickerFragment mLocalPicFragment;

	public static SelectPictureDialog getInstance(Context mContext, Bundle bundle)
	{
		SelectPictureDialog selectPictureDialog = new SelectPictureDialog();
		selectPictureDialog.mContext = mContext;
		selectPictureDialog.maxCount = bundle.getInt(EXTRA_MAX_COUNT,DEFAULT_MAX_COUNT);

		return selectPictureDialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.w(TAG,"{onCreate}");

		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_NoTitleBar_Fullscreen);//全屏（在状态栏底下）
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.w(TAG,"{onCreateView}");

		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));//设置背景为透明，并且没有标题
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		//设置窗体全屏
		getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		myView = inflater.inflate(R.layout.dialog_select_picture, container, false);
		return myView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.w(TAG,"{onActivityCreated}");

		//初始化控件以及设置
		initView();
		//初始化数据
		initDatas();
		//初始化事件
		initEvent();

		//初始化碎片管理器
		fragmentManager = getChildFragmentManager();//必须使用getChildFragmentManager,否则子Fragment中无法使用getParentFragment获取父fragment

	}

	/**
	 * 设置宽度和高度值，以及打开的动画效果
	 */
	@Override
	public void onStart() {
		super.onStart();
		if(mTag.equals("previewWithScreen")){//对话框全屏显示，预览图片也是全屏显示
			fullscreen = true;
			//设置对话框的宽高，必须在onStart中
			Window window = this.getDialog().getWindow();
			window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);//全屏（盖住状态栏）
			window.setGravity(Gravity.BOTTOM);//设置在底部
			//打开的动画效果--缩放+渐隐
		}else{
			fullscreen = false;
			//设置对话框的宽高，必须在onStart中
			DisplayMetrics metrics = new DisplayMetrics();
			this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
			Window window = this.getDialog().getWindow();
			window.setLayout(metrics.widthPixels, metrics.heightPixels - getStatusBarHeight(mContext));
			window.setGravity(Gravity.BOTTOM);//设置在底部
			//打开的动画效果--缩放+渐隐
		}
	}

	/**获取状态栏的高度*/
	private int getStatusBarHeight(Context context) {
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		return context.getResources().getDimensionPixelSize(resourceId);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		ShowFragment();
	}

	private void initView() {
		mConfirmTv = (TextView) myView.findViewById(R.id.nav_selpic_edit_save);
	}

	private void initDatas() {
		mTag = this.getTag();
	}

	private void initEvent() {
		//完成图标
		mConfirmTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//返回界面，并传递值回去
				ArrayList<PictureBean> selectedPhotoList = new ArrayList<PictureBean>();

				if(maxCount <= 1){
					ArrayList<String> selectedPhotos = new ArrayList<String>();
					if(mLocalPicFragment != null) {
						selectedPhotos = mLocalPicFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
					}
					String imgPath = selectedPhotos.get(0);
					PictureBean pictureBean = new PictureBean();
					pictureBean.setPicPath(imgPath);
					pictureBean.setPicName(Globals.getFileName(imgPath));
					selectedPhotoList.add(pictureBean);
				}else{
					ArrayList<String> selectedPhotos = new ArrayList<String>();
					if(mLocalPicFragment != null) {
						selectedPhotos = mLocalPicFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
					}
					for(String path : selectedPhotos){
						PictureBean pictureBean = new PictureBean();
						pictureBean.setPicPath(path);
						pictureBean.setPicName(Globals.getFileName(path));
						selectedPhotoList.add(pictureBean);
					}
				}

				backWithResult(selectedPhotoList);
			}
		});
		//刚开始设置不可点击【必须在setOnclick事件之后，否则不起作用】
		changeConfirmState(false);
	}

	private void ShowFragment() {
		//开启一个事务
		FragmentTransaction transcation = fragmentManager.beginTransaction();
		if(mLocalPicFragment == null){
			mLocalPicFragment = NewPhotoPickerFragment.getInstance(true, showGif, previewEnabled, columnNumber, maxCount, null,fullscreen);
			initLocalFragmentAdapterItemCheckListener();//初始化本地图片碎片界面的列表项的复选框图标点击事件
			transcation.add(R.id.center_layout, mLocalPicFragment);
		}else{
			transcation.show(mLocalPicFragment);
		}
		transcation.commitAllowingStateLoss();
	}

	//本地图片的选择监听
	private void initLocalFragmentAdapterItemCheckListener() {
		//本地图片的选择的监听事件
		mLocalPicFragment.setOnOnLocalPhotoCheckListener(new NewPhotoPickerFragment.OnLocalPhotoCheckListener() {
			@Override
			public boolean onItemCheck(int position, String imgId, String imgName, String imgPath, int selectedItemCount) {
				//如果选中的数目小于0，则顶部导航栏右侧的完成文本，不可点击
				if(selectedItemCount > 0){
					changeConfirmState(true);
				}else{
					changeConfirmState(false);
				}

				//如果单选，那么选中的图片路径集合肯定只有一个
				if (maxCount <= 1) {
					List<String> photos = mLocalPicFragment.getPhotoGridAdapter().getSelectedPhotos();
					if (!photos.contains(imgPath)) {//如果点击的当前选中的图片，则取消选中状态
						photos.clear();//清空选中的图片集合
						mLocalPicFragment.getPhotoGridAdapter().notifyDataSetChanged();
					}
					return true;
				}

				if (selectedItemCount > maxCount) {
					Toast.makeText(mContext,getString(R.string.over_max_count_tips, maxCount),Toast.LENGTH_SHORT).show();
					return false;
				}
				mConfirmTv.setText(getString(R.string.done_with_count, selectedItemCount, maxCount));//更改导航栏右侧的 完成(1/9)
				return true;
			}
		});
	}

	//更换完成文本的点击状态
	private void changeConfirmState(boolean canClick){
		if(canClick){
			mConfirmTv.setClickable(true);
			mConfirmTv.setTextColor(getResources().getColor(R.color.nav_done_text_color));
		}else{
			mConfirmTv.setClickable(false);
			mConfirmTv.setTextColor(getResources().getColor(R.color.nav_done_ennable_text_color));
		}
	}


	private void backWithResult(ArrayList<PictureBean> photosList) {
		dismiss();
		if(mOnSelPicDialogConfirmClickListener != null){
			mOnSelPicDialogConfirmClickListener.onConfirmClick(photosList);
		}
	}

	/*=====================添加OnConfirmClickListener回调================================*/
	private OnSelPicDialogConfirmClickListener mOnSelPicDialogConfirmClickListener;

	public void setOnSelPicDialogConfirmClickListener(OnSelPicDialogConfirmClickListener mOnSelPicDialogConfirmClickListener)
	{
		this.mOnSelPicDialogConfirmClickListener = mOnSelPicDialogConfirmClickListener;
	}

	public interface OnSelPicDialogConfirmClickListener {
		void onConfirmClick(ArrayList<PictureBean> selectedPhotoList);
	}
}
