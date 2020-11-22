package com.jbj.zoom.features.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;

public class CameraManager {
    private static CameraManager cameraManager;


    private CameraManager(){}
    public static CameraManager getCameraManager(){
        if(cameraManager == null){
            cameraManager = new CameraManager();
        }
        return cameraManager;

    }


    //카메라 요청하기
    public Camera getCamera(){
        Camera camera = null;

        try {
            camera = Camera.open();     //.open()으로 요청
            Camera.Parameters cameraParameters = camera.getParameters();
            if(cameraParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(cameraParameters);

            }
        }
        catch (Exception ex){
            Log.e("CameraManager",ex.toString());
            System.exit(1);
        }
        return camera;
    }



    public boolean checkCameraUsable(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        }
        else {
            return false;
        }
    }



}
