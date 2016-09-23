package com.runing.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.runing.library_maskimageview.R;

import java.lang.ref.WeakReference;

/**
 * 形状遮罩ImageView
 * Created by runing on 2016/9/17.
 */

public class MaskImageView extends ImageView {

    public static final int MASK_MODE_SHADER = 0;
    public static final int MASK_MODE_XFERMODE = 1;

    /**
     * 居中
     */
    private boolean mIsCenter = false;
    /**
     * 遮罩模式
     */
    private int mMaskMode = MASK_MODE_SHADER;
    /**
     * 原图缓存
     */
    private WeakReference<Bitmap> mBitmapCache;

    private int mWidth;
    private int mHeight;

    private Paint mPaint;

    private Xfermode mXfermode;
    private BitmapShader mBitmapShader;

    /**
     * 默认椭圆形遮罩
     */
    public static final DrawMask MASK_CIRCLE = new DrawMask() {
        @Override
        public void onDrawMask(Canvas canvas, Paint paint, int viewW, int viewH) {
            canvas.drawOval(new RectF(0F, 0F, viewW * 1F, viewH * 1F), paint);
        }
    };

    /**
     * 默认圆角矩形遮罩
     */
    public static final DrawMask MASK_ROUND_RECT = new DrawMask() {
        @Override
        public void onDrawMask(Canvas canvas, Paint paint, @Px int viewW, @Px int viewH) {
            canvas.drawRoundRect(new RectF(0F, 0F, viewW * 1F, viewH * 1F), viewW / 6F,
                    viewW / 6F, paint);
        }
    };

    private DrawMask mDrawMask = MASK_CIRCLE;

    public MaskImageView(Context context) {
        super(context, null, 0);
    }

    public MaskImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 遮罩绘制外部接口
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.getResources().obtainAttributes(attrs,
                R.styleable.MaskImageView);

        mMaskMode = array.getInt(R.styleable.MaskImageView_mask_mode, MASK_MODE_XFERMODE);
        mIsCenter = array.getBoolean(R.styleable.MaskImageView_center, false);
        final int maskType = array.getInt(R.styleable.MaskImageView_mask_shape, 0);
        if (maskType == 0) {
            mDrawMask = MASK_CIRCLE;
        } else if (maskType == 1) {
            mDrawMask = MASK_ROUND_RECT;
        }

        array.recycle();
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        initSrcBitmap();
    }

    private void initSrcBitmap() {
        if (mWidth != 0 && mHeight != 0) {
            Bitmap srcBitmap = getScaleSrcBitmap(getDrawable());
            mBitmapCache = new WeakReference<>(srcBitmap);
            // 初始化BitmapShader
            if (mMaskMode == MASK_MODE_SHADER) {
                mBitmapShader = new BitmapShader(srcBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            }
        }
    }

    /**
     * 设置遮罩模式
     *
     * @param maskMode {shader, xfermode}
     */
    public void setMaskMode(int maskMode) {
        this.mMaskMode = maskMode;
    }

    /**
     * 设置Canvas遮罩
     *
     * @param mDrawMask canvas 绘制
     */
    public void setDrawMask(DrawMask mDrawMask) {
        this.mDrawMask = mDrawMask;
        invalidate();
    }

    public interface DrawMask {
        /**
         * 绘制遮罩
         *
         * @param canvas 画布
         * @param paint  画笔
         * @param viewW  view宽度
         * @param viewH  view高度
         */
        void onDrawMask(Canvas canvas, Paint paint, @Px int viewW, @Px int viewH);

    }

    private void init() {
        mPaint = new Paint();

        if (mMaskMode == MASK_MODE_XFERMODE) {
            mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        Bitmap mixBitmap = getMixBitmap(drawable);
        canvas.drawBitmap(mixBitmap, 0, 0, null);
    }

    /**
     * 获取混合后的图像
     *
     * @param drawable drawable
     * @return result
     */
    private Bitmap getMixBitmap(Drawable drawable) {
        Bitmap src = mBitmapCache == null ? null : mBitmapCache.get();
        if (src == null) {
            mBitmapCache = new WeakReference<>(src = getScaleSrcBitmap(drawable));
        }
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        mPaint.reset();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        // 使用Shader或Xfermode绘制
        if (mMaskMode == MASK_MODE_SHADER) {
            mPaint.setShader(mBitmapShader);
            mDrawMask.onDrawMask(canvas, mPaint, mWidth, mHeight);

        } else if (mMaskMode == MASK_MODE_XFERMODE) {
            mDrawMask.onDrawMask(canvas, mPaint, mWidth, mHeight);
            mPaint.setXfermode(mXfermode);
            canvas.drawBitmap(src, 0, 0, mPaint);
            mPaint.setXfermode(null);
        }
        return bitmap;
    }

    private Bitmap getScaleSrcBitmap(Drawable drawable) {

        final int bW = drawable.getIntrinsicWidth();
        final int bH = drawable.getIntrinsicHeight();

        final float scale = Math.max(mWidth * 1F / bW, mHeight / 1F / bH);

        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        final int imgW = (int) (bW * scale);
        final int imgH = (int) (bH * scale);

        int boundL = 0;
        int boundT = 0;

        // 图像居中
        if (mIsCenter) {
            if (imgW > mWidth) {
                boundL = (mWidth - imgW) >> 1;
            } else {
                boundT = (mHeight - imgH) >> 1;
            }
        }
        drawable.setBounds(boundL, boundT, boundL + imgW, boundT + imgH);

        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        throw new UnsupportedOperationException("not support set scale type!");
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        initMaskMode();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        initMaskMode();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        initMaskMode();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        initMaskMode();
    }

    /**
     * 设置新资源时重新初始化Shader
     */
    private void initMaskMode() {
        mBitmapCache = null;
        initSrcBitmap();
    }

}
