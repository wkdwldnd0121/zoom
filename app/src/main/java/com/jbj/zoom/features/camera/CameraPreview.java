package com.jbj.zoom.features.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.jbj.zoom.MainActivity;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {  // extends surfaceview 화면 덮기
    private Camera camera;
    private SurfaceHolder holder;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;

        this.holder = getHolder();
        this.holder.addCallback(this);
        this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override           // 화면 만들기
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            this.camera.setPreviewDisplay(holder);      // 미리보기 관리  화면에서 보여줄 방향 방향을 보여주기 위해 각도가 필요함
            this.camera.setDisplayOrientation(this.getDegree());

              this.camera.startPreview();             //미리보기 생성해주는거

        } catch (IOException e) {
            Log.d("CameraPreview", "미리보기 생성 실패" + e.getMessage());
        }
    }

    @Override       //화면 바꼇을때
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (this.holder.getSurface() == null) {
            return;
        }
        try {       //화면 없애고
            this.camera.stopPreview();
        } catch (Exception e) {

        }
        try {           //새로 그리기
            this.camera.setPreviewDisplay(this.holder);
            this.camera.setDisplayOrientation(this.getDegree()); //화면방향지정
            this.camera.startPreview();

        } catch (Exception e) {
            Log.d("CameraPreview", "미리보기 생성 실패" + e.getMessage());
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            this.camera.stopPreview();
            this.camera.setPreviewCallback(null);
            this.camera.release();
            this.camera = null;

        } catch (Exception e) {

        }

    }

    // 카메라 각도조절
    private int getDegree() {
        Activity currentActivity = (Activity) (this.getContext());
        int rotation = currentActivity.getWindowManager().getDefaultDisplay().getRotation();    //windowmanager 화면관리하는 거
        switch (rotation) {
            case Surface.ROTATION_90:
                return 0;
            case Surface.ROTATION_180:
                return 270;
            case Surface.ROTATION_270:
                return 180;
            default:
                return 90;
        }
    }

    //기존 미리보기 없애고
    //newCamera에 설정해서 새로 시작
    public void changeCamera(Camera newCamera) {
        try {
            this.camera.stopPreview();
            this.camera.setPreviewCallback(null);
            this.camera.release();
            this.camera = null;
        } catch (Exception e) {

        }
        try {
            newCamera.setPreviewDisplay(this.holder);
            newCamera.setDisplayOrientation(this.getDegree());
            newCamera.startPreview();
            this.camera = newCamera;
        } catch (Exception e) {
            Log.d("CameraPreView", "미리보기 변경 실패" + e.getMessage());
        }

    }

}
