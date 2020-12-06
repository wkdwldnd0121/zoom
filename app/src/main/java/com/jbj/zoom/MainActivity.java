package com.jbj.zoom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.jbj.zoom.features.camera.CameraManager;
import com.jbj.zoom.features.camera.CameraPreview;
import com.jbj.zoom.features.camera.CameraStreamView;

import java.io.ByteArrayOutputStream;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 100001;        //권한요청번호
    private static final int PERMISSION_REQUEST_SAVE_FILE = 100002;
    private static CameraPreview cameraPreview; //전역 변수 선언
    private static Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 앱 실행시 권한 검사
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //카메라 권한이 있는지 확인 후 없으면 요청
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                return;
            }
            //저장공간 권한이 있느지 확인 후 없으면 요청
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_SAVE_FILE);
                return;
            }
        }


        //카메라가 사용가능한지 확인하기
        CameraManager manager = CameraManager.getCameraManager();
        if (!manager.checkCameraUsable(this)) {
            new AlertDialog.Builder(this)
                    .setMessage("카메라 사용불가")
                    .setNeutralButton("종료", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .show();
        }

        Camera camera = manager.getCamera();
        cameraPreview = new CameraPreview(this, camera);
        FrameLayout preview = findViewById(R.id.camera_preview);     // framelayout 만든거 가져옴
        preview.addView(cameraPreview);                             // 미리보기 띠우기
        this.camera = camera;                                       //요청된 카메라 activity에 보관


        FrameLayout preview2 = findViewById(R.id.camera_preview_second);
        final CameraStreamView streamView = new CameraStreamView(this);

        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Parameters parameters = camera.getParameters();
                int width = parameters.getPreviewSize().width;
                int height = parameters.getPreviewSize().height;

                YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

                byte[] bytes = out.toByteArray();

                streamView.drawStream(bytes);
            }
        });
        preview2.addView(streamView);
    }


    // 사용자 권한에 대해 반응한 결과 받기
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA:
            case PERMISSION_REQUEST_SAVE_FILE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //권한 승인이 된 경우 첨부터 다시 실행해서 다음 단계 진행
                    recreate();
                } else {
                    // 권한 승인이 안된 경우 종료
                    finish();
                }
                break;
            default:
                break;
        }
    }

    public void changeCamera(View view) {
        CameraManager manager = CameraManager.getCameraManager();
        Camera camera = manager.getNextCamera();   //카메라 순서대로 바꾸기
        cameraPreview.changeCamera(camera);
        this.camera = camera;
    }

    public void takePicture(View view) {         // View는 화면에 표시되는 모든거 View view는 이벤트핸들러 역할
        CameraManager cameraManager = CameraManager.getCameraManager();
        cameraManager.takeAndSaveImage(this.camera);        //takeAndSaveImage함수실행
        Toast.makeText(this, "저장완료", Toast.LENGTH_LONG).show();
    }


}
