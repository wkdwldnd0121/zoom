package com.jbj.zoom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.jbj.zoom.features.camera.CameraManager;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 100001;

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
        }

        //카메라가 사용가능한지 확인하기
        CameraManager manager = CameraManager.getCameraManager();
        if(!manager.checkCameraUsable(this)){
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
        else {
            new AlertDialog.Builder(this)
                    .setMessage("카메라 사용가능").show();

        }

    }



    // 사용자 권한에 대해 반응한 결과 받기
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA:
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
}
