package com.why.project.photopickerdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.why.project.photopickerdemo.adapter.PictureAdapter;
import com.why.project.photopickerdemo.bean.PictureBean;
import com.why.project.photopickerdemo.utils.Globals;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.fragment.NewImagePagerDialogFragment;
import me.iwf.photopicker.utils.PermissionsUtils;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private RecyclerView mRecyclerView;
	private ArrayList<PictureBean> mPictureBeansList;
	private PictureAdapter mPictureAdapter;

	private ArrayList<String> selPhotosPath = null;//选中的图片路径集合

	private Button mOpenPicDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();
		initDatas();
		initEvents();
	}

	private void initViews() {
		mRecyclerView = findViewById(R.id.picture_grid);
		mOpenPicDialog = findViewById(R.id.btn_openPicDialog);
	}

	private void initDatas() {
		selPhotosPath = new ArrayList<String>();
		//=============图片九宫格=========================
		mPictureAdapter = null;
		mPictureBeansList = new ArrayList<PictureBean>();
		//设置布局管理器
		GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
		mRecyclerView.setLayoutManager(gridLayoutManager);

		if(mPictureAdapter == null){
			//设置适配器
			mPictureAdapter = new PictureAdapter(this, mPictureBeansList);
			mRecyclerView.setAdapter(mPictureAdapter);
			//添加分割线
			//设置添加删除动画
			//调用ListView的setSelected(!ListView.isSelected())方法，这样就能及时刷新布局
			mRecyclerView.setSelected(true);
		}else{
			mPictureAdapter.notifyDataSetChanged();
		}
	}

	private void initEvents() {
		//图片九宫格点击事件
		mPictureAdapter.setOnItemClickLitener(new PictureAdapter.OnItemClickLitener() {
			@Override
			public void onItemClick(View v,int position) {
				//打开自定义的图片预览对话框
				List<String> photos = mPictureAdapter.getAllPhotoPaths();

				int[] screenLocation = new int[2];
				v.getLocationOnScreen(screenLocation);

				NewImagePagerDialogFragment newImagePagerDialogFragment = NewImagePagerDialogFragment.getInstance(MainActivity.this,photos,position,screenLocation, v.getWidth(),
						v.getHeight(),false);
				newImagePagerDialogFragment.show(getSupportFragmentManager(),"preview img");
			}

			@Override
			public void onItemAddClick() {
				PhotoPicker.builder()
						.setPhotoCount(mPictureAdapter.MAX)
						.setGridColumnCount(3)
						//.setSelected(selPhotosPath)
						.start(MainActivity.this, Globals.CHOOSE_PIC_REQUEST_CODE);
			}
		});

		//按钮的点击事件
		mOpenPicDialog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (PermissionsUtils.checkReadStoragePermission(MainActivity.this)) {//调用PhotoPicker中的运行时权限申请工具类，在这里也可以使用其他运行时权限申请库
					Bundle bundle = new Bundle();
					bundle.putInt(SelectPictureDialog.EXTRA_MAX_COUNT, 1);
					SelectPictureDialog selectPictureDialog = SelectPictureDialog.getInstance(MainActivity.this, bundle);
					selectPictureDialog.setOnSelPicDialogConfirmClickListener(new SelectPictureDialog.OnSelPicDialogConfirmClickListener() {
						@Override
						public void onConfirmClick(ArrayList<PictureBean> selectedPhotoList) {
							String path = "";
							if (selectedPhotoList.size() > 0) {
								path = selectedPhotoList.get(0).getPicPath();
							}
							Toast.makeText(MainActivity.this, "path=" + path, Toast.LENGTH_SHORT).show();
						}
					});
					selectPictureDialog.show(getSupportFragmentManager(), "previewWithScreen");
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.w(TAG, "{onActivityResult}resultCode="+resultCode);
		Log.w(TAG, "{onActivityResult}requestCode="+requestCode);
		if (resultCode == Activity.RESULT_OK) {
			//选择照片
			if(requestCode == Globals.CHOOSE_PIC_REQUEST_CODE){

				if (data != null) {
					selPhotosPath = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
				}
				if (selPhotosPath != null) {

					//下面的代码主要用于这样一个场景，就是注释了.setSelected(selPhotosPath)之后，还想要保证选择的图片不重复
					/*for(String path : selPhotosPath){
						Log.w(TAG,"path="+path);///storage/emulated/0/tempHxzk/IMG_1498034535796.jpg
						boolean existThisPic = false;
						for(int i=0;i<mPictureBeansList.size();i++){
							if(path.equals(mPictureBeansList.get(i).getPicPath())){
								//如果新选择的图片集合中存在之前选中的图片，那么跳过去
								existThisPic = true;
								break;
							}
						}
						if(! existThisPic){
							PictureBean pictureBean = new PictureBean();
							pictureBean.setPicPath(path);
							pictureBean.setPicName(getFileName(path));
							//去掉总数目的限制，这里通过增大MAX的数字来实现
							if (mPictureBeansList.size() < mPictureAdapter.MAX) {
								mPictureBeansList.add(pictureBean);
							} else {
								Toast.makeText(MainActivity.this, "最多可以选择" + mPictureAdapter.MAX + "张图片", Toast.LENGTH_SHORT).show();
								break;
							}
						}
					}*/

					//是常规操作，和上面的代码不可共存
					for (String path : selPhotosPath) {
						PictureBean pictureBean = new PictureBean();
						pictureBean.setPicPath(path);
						pictureBean.setPicName(Globals.getFileName(path));
						//去掉总数目的限制，这里通过增大MAX的数字来实现
						if (mPictureBeansList.size() < mPictureAdapter.MAX) {
							mPictureBeansList.add(pictureBean);
						} else {
							Toast.makeText(MainActivity.this, "最多可以选择" + mPictureAdapter.MAX + "张图片", Toast.LENGTH_SHORT).show();
							break;
						}
					}
					mPictureAdapter.notifyDataSetChanged();
				}
			}
		}
	}

}
