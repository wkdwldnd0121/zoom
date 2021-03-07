package com.jbj.zoom.features.chat;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

public class ChatClient {
    private Socket socket;
    private Handler messageHandler;

    public ChatClient(final Handler messageHandler) {
        this.messageHandler = messageHandler;
        try {
            IO.Options options = IO.Options.builder()   //builder 클라이언트 만들어주는거
                    .setPath("/chat/")                  //지정한 경로로 가게함 /chat/
                    .setTransports(new String[]{WebSocket.NAME})    //websocket사용할거임
                    .build();
            this.socket = IO.socket("http://192.168.0.17:5000", options);  //내가 접속할 곳  내컴퓨터 ip주소
            this.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {   //서버랑 연결되었을때  'connected'출력
                @Override
                public void call(Object... args) {
                    Log.i("[WebSocket", "connected");
                }
            });
            this.socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() { //연결시도 실패 접속 실패
                @Override
                public void call(Object... args) {
                    Log.i("[WebSocket]", args[0].toString());   //args[0]출력
                }
            });
            this.socket.on(ChatEvent.GET_MESSAGE, new Emitter.Listener() {  //서버가 zoom room에다가 메시지 다 뿌릴때
                @Override
                public void call(Object... args) {  // object는 type이 명확하게정해지지 않음
                    Message message = new Message();
                    message.obj = args[0].toString();   //.toString()으로 문자열로 바꿈
                    messageHandler.sendMessage(message);
                }
            });
            this.socket.connect();  //연결 시도

        } catch (URISyntaxException e) {
            Log.e("[WebSocket]", "Failed to initialize");
        }
    }

    public void send(String message) {
        this.socket.emit(ChatEvent.NEW_MESSAGE, message);
    }  //eventtype이 NEW_MESSAGE
}
