package me.iwf.photopicker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.entity.Photo;
import me.iwf.photopicker.event.OnItemCheckListener;
import me.iwf.photopicker.fragment.ImagePagerFragment;
import me.iwf.photopicker.fragment.PhotoPickerFragment;

import static android.widget.Toast.LENGTH_LONG;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static me.iwf.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static me.iwf.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;

/**
 * 图片选择器主页面
 * 解决Toolbar中的困惑，随心所欲定制Toolbar：https://blog.csdn.net/jungle_pig/article/details/52785781
 */
public class PhotoPickerActivity extends AppCompatActivity {

	private PhotoPickerFragment pickerFragment;//图片列表碎片界面
	private ImagePagerFragment imagePagerFragment;//图片预览碎片界面

//	private MenuItem menuDoneItem;//顶部导航栏右侧menu【方案一】

	/**返回图标*/
	private ImageView mBackImg;
	/**标题*/
	private TextView mTitleTv;
	/**完成*/
	private TextView mConfirmTv;

	private int maxCount = DEFAULT_MAX_COUNT;//多选的最大值

	/**
	 * to prevent multiple calls to inflate menu
	 * 防止多次创建菜单
	 */
	private boolean menuIsInflated = false;

	private boolean showGif = false;//是否显示gif
	private int columnNumber = DEFAULT_COLUMN_NUMBER;//一行显示多少列
	private ArrayList<String> originalPhotos = null;//选中的图片路径集合


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//保持屏幕为横屏或者竖屏，禁止旋转
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏

		//将Acitivity 中的Window 的背景图设置为空,解决Android Activity切换时出现白屏问题
		getWindow().setBackgroundDrawable(null);

		boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
		boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, false);
		boolean previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);

		setShowGif(showGif);

		setContentView(R.layout.__picker_activity_photo_picker);//布局文件

		//initToolbar();//初始化Toolbar【方案一】

		maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
		columnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
		originalPhotos = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS);

		initToolbarCustom();//初始化自定义的Toolbar

		pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag("tag");
		if (pickerFragment == null) {
			pickerFragment = PhotoPickerFragment
					.newInstance(showCamera, showGif, previewEnabled, columnNumber, maxCount, originalPhotos);
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.container, pickerFragment, "tag")
					.commit();
			getSupportFragmentManager().executePendingTransactions();
		}

		initToolBarEvent();//初始化导航栏事件监听

		//【方案一】
		/*pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
			@Override
			public boolean onItemCheck(int position, Photo photo, final int selectedItemCount) {

				menuDoneItem.setEnabled(selectedItemCount > 0);//如果选中的数目小于0，则顶部导航栏右侧的完成文本，不可点击

				//如果单选，那么选中的图片路径集合肯定只有一个
				if (maxCount <= 1) {
					List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
					if (!photos.contains(photo.getPath())) {//如果点击的当前选中的图片，则取消选中状态
						photos.clear();//清空选中的图片集合
						pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
					}
					return true;
				}

				if (selectedItemCount > maxCount) {
					Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, maxCount),
							LENGTH_LONG).show();
					return false;
				}
				menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, selectedItemCount, maxCount));//更改导航栏右侧的 完成(1/9)
				return true;
			}
		});*/

	}

	private void initToolbarCustom() {

		mBackImg = (ImageView)findViewById(R.id.backImg);
		mTitleTv = (TextView)findViewById(R.id.titleText);
		mTitleTv.setText("插入图片");
		mConfirmTv = findViewById(R.id.confirmText);
		mConfirmTv.setVisibility(View.VISIBLE);

		if (originalPhotos != null && originalPhotos.size() > 0) {
			mConfirmTv.setClickable(true);
			mConfirmTv.setTextColor(getResources().getColor(R.color.__picker_nav_done_text_color));
			mConfirmTv.setText(
					getString(R.string.__picker_done_with_count, originalPhotos.size(), maxCount));
		} else {
			mConfirmTv.setClickable(false);
			mConfirmTv.setTextColor(getResources().getColor(R.color.__picker_nav_done_ennable_text_color));
		}
	}

	private void initToolBarEvent() {
		pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
			@Override
			public boolean onItemCheck(int position, Photo photo, final int selectedItemCount) {

				mConfirmTv.setClickable(selectedItemCount > 0);//如果选中的数目小于0，则顶部导航栏右侧的完成文本，不可点击
				if(selectedItemCount > 0){
					mConfirmTv.setTextColor(getResources().getColor(R.color.__picker_nav_done_text_color));
				}else{
					mConfirmTv.setTextColor(getResources().getColor(R.color.__picker_nav_done_ennable_text_color));
				}


				//如果单选，那么选中的图片路径集合肯定只有一个
				if (maxCount <= 1) {
					List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
					if (!photos.contains(photo.getPath())) {//如果点击的当前选中的图片，则取消选中状态
						photos.clear();//清空选中的图片集合
						pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
					}
					return true;
				}

				if (selectedItemCount > maxCount) {
					Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, maxCount),
							LENGTH_LONG).show();
					return false;
				}
				mConfirmTv.setText(getString(R.string.__picker_done_with_count, selectedItemCount, maxCount));//更改导航栏右侧的 完成(1/9)
				return true;
			}
		});


		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		mConfirmTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
				intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		mConfirmTv.setClickable(false);//需要放到这里执行，否则不生效
		mConfirmTv.setTextColor(getResources().getColor(R.color.__picker_nav_done_ennable_text_color));
	}

	//初始化Toolbar【方案一】
	/*private void initToolbar(){
		//像获取普通控件那样获取Toolbar
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitle("");//设置原有的标题内容为空
		((TextView) findViewById(R.id.toolbarTitle)).setText(R.string.__picker_title);

		setSupportActionBar(mToolbar);//Toolbar也可以如此使用，转化成ActionBar,这样一来，选项菜单就得按照之前ActionBar的方式设置

		ActionBar actionBar = getSupportActionBar();

		assert actionBar != null;
		actionBar.setDisplayHomeAsUpEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			actionBar.setElevation(20);//设置阴影
		}
	}*/


	/**
	 * Overriding this method allows us to run our exit animation first, then exiting
	 * the activity when it complete.
	 */
	@Override
	public void onBackPressed() {
		//如果图片预览碎片界面正在显示，则按返回键关闭图片预览碎片界面
		if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
			imagePagerFragment.runExitAnimation(new Runnable() {
				public void run() {
					if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
						getSupportFragmentManager().popBackStack();
					}
				}
			});
		} else {
			super.onBackPressed();
		}
	}

	/**替换成图片预览碎片界面*/
	public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
		this.imagePagerFragment = imagePagerFragment;
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container, this.imagePagerFragment)
				.addToBackStack(null)
				.commit();
	}

	//【方案一】
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!menuIsInflated) {
			getMenuInflater().inflate(R.menu.__picker_menu_picker, menu);
			menuDoneItem = menu.findItem(R.id.done);
			if (originalPhotos != null && originalPhotos.size() > 0) {
				menuDoneItem.setEnabled(true);
				menuDoneItem.setTitle(
						getString(R.string.__picker_done_with_count, originalPhotos.size(), maxCount));
			} else {
				menuDoneItem.setEnabled(false);
			}
			menuIsInflated = true;
			return true;
		}
		return false;
	}*/

	//【方案一】
	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			super.onBackPressed();
			return true;
		}

		if (item.getItemId() == R.id.done) {
			Intent intent = new Intent();
			ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
			intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
			setResult(RESULT_OK, intent);
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}*/

	public PhotoPickerActivity getActivity() {
		return this;
	}

	public boolean isShowGif() {
		return showGif;
	}

	public void setShowGif(boolean showGif) {
		this.showGif = showGif;
	}
}
