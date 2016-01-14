package com.example.mosaicimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 自定义的ImageView控制，可对图片进行多点触控缩放和拖动和打码
 * 
 * @author way
 */
public class MosaicView extends View {

	/**
	 * 初始化状态常量
	 */
	public static final int STATUS_INIT = 1;

	/**
	 * 图片放大状态常量
	 */
	public static final int STATUS_ZOOM_OUT_AND_MOVE = 2;

	/**
	 * 图片缩小状态常量
	 */
	public static final int STATUS_ZOOM_IN_AND_MOVE = 3;

	/**
	 * 图片拖动状态常量
	 */
	public static final int STATUS_MOVE = 4;

	/**
	 * 图片局部显示状态
	 */
	public static final int STATUS_PART = 5;

	/**
	 * 手指抬起时图片恢复状态
	 */
	public static final int STATUS_ACTION_UP = 6;

	/**
	 * 绘制笔触大小
	 */
	public static final int STATUS_DRAW_STOKE = 7;

	/**
	 * 放大镜的半径
	 */
	private int mPreviewRadius = 100;
	/**
	 * 放大倍数
	 */
	private final int mFactor = 1;
	/**
	 * 触摸公差
	 */
	private static final float TOUCH_TOLERANCE = 4;
	/**
	 * 笔触大小
	 */
	private static float mPaintStrokeWidth = 30;
	/**
	 * 当前笔触缩放倍数
	 */
	private float mStrokeMultiples = 1L;
	/**
	 * 用于对图片进行移动和缩放变换的矩阵
	 */
	private Matrix mMatrix = new Matrix();

	/**
	 * 待展示的Bitmap对象
	 */
	private Bitmap mSourceBitmap;

	private Bitmap mMosaicBitmap;

	/**
	 * 记录当前操作的状态，可选值为STATUS_INIT、STATUS_ZOOM_OUT、STATUS_ZOOM_IN和STATUS_MOVE
	 */
	private int mCurrentStatus;

	/**
	 * ZoomImageView控件的宽度
	 */
	private int mViewWidth;

	/**
	 * ZoomImageView控件的高度
	 */
	private int mViewHeight;

	/**
	 * 记录两指同时放在屏幕上时，中心点的横坐标值
	 */
	private float mTwoFingerCenterPointX;

	/**
	 * 记录两指同时放在屏幕上时，中心点的纵坐标值
	 */
	private float mTwoFingerCenterPointY;

	/**
	 * 记录当前图片的宽度，图片被缩放时，这个值会一起变动
	 */
	private float mCurrentBitmapWidth;

	/**
	 * 记录当前图片的高度，图片被缩放时，这个值会一起变动
	 */
	private float mCurrentBitmapHeight;

	/**
	 * 记录上次手指移动时的横坐标
	 */
	private float mLastXMove = -1;

	/**
	 * 记录上次手指移动时的纵坐标
	 */
	private float mLastYMove = -1;

	/**
	 * 记录手指在横坐标方向上的移动距离
	 */
	private float mMovedDistanceX;

	/**
	 * 记录手指在纵坐标方向上的移动距离
	 */
	private float mMovedDistanceY;

	/**
	 * 记录图片在矩阵上的横向偏移值
	 */
	private float mTotalTranslateX;

	/**
	 * 记录图片在矩阵上的纵向偏移值
	 */
	private float mTotalTranslateY;

	/**
	 * 记录图片在矩阵上的总缩放比例
	 */
	private float mTotalRatio;

	/**
	 * 记录手指移动的距离所造成的缩放比例
	 */
	private float mScaledRatio;

	/**
	 * 记录图片初始化时的缩放比例
	 */
	private float mInitRatio;

	/**
	 * 记录上次两指之间的距离
	 */
	private double mTwoFingerLastDis;

	/**
	 * 当前单指点击是X轴坐标
	 */
	private float mCurrentX;
	/**
	 * 当前单指点击是Y轴坐标
	 */
	private float mCurrentY;
	/**
	 * 当前局部显示图是否在左边
	 */
	private boolean IsPreviewLeft = true;

	/**
	 * 前景图绘制板
	 */
	private Canvas mSourceCanvas;
	/**
	 * 绘画笔
	 */
	private Paint mSourcePaint;
	private Path mTouchPath;
	/**
	 * 移动时点击位置相对bitmap的X轴坐标
	 */
	private float mX;
	/**
	 * 移动时点击位置相对bitmap的Y轴坐标
	 */
	private float mY;

	/**
	 * 是否为多指触摸
	 */
	private boolean isMultiTouch;

	public MosaicView(Context context) {
		this(context, null);
	}

	public MosaicView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public MosaicView(Context context, AttributeSet attrs, int style) {
		super(context, attrs, style);
		init(context);
	}

	private void init(Context context) {
		mPreviewRadius = DensityUtil.dip2px(getContext(), 50f);
		mPaintStrokeWidth = DensityUtil.dip2px(getContext(), 12f);

		mSourcePaint = new Paint();
		mSourcePaint.setAlpha(0);
		mSourcePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));// 取两层绘制交集。显示上层。
		mSourcePaint.setAntiAlias(true);

		mSourcePaint.setDither(true);
		mSourcePaint.setStyle(Paint.Style.STROKE);
		mSourcePaint.setStrokeJoin(Paint.Join.ROUND);
		mSourcePaint.setStrokeCap(Paint.Cap.ROUND);
		mTouchPath = new Path();
	}

	public void setSourceBitmap(Bitmap bitmap) {
		mCurrentStatus = STATUS_INIT;
		bitmap = zoomImage(bitmap, DensityUtil.getDisplayWidth(getContext()),
				DensityUtil.getDisplayHeight(getContext()));

		mSourceBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		mMosaicBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		mSourceCanvas = new Canvas(mSourceBitmap);
		mSourceCanvas.drawBitmap(bitmap, 0, 0, null);

		Canvas canvas = new Canvas(mMosaicBitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);
		bitmap.recycle();
		invalidate();
	}

	public void release() {
		if (mSourceBitmap != null && !mSourceBitmap.isRecycled())
			mSourceBitmap.recycle();
		if (mMosaicBitmap != null && !mMosaicBitmap.isRecycled())
			mMosaicBitmap.recycle();
		destroyDrawingCache();
	}

	public void reset(Bitmap bitmap) {
		mSourceBitmap.recycle();
		mSourceBitmap = null;

		bitmap = zoomImage(bitmap, DensityUtil.getDisplayWidth(getContext()),
				DensityUtil.getDisplayHeight(getContext()));
		mSourceBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		mSourceCanvas = new Canvas(mSourceBitmap);
		mSourceCanvas.drawBitmap(bitmap, 0, 0, null);
		bitmap.recycle();

		mCurrentStatus = STATUS_ACTION_UP;
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			// 分别获取到ZoomImageView的宽度和高度
			mViewWidth = getWidth();
			mViewHeight = getHeight();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_POINTER_DOWN:
			if (event.getPointerCount() == 2) {
				// 当有两个手指按在屏幕上时，计算两指之间的距离
				mTwoFingerLastDis = distanceBetweenFingers(event);
				isMultiTouch = true;
			}
			break;
		case MotionEvent.ACTION_DOWN:
			if (event.getPointerCount() == 1) {
				touchDown((event.getX() - mTotalTranslateX) / mTotalRatio,
						(event.getY() - mTotalTranslateY) / mTotalRatio);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() == 1 && !isMultiTouch) {
				// 只有单指按在屏幕上移动时，为查看局部状态
				mCurrentStatus = STATUS_PART;
				mCurrentX = event.getX();
				mCurrentY = event.getY();
				touchMove((mCurrentX - mTotalTranslateX) / mTotalRatio, (mCurrentY - mTotalTranslateY) / mTotalRatio);
				invalidate();
			} else if (event.getPointerCount() == 2) {
				// 拖动
				float xMove = (event.getX(0) + event.getX(1)) / 2;
				float yMove = (event.getY(0) + event.getY(1)) / 2;

				if (mLastXMove == -1 && mLastYMove == -1) {
					centerMovePointBetweenFingers(event);
				}
				mMovedDistanceX = xMove - mLastXMove;
				mMovedDistanceY = yMove - mLastYMove;
				// 进行边界检查，不允许将图片拖出边界
				if (mTotalTranslateX + mMovedDistanceX > 0) {
					mMovedDistanceX = 0;
				} else if (mViewWidth - (mTotalTranslateX + mMovedDistanceX) > mCurrentBitmapWidth) {
					mMovedDistanceX = 0;
				}
				if (mTotalTranslateY + mMovedDistanceY > 0) {
					mMovedDistanceY = 0;
				} else if (mViewHeight - (mTotalTranslateY + mMovedDistanceY) > mCurrentBitmapHeight) {
					mMovedDistanceY = 0;
				}

				// 缩放
				Boolean isDrag = false;
				centerPointBetweenFingers(event);
				double fingerDis = distanceBetweenFingers(event);
				if (fingerDis > mTwoFingerLastDis) {
					mCurrentStatus = STATUS_ZOOM_OUT_AND_MOVE;
				} else {
					mCurrentStatus = STATUS_ZOOM_IN_AND_MOVE;
				}
				// 进行缩放倍数检查，最大只允许将图片放大4倍，最小可以缩小到初始化比例
				if ((mCurrentStatus == STATUS_ZOOM_OUT_AND_MOVE && mTotalRatio < 4 * mInitRatio)
						|| (mCurrentStatus == STATUS_ZOOM_IN_AND_MOVE && mTotalRatio > mInitRatio)) {
					mScaledRatio = (float) (fingerDis / mTwoFingerLastDis);
					mTotalRatio = mTotalRatio * mScaledRatio;
					if (mTotalRatio > 4 * mInitRatio) {
						mTotalRatio = 4 * mInitRatio;
					} else if (mTotalRatio < mInitRatio) {
						mTotalRatio = mInitRatio;
					}

					isDrag = true;
				} else {
					mCurrentStatus = STATUS_MOVE;
				}

				// 调用onDraw()方法绘制图片
				invalidate();
				if (isDrag) {
					mTwoFingerLastDis = fingerDis;
				}
				centerMovePointBetweenFingers(event);
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (event.getPointerCount() == 2) {
				mCurrentStatus = STATUS_ACTION_UP;
				invalidate();
				// 手指离开屏幕时将临时值还原
				mLastXMove = -1;
				mLastYMove = -1;
			}
			break;
		case MotionEvent.ACTION_UP:
			isMultiTouch = false;
			// 手指离开屏幕时将临时值还原
			mCurrentStatus = STATUS_ACTION_UP;
			touchUp();
			invalidate();
			mLastXMove = -1;
			mLastYMove = -1;
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * 根据currentStatus的值来决定对图片进行什么样的绘制操作。
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		if (mSourceBitmap == null)
			return;
		super.onDraw(canvas);
		switch (mCurrentStatus) {
		case STATUS_PART:
			part(canvas);
			break;
		case STATUS_ZOOM_OUT_AND_MOVE:
		case STATUS_ZOOM_IN_AND_MOVE:
			move(canvas);
			zoom(canvas);
			break;
		case STATUS_MOVE:
			move(canvas);
			break;
		case STATUS_INIT:
			initBitmap(canvas);
			replyPosition(canvas);
			// 更新界面图片
			break;
		case STATUS_ACTION_UP:
			replyPosition(canvas);
			break;
		case STATUS_DRAW_STOKE:
			drawStrokeSize(canvas);
			break;
		default:
			canvas.drawBitmap(mSourceBitmap, mMatrix, null);
			break;
		}
	}

	private void touchDown(float x, float y) {
		mTouchPath.reset();
		mTouchPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touchMove(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mTouchPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	private void touchUp() {
		// mPath.lineTo(mX, mY);
		// commit the path to our offscreen
		mSourceCanvas.drawPath(mTouchPath, mSourcePaint);
		// kill this so we don't double draw
		mTouchPath.reset();
	}

	/**
	 * 局部图片窗口的绘制
	 * 
	 * @param canvas
	 */
	private void part(Canvas canvas) {
		float bitmapTop = mTotalTranslateY;// bitmap顶部Y轴值
		float bitmapBottom = bitmapTop + mSourceBitmap.getHeight() * mTotalRatio;// bitmap底部Y轴值
		float bitmapLeft = mTotalTranslateX;// bitmap左部X轴值
		float bitmapRight = bitmapLeft + mSourceBitmap.getWidth() * mTotalRatio;// bitmap右部X轴值

		float circleCenterX = mCurrentX;// 指示点圆心点X轴坐标
		float circleCenterY = mCurrentY;// 指示点圆心点Y轴坐标

		float partCenterX = mCurrentX;// 局部图中心点X轴坐标
		float partCenterY = mCurrentY;// 局部图中心点Y轴坐标

		if (mCurrentX < (mPreviewRadius * 2) && mCurrentY < (mPreviewRadius * 2) && IsPreviewLeft) {
			IsPreviewLeft = false;
		} else if ((mCurrentX > canvas.getWidth() - (mPreviewRadius * 2) && mCurrentY < (mPreviewRadius * 2))
				&& !IsPreviewLeft) {
			IsPreviewLeft = true;
		}

		if ((mCurrentY < bitmapTop + mPreviewRadius) || (mCurrentY > bitmapBottom - mPreviewRadius)
				|| (mCurrentX < bitmapLeft + mPreviewRadius) || (mCurrentX > bitmapRight - mPreviewRadius)) {
			if ((mCurrentY < bitmapTop + mPreviewRadius)) {// 准备上方越界
				partCenterY = mCurrentY + (bitmapTop + mPreviewRadius - mCurrentY);
			}
			if ((mCurrentY > bitmapBottom - mPreviewRadius)) {// 准备下方越界
				partCenterY = mCurrentY - (mCurrentY - bitmapBottom + mPreviewRadius);
			}
			if (mCurrentX < bitmapLeft + mPreviewRadius) {// 准备左方越界
				partCenterX = mCurrentX + (bitmapLeft + mPreviewRadius - mCurrentX);
			}
			if (mCurrentX > bitmapRight - mPreviewRadius) {// 准备右方越界
				partCenterX = mCurrentX - (mCurrentX - bitmapRight + mPreviewRadius);
			}
		}
		Path path = new Path();
		path.addRect(0, 0, mPreviewRadius * 2, mPreviewRadius * 2, Direction.CW);
		// 底图
		canvas.drawBitmap(mMosaicBitmap, mMatrix, null);
		canvas.drawBitmap(mSourceBitmap, mMatrix, null);
		mSourceCanvas.drawPath(mTouchPath, mSourcePaint);

		// 剪切
		if (IsPreviewLeft) {
			canvas.translate(0, 0);
		} else {
			canvas.translate(canvas.getWidth() - mPreviewRadius * 2, 0);
		}
		canvas.clipPath(path);
		// 画局部图
		canvas.translate(mPreviewRadius - partCenterX * mFactor, mPreviewRadius - partCenterY * mFactor);
		canvas.drawBitmap(mMosaicBitmap, mMatrix, null);
		canvas.drawBitmap(mSourceBitmap, mMatrix, null);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		// 绘制指示点中间
		paint.setColor(getResources().getColor(R.color.mosaicdark));
		paint.setStyle(Style.FILL);// 实心图案
		if (mInitRatio > 1) {
			canvas.drawCircle(circleCenterX, circleCenterY, (mPaintStrokeWidth - 5) / 2 * mInitRatio * mStrokeMultiples,
					paint);
		} else {
			canvas.drawCircle(circleCenterX, circleCenterY, (mPaintStrokeWidth - 5) / 2 / mInitRatio * mStrokeMultiples,
					paint);
		}
		// 绘制绘制指示点圆形形边框
		paint.setColor(getResources().getColor(R.color.mosaicblue));
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(DensityUtil.dip2px(getContext(), 2f));
		if (mTotalRatio > 1) {
			canvas.drawCircle(circleCenterX, circleCenterY, (mPaintStrokeWidth - DensityUtil.dip2px(getContext(), 2f)) / 2 * mInitRatio * mStrokeMultiples,
					paint);
		} else {
			canvas.drawCircle(circleCenterX, circleCenterY, (mPaintStrokeWidth - DensityUtil.dip2px(getContext(), 2f)) / 2 / mInitRatio * mStrokeMultiples,
					paint);
		}

		// 绘制白色边框
		paint.setStyle(Style.STROKE);// 空心图案
		paint.setStrokeWidth(2.5f);
		paint.setColor(Color.WHITE);
		canvas.drawRect(new RectF(partCenterX - mPreviewRadius + 1, partCenterY - mPreviewRadius + 1,
				partCenterX + mPreviewRadius - 1, partCenterY + mPreviewRadius - 1), paint);

	}

	/**
	 * 对图片进行缩放处理。
	 * 
	 * @param canvas
	 */
	private void zoom(Canvas canvas) {
		mSourcePaint.setStrokeWidth(mPaintStrokeWidth / mTotalRatio * mStrokeMultiples);
		mMatrix.reset();
		// 将图片按总缩放比例进行缩放
		mMatrix.postScale(mTotalRatio, mTotalRatio);
		float scaledWidth = mSourceBitmap.getWidth() * mTotalRatio;
		float scaledHeight = mSourceBitmap.getHeight() * mTotalRatio;
		float translateX = 0f;
		float translateY = 0f;
		// 如果当前图片宽度小于屏幕宽度，则按屏幕中心的横坐标进行水平缩放。否则按两指的中心点的横坐标进行水平缩放
		if (mCurrentBitmapWidth < mViewWidth) {
			translateX = (mViewWidth - scaledWidth) / 2f;
		} else {
			translateX = mTotalTranslateX * mScaledRatio + mTwoFingerCenterPointX * (1 - mScaledRatio);
			// 进行边界检查，保证图片缩放后在水平方向上不会偏移出屏幕
			if (translateX > 0) {
				translateX = 0;
			} else if (mViewWidth - translateX > scaledWidth) {
				translateX = mViewWidth - scaledWidth;
			}
		}
		// 如果当前图片高度小于屏幕高度，则按屏幕中心的纵坐标进行垂直缩放。否则按两指的中心点的纵坐标进行垂直缩放
		if (mCurrentBitmapHeight < mViewHeight) {
			translateY = (mViewHeight - scaledHeight) / 2f;
		} else {
			translateY = mTotalTranslateY * mScaledRatio + mTwoFingerCenterPointY * (1 - mScaledRatio);
			// 进行边界检查，保证图片缩放后在垂直方向上不会偏移出屏幕
			if (translateY > 0) {
				translateY = 0;
			} else if (mViewHeight - translateY > scaledHeight) {
				translateY = mViewHeight - scaledHeight;
			}
		}
		// 缩放后对图片进行偏移，以保证缩放后中心点位置不变
		mMatrix.postTranslate(translateX, translateY);
		mTotalTranslateX = translateX;
		mTotalTranslateY = translateY;
		mCurrentBitmapWidth = scaledWidth;
		mCurrentBitmapHeight = scaledHeight;
		canvas.drawBitmap(mMosaicBitmap, mMatrix, null);
		canvas.drawBitmap(mSourceBitmap, mMatrix, null);
	}

	/**
	 * 对图片进行平移处理
	 * 
	 * @param canvas
	 */
	private void move(Canvas canvas) {
		mMatrix.reset();
		// 根据手指移动的距离计算出总偏移值
		float translateX = mTotalTranslateX + mMovedDistanceX;
		float translateY = mTotalTranslateY + mMovedDistanceY;
		// 先按照已有的缩放比例对图片进行缩放
		mMatrix.postScale(mTotalRatio, mTotalRatio);
		// 再根据移动距离进行偏移
		mMatrix.postTranslate(translateX, translateY);
		mTotalTranslateX = translateX;
		mTotalTranslateY = translateY;
		canvas.drawBitmap(mMosaicBitmap, mMatrix, null);
		canvas.drawBitmap(mSourceBitmap, mMatrix, null);
	}

	/**
	 * 对图片进行初始化操作，包括让图片居中，以及当图片大于屏幕宽高时对图片进行压缩。
	 * 
	 * @param canvas
	 */
	private void initBitmap(Canvas canvas) {
		if (mSourceBitmap != null) {
			mMatrix.reset();
			int bitmapWidth = mSourceBitmap.getWidth();
			int bitmapHeight = mSourceBitmap.getHeight();
			if (bitmapWidth > mViewWidth || bitmapHeight > mViewHeight) {
				if (bitmapWidth - mViewWidth > bitmapHeight - mViewHeight) {
					// 当图片宽度大于屏幕宽度时，将图片等比例压缩，使它可以完全显示出来
					float ratio = mViewWidth / (bitmapWidth * 1.0f);
					mMatrix.postScale(ratio, ratio);
					float translateY = (mViewHeight - (bitmapHeight * ratio)) / 2f;
					// 在纵坐标方向上进行偏移，以保证图片居中显示
					mMatrix.postTranslate(0, translateY);
					mTotalTranslateY = translateY;
					mTotalRatio = mInitRatio = ratio;
				} else {
					// 当图片高度大于控件高度时，将图片等比例压缩，使它可以完全显示出来
					float ratio = mViewHeight / (bitmapHeight * 1.0f);
					mMatrix.postScale(ratio, ratio);
					float translateX = (mViewWidth - (bitmapWidth * ratio)) / 2f;
					// 在横坐标方向上进行偏移，以保证图片居中显示
					mMatrix.postTranslate(translateX, 0);
					mTotalTranslateX = translateX;
					mTotalRatio = mInitRatio = ratio;
				}
				mCurrentBitmapWidth = bitmapWidth * mInitRatio;
				mCurrentBitmapHeight = bitmapHeight * mInitRatio;
			} else {

				// 当图片的宽高都小于屏幕宽高时，直接让放大至一边贴边为止
				float ratio = 0;
				if ((mViewWidth / (bitmapWidth * 1.0f)) > (mViewHeight / (bitmapHeight * 1.0f))) {
					ratio = mViewHeight / (bitmapHeight * 1.0f);
				} else {
					ratio = mViewWidth / (bitmapWidth * 1.0f);
				}
				mMatrix.postScale(mTotalRatio, mTotalRatio);

				float translateY = (mViewHeight - (bitmapHeight * ratio)) / 2f;
				float translateX = (mViewWidth - (bitmapWidth * ratio)) / 2f;
				mMatrix.postTranslate(translateX, translateY);
				mTotalRatio = mInitRatio = ratio;
				mTotalTranslateX = translateX;
				mTotalTranslateY = translateY;
				mCurrentBitmapWidth = bitmapWidth * mInitRatio;
				mCurrentBitmapHeight = bitmapHeight * mInitRatio;
			}
			mMosaicBitmap = getMosaic(mMosaicBitmap);
			canvas.drawBitmap(mMosaicBitmap, mMatrix, null);
			canvas.drawBitmap(mSourceBitmap, mMatrix, null);
			mSourcePaint.setStrokeWidth(mPaintStrokeWidth / mTotalRatio * mStrokeMultiples);
		}
	}

	/**
	 * 绘制笔触大小
	 * 
	 * @param mStrokeMultiples
	 * @param canvas
	 */
	public void drawStrokeSize(Canvas canvas) {
		// 底图
		canvas.drawBitmap(mMosaicBitmap, mMatrix, null);
		canvas.drawBitmap(mSourceBitmap, mMatrix, null);
		mSourceCanvas.drawPath(mTouchPath, mSourcePaint);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		// 绘制绘制指示点原形边框
		paint.setColor(getResources().getColor(R.color.mosaicblue));
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(4f);
		if (mTotalRatio > 1) {
			canvas.drawCircle(mViewWidth / 2, mViewHeight / 2,
					(mPaintStrokeWidth - 4) / 2 * mInitRatio * mStrokeMultiples, paint);
		} else {
			canvas.drawCircle(mViewWidth / 2, mViewHeight / 2,
					(mPaintStrokeWidth - 4) / 2 / mInitRatio * mStrokeMultiples, paint);
		}
	}

	public void setStrokeMultiples(float strokeMultiples) {
		this.mStrokeMultiples = strokeMultiples;
		mSourcePaint.setStrokeWidth(mPaintStrokeWidth / mTotalRatio * strokeMultiples);
		mCurrentStatus = STATUS_DRAW_STOKE;
		invalidate();
	}

	public void removeStrokeView() {
		// TODO Auto-generated method stub
		mCurrentStatus = STATUS_ACTION_UP;
		invalidate();
	}

	/**
	 * 对图片进行恢复
	 * 
	 * @param canvas
	 */
	private void replyPosition(Canvas canvas) {
		mMatrix.reset();
		// 先按照已有的缩放比例对图片进行缩放
		mMatrix.postScale(mTotalRatio, mTotalRatio);
		// 再根据移动距离进行偏移
		mMatrix.postTranslate(mTotalTranslateX, mTotalTranslateY);
		canvas.drawBitmap(mMosaicBitmap, mMatrix, null);
		canvas.drawBitmap(mSourceBitmap, mMatrix, null);
	}

	/**
	 * 计算两个手指之间的距离。
	 * 
	 * @param event
	 * @return 两个手指之间的距离
	 */
	private double distanceBetweenFingers(MotionEvent event) {
		float disX = Math.abs(event.getX(0) - event.getX(1));
		float disY = Math.abs(event.getY(0) - event.getY(1));
		return Math.sqrt(disX * disX + disY * disY);
	}

	/**
	 * 计算两个手指之间中心点的坐标。
	 * 
	 * @param event
	 */
	private void centerPointBetweenFingers(MotionEvent event) {
		float xPoint0 = event.getX(0);
		float yPoint0 = event.getY(0);
		float xPoint1 = event.getX(1);
		float yPoint1 = event.getY(1);
		mTwoFingerCenterPointX = (xPoint0 + xPoint1) / 2;
		mTwoFingerCenterPointY = (yPoint0 + yPoint1) / 2;
	}

	/**
	 * 计算移动时两个手指之间中心点的坐标。
	 * 
	 * @param event
	 */
	private void centerMovePointBetweenFingers(MotionEvent event) {
		float xPoint0 = event.getX(0);
		float yPoint0 = event.getY(0);
		float xPoint1 = event.getX(1);
		float yPoint1 = event.getY(1);
		mLastXMove = (xPoint0 + xPoint1) / 2;
		mLastYMove = (yPoint0 + yPoint1) / 2;
	}

	/**
	 * 合并两张bitmap为一张
	 * 
	 * @param background
	 * @param foreground
	 * @return Bitmap
	 */
	public Bitmap getMosaicBitmap() {
		if (mMosaicBitmap == null) {
			return null;
		}
		int bgWidth = mMosaicBitmap.getWidth();
		int bgHeight = mMosaicBitmap.getHeight();
		int fgWidth = mSourceBitmap.getWidth();
		int fgHeight = mSourceBitmap.getHeight();
		Bitmap newmap = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(newmap);
		canvas.drawBitmap(mMosaicBitmap, 0, 0, null);
		canvas.drawBitmap(mSourceBitmap, (bgWidth - fgWidth) / 2, (bgHeight - fgHeight) / 2, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return newmap;
	}

	/**
	 * 马赛克效果(Native)
	 * 
	 * @param bitmap
	 *            原图
	 * 
	 * @return 马赛克图片
	 * 
	 */
	public static Bitmap getMosaic(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int radius = width / 30;

		Bitmap mosaicBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(mosaicBitmap);

		int horCount = (int) Math.ceil(width / (float) radius);
		int verCount = (int) Math.ceil(height / (float) radius);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		for (int horIndex = 0; horIndex < horCount; ++horIndex) {
			for (int verIndex = 0; verIndex < verCount; ++verIndex) {
				int l = radius * horIndex;
				int t = radius * verIndex;
				int r = l + radius;
				if (r > width) {
					r = width;
				}
				int b = t + radius;
				if (b > height) {
					b = height;
				}
				int color = bitmap.getPixel(l, t);
				Rect rect = new Rect(l, t, r, b);
				paint.setColor(color);
				canvas.drawRect(rect, paint);
			}
		}
		canvas.save();

		return mosaicBitmap;
	}

	/***
	 * 图片的缩放方法
	 * 
	 * @param bgimage
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return
	 */
	public Bitmap zoomImage(Bitmap bgimage, int newWidth, int newHeight) {
		// 获取这个图片的宽和高
		int width = bgimage.getWidth();
		int height = bgimage.getHeight();
		// 执行该缩放方法的条件有，有一边小月屏幕的大小或者图片的高度大于长度
		double x = width * newHeight;
		double y = height * newWidth;

		if (x > y) {
			newHeight = (int) (y / (double) width);
		} else if (x < y) {
			newWidth = (int) (x / (double) height);
		}

		if (newWidth > width && newHeight > height) {
			newWidth = width;
			newHeight = height;
		}
		Matrix matrix = new Matrix();
		matrix.reset();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / (float) width;
		float scaleHeight = ((float) newHeight) / (float) height;
		matrix.postScale(scaleWidth, scaleHeight);

		bgimage = Bitmap.createBitmap(bgimage, 0, 0, (int) width, (int) height, matrix, true);
		return bgimage;
	}

}