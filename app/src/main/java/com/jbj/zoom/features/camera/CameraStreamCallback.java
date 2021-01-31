package com.jbj.zoom.features.camera;

import android.hardware.Camera;

public interface CameraStreamCallback {
    void drawStream(byte[] buffer, Camera.Size size, boolean isFront); //연속적으로 들어오는 화면출력

}
