package com.android.zhsl.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @category 带辐射效果的view
 *
 *
 */
public class RadiationView extends View {
	/**
	 * 画笔
	 */
	private Paint mPaint = new Paint();

	/**
	 * 颜色
	 */
	private int mColor = 0x3f990e;

	/**
	 * 透明度
	 */
	private int mAlpha = 60;

	private Handler mHandler = null;

	private static final int MESSAGE_DRAW = 0;
	private static final String TAG = "RadiationView";

	private int width;
	private int height;

	/**
	 * 扩散速度
	 */
	private int speed = 30;
	/**
	 * 最大辐射半径
	 */
	private int maxRadius = 100;

	private int centerX;
	private int centerY;

	/**
	 * 最小半径
	 */
	private int minRadius = 70;
	/**
	 * 当前半径
	 */
	private int radius = minRadius;

	private boolean isStarted = false;

	public RadiationView(Context context) {
		super(context);
		init();
	}

	public RadiationView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		init();
	}

	public void startRadiate() {
		isStarted = true;
		mHandler.sendEmptyMessage(MESSAGE_DRAW);
	}

	public void stopRadiate() {
		isStarted = false;
		mHandler.removeMessages(MESSAGE_DRAW);
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setStrokeWidth(1);
		// 必须先设置color，再设置alpha
		mPaint.setColor(mColor);
		mPaint.setAlpha(mAlpha);

		mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (msg.what == MESSAGE_DRAW) {
					invalidate();
					if (isStarted) {
						sendEmptyMessageDelayed(MESSAGE_DRAW, speed);
					}
				}
			}
		};
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
							int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		width = this.getWidth();
		height = this.getHeight();
		if (width <= 0 || height <= 0) {
			throw new RuntimeException("size illegal");
		}
		// 中心点
		centerX = width / 2;
		centerY = height / 2;
		// 最大辐射半径
		maxRadius = (width > height) ? height / 2 : width / 2;
		Log.i(TAG, "MAX" + maxRadius);
		if (maxRadius < 70) {
			throw new RuntimeException("size too small");
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setColor(mColor);
		mPaint.setAlpha(mAlpha);
		if (radius <= 0) {
			return;
		}
		if (radius > maxRadius) {
			radius = minRadius;
		}
		canvas.save();
		canvas.drawCircle(centerX, centerY, radius, mPaint);
		canvas.restore();
		radius += 1;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	public void setColor(int color) {
		this.mColor = color;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void setMinRadius(int radius) {
		this.minRadius = radius;
	}
}
