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
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.jbj.zoom.features.camera.CameraManager;
import com.jbj.zoom.features.camera.CameraPreview;
import com.jbj.zoom.features.camera.CameraStreamView;
import com.jbj.zoom.features.chat.ChatClient;
import com.jbj.zoom.features.chat.ChatEvent;
import com.jbj.zoom.features.chat.ChatTextAdapter;
import com.jbj.zoom.features.chat.ChatUpdateEvent;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 100001;        //권한요청번호
    private static final int PERMISSION_REQUEST_SAVE_FILE = 100002;
    private static final int PERMISSION_REQUEST_INTERNET = 100003;

    private static CameraPreview cameraPreview; //전역 변수 선언
    private static Camera camera;

    private List<CameraStreamView> streamViewList = new ArrayList<>();
    private ChatTextAdapter chatTextAdapter;
    private ChatClient chatClient;

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
            //인터넷 연결 권한 있는지 확인 후 없으면 요청
            if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_INTERNET);
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

//        final CameraStreamView streamView = new CameraStreamView(this);
//        this.streamViewList.add(streamView);
//        LinearLayout streamList = findViewById(R.id.stream_list);
//        streamList.addView(streamView);

        this.addStreamView(null);

        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                MainActivity.this.updateStreamView(data, camera);
            }
        });
        this.camera = camera;              //요청된 카메라 activity에 보관

        this.chatTextAdapter = new ChatTextAdapter(this);

        ListView chatList = new ListView(this);
        chatList.setAdapter(this.chatTextAdapter);

        preview.addView(chatList);

        this.chatClient = new ChatClient(this.getMessageHandler());
        this.chatClient.send("hello?");

    }

    public void sendMessage(View view) {
        EditText editText = findViewById(R.id.message_edit);
        String message = editText.getText().toString();
        this.chatClient.send(message);
//        this.chatTextAdapter.addMessage(message);
//        this.chatTextAdapter.notifyDataSetChanged();
//        this.chatTextAdapter.removeMessage();
    }

    public Handler getMessageHandler() {
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case ChatUpdateEvent.RECEIVE_MESSAGE:
                        String message = (String) msg.obj;
                        chatTextAdapter.addMessage(message);
                        break;
                    case ChatUpdateEvent.UPDATE_MESSAGE:
                        List<String> messageList = (List<String>) msg.obj;
                        chatTextAdapter.updateMessage(messageList);
                        break;
                    default:
                        break;
                }
                chatTextAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    // 사용자 권한에 대해 반응한 결과 받기
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA:
            case PERMISSION_REQUEST_SAVE_FILE:
            case PERMISSION_REQUEST_INTERNET:
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

    //카메라바꾸기
    public void changeCamera(View view) {
        CameraManager manager = CameraManager.getCameraManager();
        Camera camera = manager.getNextCamera();   //카메라 순서대로 바꾸기
        cameraPreview.changeCamera(camera);
        final CameraStreamView streamView = new CameraStreamView(this);

        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                MainActivity.this.updateStreamView(data, camera);
            }
        });

        this.camera = camera;


    }

    public void takePicture(View view) {         // View는 화면에 표시되는 모든거 View view는 이벤트핸들러 역할
        CameraManager cameraManager = CameraManager.getCameraManager();
        cameraManager.takeAndSaveImage(this.camera);        //takeAndSaveImage 함수실행
        Toast.makeText(this, "저장완료", Toast.LENGTH_LONG).show();
    }

    //화면추가
    public void addStreamView(View view) {
        final CameraStreamView streamView = new CameraStreamView(this);
        this.streamViewList.add(streamView);    //새로만든거 리스트에 추가
        LinearLayout streamLayout = findViewById(R.id.stream_list); //지금 추가된 화면을 출력
        final LinearLayout userView = new LinearLayout(this); //userView를 만듬
        userView.setOrientation(LinearLayout.VERTICAL);
        Button closeButton = new Button(this);
        userView.addView(streamView);   //streamview나오고
        userView.addView(closeButton);  //밑에 button이 나옴
        streamLayout.addView(userView); //addview는 하나밖에 추가 안되서 여러개를 묶어서 한번에 보내줘야댐
        closeButton.setText("종료");
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.removeStreamView(userView, streamView);
            }
        });
    }

    public void updateStreamView(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

        CameraManager manager = CameraManager.getCameraManager();

        byte[] bytes = out.toByteArray();
        for (CameraStreamView stream : this.streamViewList) {
            stream.drawStream(bytes, parameters.getJpegThumbnailSize(), manager.isFrontCamera());
        }
    }

    // 삭제를 위한 함수
    public void removeStreamView(LinearLayout view, CameraStreamView streamView) {
        LinearLayout streamLayout = findViewById(R.id.stream_list);
        streamLayout.removeViewInLayout(view);  // 전달받은 미리보기를 지우기
        this.streamViewList.remove(streamView);
    }

}
