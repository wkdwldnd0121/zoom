package com.jbj.zoom.features.chat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatTextAdapter extends BaseAdapter {  //baseadapter를 extends로 상속받음 리스트를 출력해주기 위해
    private Context context;
    private List<String> chatTextList = new ArrayList<>();  //메시지 보관리스트

    public ChatTextAdapter(Context context) {
        this.context = context;
    }


    public void addMessage(String message) {
        this.chatTextList.add(message);

    }
//    public void removeMessage(){
//        this.chatTextList.remove(0);
//    }

    @Override
    public int getCount() {         //가지고 있는 메시지 크기
        return this.chatTextList.size();
    }

    @Override
    public Object getItem(int position) {        // position은 index로 쓰임
        return this.chatTextList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {     //화면에 출력해주는거
        TextView message = new TextView(this.context);          //textview를 만들어서
        message.setText(this.chatTextList.get(position));       //리스트의 특정 position에 잇는걸 꺼내다 message에 넣음
        return message;
    }

}

