package me.iwf.photopicker.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.iwf.photopicker.R;
import me.iwf.photopicker.adapter.PhotoPagerAdapter;

/**
 * Used 图片预览对话框界面，参考ImagePagerFragment【貌似支持网络图片，那么就这样使用吧】
 */

public class NewImagePagerDialogFragment extends DialogFragment {
	private static final String TAG = NewImagePagerDialogFragment.class.getSimpleName();
	/**View实例*/
	private View myView;
	/**context实例*/
	private Context mContext;
	/**标记：用来代表是从哪个界面打开的这个对话框*/
	private String mTag;

	/**返回图标*/
	private ImageView mBackImg;
	/**标题*/
	private TextView mTitleTv;
	/**完成*/
	private TextView mConfirmTv;

	public final static String ARG_PATH = "PATHS";
	public final static String ARG_CURRENT_ITEM = "ARG_CURRENT_ITEM";

	private ArrayList<String> paths;
	private ViewPager mViewPager;
	private PhotoPagerAdapter mPagerAdapter;

	public final static long ANIM_DURATION = 200L;

	public final static String ARG_THUMBNAIL_TOP = "THUMBNAIL_TOP";
	public final static String ARG_THUMBNAIL_LEFT = "THUMBNAIL_LEFT";
	public final static String ARG_THUMBNAIL_WIDTH = "THUMBNAIL_WIDTH";
	public final static String ARG_THUMBNAIL_HEIGHT = "THUMBNAIL_HEIGHT";
	public final static String ARG_HAS_ANIM = "HAS_ANIM";

	private int thumbnailTop = 0;
	private int thumbnailLeft = 0;
	private int thumbnailWidth = 0;
	private int thumbnailHeight = 0;

	private boolean hasAnim = false;

	private final ColorMatrix colorizerMatrix = new ColorMatrix();

	private int currentItem = 0;

	private Boolean fullscreen = false;//是否全屏展现预览界面的标志

	public static NewImagePagerDialogFragment getInstance(Context mContext, List<String> paths, int currentItem)
	{
		NewImagePagerDialogFragment newImagePagerDialogFragment = new NewImagePagerDialogFragment();
		newImagePagerDialogFragment.mContext = mContext;

		Bundle args = new Bundle();
		args.putStringArray(ARG_PATH, paths.toArray(new String[paths.size()]));
		args.putInt(ARG_CURRENT_ITEM, currentItem);
		args.putBoolean(ARG_HAS_ANIM, false);

		newImagePagerDialogFragment.setArguments(args);

		return newImagePagerDialogFragment;
	}

	public static NewImagePagerDialogFragment getInstance(Context mContext, List<String> paths, int currentItem, int[] screenLocation, int thumbnailWidth, int thumbnailHeight,boolean fullscreen) {

		NewImagePagerDialogFragment newImagePagerDialogFragment = getInstance(mContext, paths, currentItem);

		newImagePagerDialogFragment.getArguments().putInt(ARG_THUMBNAIL_LEFT, screenLocation[0]);
		newImagePagerDialogFragment.getArguments().putInt(ARG_THUMBNAIL_TOP, screenLocation[1]);
		newImagePagerDialogFragment.getArguments().putInt(ARG_THUMBNAIL_WIDTH, thumbnailWidth);
		newImagePagerDialogFragment.getArguments().putInt(ARG_THUMBNAIL_HEIGHT, thumbnailHeight);
		newImagePagerDialogFragment.getArguments().putBoolean(ARG_HAS_ANIM, true);
		newImagePagerDialogFragment.fullscreen = fullscreen;

		return newImagePagerDialogFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.w(TAG,"{onCreate}");
		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_NoTitleBar_Fullscreen);//全屏（在状态栏底下）

		paths = new ArrayList<>();

		Bundle bundle = getArguments();

		if (bundle != null) {
			String[] pathArr = bundle.getStringArray(ARG_PATH);
			paths.clear();
			if (pathArr != null) {

				paths = new ArrayList<>(Arrays.asList(pathArr));
			}

			hasAnim = bundle.getBoolean(ARG_HAS_ANIM);
			currentItem = bundle.getInt(ARG_CURRENT_ITEM);

			thumbnailTop = bundle.getInt(ARG_THUMBNAIL_TOP);
			thumbnailLeft = bundle.getInt(ARG_THUMBNAIL_LEFT);
			thumbnailWidth = bundle.getInt(ARG_THUMBNAIL_WIDTH);
			thumbnailHeight = bundle.getInt(ARG_THUMBNAIL_HEIGHT);
		}

		mPagerAdapter = new PhotoPagerAdapter(Glide.with(this), paths);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));//设置背景为透明，并且没有标题
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		//设置窗体全屏
		getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		myView = inflater.inflate(R.layout.__picker_picker_fragment_image_pager_dialog, container, false);

		mBackImg = (ImageView)myView.findViewById(R.id.backImg);
		mTitleTv = (TextView)myView.findViewById(R.id.titleText);
		mConfirmTv = myView.findViewById(R.id.confirmText);
		//返回图标的点击事件
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				runExitAnimation(new Runnable() {
					public void run() {
						dismiss();
					}
				});
			}
		});

		mTitleTv.setText((currentItem+1) + "/" + paths.size());//设置当前页/总页数

		mViewPager = (ViewPager) myView.findViewById(R.id.vp_photos);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(currentItem);
		mViewPager.setOffscreenPageLimit(5);

		// Only run the animation if we're coming from the parent activity, not if
		// we're recreated automatically by the window manager (e.g., device rotation)
		if (savedInstanceState == null && hasAnim) {
			ViewTreeObserver observer = mViewPager.getViewTreeObserver();
			observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {

					mViewPager.getViewTreeObserver().removeOnPreDrawListener(this);

					// Figure out where the thumbnail and full size versions are, relative
					// to the screen and each other
					int[] screenLocation = new int[2];
					mViewPager.getLocationOnScreen(screenLocation);
					thumbnailLeft = thumbnailLeft - screenLocation[0];
					thumbnailTop = thumbnailTop - screenLocation[1];

					runEnterAnimation();

					return true;
				}
			});
		}


		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				hasAnim = currentItem == position;
				mTitleTv.setText((position+1) + "/" + paths.size());//设置当前页/总页数
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		this.getDialog().setOnKeyListener(new DialogInterface.OnKeyListener()
		{
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					Log.w(TAG, "onKey");

					runExitAnimation(new Runnable() {
						public void run() {
							dismiss();
						}
					});
					return true; // return true是中断事件，那么下面的就接受不到按键信息了
				}else
				{
					return false; //在return false的时候 才会事件继续向下传递。
				}
			}
		});

		return myView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	/**
	 * 设置宽度和高度值，以及打开的动画效果
	 */
	@Override
	public void onStart() {
		super.onStart();

		if(fullscreen){
			//设置对话框的宽高，必须在onStart中
			Window window = this.getDialog().getWindow();
			window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);//全屏（盖住状态栏）
			window.setGravity(Gravity.BOTTOM);//设置在底部
			//打开的动画效果--缩放+渐隐
		}else{
			//从我的场景列表界面中设置按钮打开的
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


	public void setPhotos(List<String> paths, int currentItem) {
		this.paths.clear();
		this.paths.addAll(paths);
		this.currentItem = currentItem;

		mViewPager.setCurrentItem(currentItem);
		mViewPager.getAdapter().notifyDataSetChanged();
	}

	/**
	 * The enter animation scales the picture in from its previous thumbnail
	 * size/location, colorizing it in parallel. In parallel, the background of the
	 * activity is fading in. When the pictue is in place, the text description
	 * drops down.
	 */
	private void runEnterAnimation() {
		final long duration = ANIM_DURATION;

		// Set starting values for properties we're going to animate. These
		// values scale and position the full size version down to the thumbnail
		// size/location, from which we'll animate it back up
		ViewHelper.setPivotX(mViewPager, 0);
		ViewHelper.setPivotY(mViewPager, 0);
		ViewHelper.setScaleX(mViewPager, (float) thumbnailWidth / mViewPager.getWidth());
		ViewHelper.setScaleY(mViewPager, (float) thumbnailHeight / mViewPager.getHeight());
		ViewHelper.setTranslationX(mViewPager, thumbnailLeft);
		ViewHelper.setTranslationY(mViewPager, thumbnailTop);

		// Animate scale and translation to go from thumbnail to full size
		ViewPropertyAnimator.animate(mViewPager)
				.setDuration(duration)
				.scaleX(1)
				.scaleY(1)
				.translationX(0)
				.translationY(0)
				.setInterpolator(new DecelerateInterpolator());

		// Fade in the black background
		ObjectAnimator bgAnim = ObjectAnimator.ofInt(mViewPager.getBackground(), "alpha", 0, 255);
		bgAnim.setDuration(duration);
		bgAnim.start();

		// Animate a color filter to take the image from grayscale to full color.
		// This happens in parallel with the image scaling and moving into place.
		ObjectAnimator colorizer = ObjectAnimator.ofFloat(NewImagePagerDialogFragment.this,
				"saturation", 0, 1);
		colorizer.setDuration(duration);
		colorizer.start();

	}


	/**
	 * The exit animation is basically a reverse of the enter animation, except that if
	 * the orientation has changed we simply scale the picture back into the center of
	 * the screen.
	 *
	 * @param endAction This action gets run after the animation completes (this is
	 *                  when we actually switch activities)
	 */
	public void runExitAnimation(final Runnable endAction) {

		if (!getArguments().getBoolean(ARG_HAS_ANIM, false) || !hasAnim) {
			endAction.run();
			return;
		}

		final long duration = ANIM_DURATION;

		// Animate image back to thumbnail size/location
		ViewPropertyAnimator.animate(mViewPager)
				.setDuration(duration)
				.setInterpolator(new AccelerateInterpolator())
				.scaleX((float) thumbnailWidth / mViewPager.getWidth())
				.scaleY((float) thumbnailHeight / mViewPager.getHeight())
				.translationX(thumbnailLeft)
				.translationY(thumbnailTop)
				.setListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						endAction.run();
					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}
				});

		// Fade out background
		ObjectAnimator bgAnim = ObjectAnimator.ofInt(mViewPager.getBackground(), "alpha", 0);
		bgAnim.setDuration(duration);
		bgAnim.start();

		// Animate a color filter to take the image back to grayscale,
		// in parallel with the image scaling and moving into place.
		ObjectAnimator colorizer =
				ObjectAnimator.ofFloat(NewImagePagerDialogFragment.this, "saturation", 1, 0);
		colorizer.setDuration(duration);
		colorizer.start();
	}


	/**
	 * This is called by the colorizing animator. It sets a saturation factor that is then
	 * passed onto a filter on the picture's drawable.
	 *
	 * @param value saturation
	 */
	public void setSaturation(float value) {
		colorizerMatrix.setSaturation(value);
		ColorMatrixColorFilter colorizerFilter = new ColorMatrixColorFilter(colorizerMatrix);
		mViewPager.getBackground().setColorFilter(colorizerFilter);
	}


	public ViewPager getViewPager() {
		return mViewPager;
	}


	public ArrayList<String> getPaths() {
		return paths;
	}


	public int getCurrentItem() {
		return mViewPager.getCurrentItem();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		paths.clear();
		paths = null;

		if (mViewPager != null) {
			mViewPager.setAdapter(null);
		}
	}
}
