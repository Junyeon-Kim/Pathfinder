package com.pathfinder.kjy.appcopy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PopupActivity extends Activity {
    ListView choose_bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity);

        //UI 객체생성
        choose_bus = (ListView)findViewById(R.id.choose_bus);

        //데이터 가져오기
        Intent intent = getIntent();
        String[] data = intent.getStringArrayExtra("data");
        String loc_position = intent.getStringExtra("loc_position");
        String loc_latitude = intent.getStringExtra("loc_latitude");
        String loc_longitude = intent.getStringExtra("loc_longitude");
        String tag = intent.getStringExtra("Tag");
        String marker_name = intent.getStringExtra("marker_name");

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, data){
            @Override

            public View getView(int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                TextView test = (TextView) view.findViewById(android.R.id.text1);
                test.setTextColor(Color.BLACK);
                return view;
            }
        };

        ListView listview = (ListView) findViewById(R.id.choose_bus) ;
        listview.setAdapter(adapter) ;

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get TextView's Text.
                String choosed_bus = (String) parent.getItemAtPosition(position) ;

                Intent intent3 = new Intent();
                intent3.putExtra("choosed_bus", choosed_bus);
                intent3.putExtra("loc_position", loc_position);
                intent3.putExtra("loc_latitude", loc_latitude);
                intent3.putExtra("loc_longitude", loc_longitude);
                intent3.putExtra("Tag", tag);
                intent3.putExtra("marker_name", marker_name);
                setResult(RESULT_OK, intent3);

                finish();
            }
        }) ;
    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        //액티비티(팝업) 닫기
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}
