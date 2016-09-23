package com.runing.mymaskimageview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Px;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.runing.view.MaskImageView;

public class MainActivity extends AppCompatActivity {

    private MaskImageView mMaskImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mMaskImageView = (MaskImageView) findViewById(R.id.miv_content);
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
    }

}
