# MaskImageView

可添加遮罩的ImageView，可在外部通过canvas绘制遮罩，内置椭圆和圆角矩形两种遮罩，可选择Xfermode或BitmapShader绘制遮罩

![round](https://github.com/wangruning/MyMaskImageView/blob/master/image/circle.png)

![round](https://github.com/wangruning/MyMaskImageView/blob/master/image/round_rect.png)

## 使用

- 直接在布局中使用

```xml
<com.runing.view.MaskImageView
    android:id="@+id/miv_content"
    android:layout_width="200dp"
    android:layout_height="200dp"
    android:src="@drawable/img0"
    app:center="true"
    app:mask_mode="xfermode"
    app:mask_shape="circle" />
```

- 在代码中设置自定义遮罩

```java
mMaskImageView.setDrawMask(new MaskImageView.DrawMask() {
    Path mPath = new Path();

    @Override
    public void onDrawMask(Canvas canvas, Paint paint, @Px int viewW, @Px int viewH) {
        mPath.reset();
        mPath.moveTo(viewW / 4, 0);
        mPath.lineTo(viewW / 4 * 3, 0);
        mPath.lineTo(viewW, viewH);
        mPath.lineTo(0, viewH);
        mPath.close();
        canvas.drawPath(mPath, paint);
    }
});
```

![round](https://github.com/wangruning/MyMaskImageView/blob/master/image/custom_mask.png)
