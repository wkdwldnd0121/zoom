package com.jbj.zoom.features.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class CameraManager {
    private static CameraManager cameraManager;
    private static int maxCamera;
    private static int currentCamera = 0;    // 기본카메라 번호

    private CameraManager() {
    }

    public static CameraManager getCameraManager() {
        if (cameraManager == null) {
            cameraManager = new CameraManager();
        }
        maxCamera = Camera.getNumberOfCameras();
        return cameraManager;

    }

    //카메라 요청하기
    public Camera getCamera() {
        Camera camera = null;

        try {
            camera = Camera.open();     //.open()으로 요청 실패하면 catch로 넘어감
            Camera.Parameters cameraParameters = camera.getParameters();    //카메라 속성 불러오기
            if (cameraParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {//카메라의 초점 조절하는거 자동사용
                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(cameraParameters);
            }
        } catch (Exception ex) {
            Log.e("CameraManager", ex.toString());
            System.exit(1);
        }
        return camera;
    }

    public Camera getNextCamera() {
        Camera camera = null;

        try {
            currentCamera = (currentCamera + 1) % maxCamera;
            camera = Camera.open(currentCamera);     //.open()으로 요청 실패하면 catch로 넘어감
            Camera.Parameters cameraParameters = camera.getParameters();    //카메라 속성 불러오기
            if (cameraParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {//카메라의 초점 조절하는거 자동사용
                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(cameraParameters);
            }
        } catch (Exception ex) {
            Log.e("CameraManager", ex.toString());
            System.exit(1);
        }
        return camera;
    }


    public boolean checkCameraUsable(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }


    public void takeAndSaveImage(Camera camera) {
        camera.takePicture(null, null, getTakePictureCallback());
    }

    //파일저장
    //이미지가 byte형식으로 들어옴 data로 들어와서 byte[]에 저장
    private Camera.PictureCallback getTakePictureCallback() {
        return new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                camera.startPreview();                      //사진 찍고 화면 안멈추게함
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.e("CameraManager", "파일 생성 실패");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile); //picture file에 저장
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.e("CameraManager", "파일 찾기 실패" + e.getMessage());
                } catch (IOException e) {
                    Log.e("CameraManager", "파일 찾기 실패" + e.getMessage());
                } catch (Exception e) {
                    Log.e("CameraManager", "파일 찾기 실패" + e.getMessage());
                }
            }
        };
    }

    //파일을 저장할 폴더 만들기
    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), //사진 저장영역에 zoompictures라는 하위 폴더 경로 설정
                "ZoomPictures"
        );

        //mkdirs()가 경로랑 파일 만들어줌
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("CameraManager", "파일 디렉토리 생성 실패");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("YYYYMMDDHHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }

    public boolean isFrontCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCamera, cameraInfo);
        return cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

}
