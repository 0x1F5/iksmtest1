package com.example.tsaka.iksmtest;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.riversun.okhttp3.OkHttp3CookieHelper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewRes;
    private TextView textViewDes;
    private Button buttonGetTest;
    private String res = "";
    private String des = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textViewRes = (TextView) findViewById(R.id.tv_res);
        textViewDes = (TextView) findViewById(R.id.tv_des);
        buttonGetTest = (Button) findViewById(R.id.btn_get_test);
        buttonGetTest.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_test:
                getTest();
                break;
            default:
                break;
        }
    }

    private void getTest() {
        EditText edit = (EditText)findViewById(R.id.editText);
        SpannableStringBuilder sp = (SpannableStringBuilder)edit.getText();
        String iksm_session1 = sp.toString();

        EditText edit2 = (EditText)findViewById(R.id.editText2);
        SpannableStringBuilder sp2 = (SpannableStringBuilder)edit2.getText();
        String battle_number = sp2.toString();

        String url = "https://app.splatoon2.nintendo.net/api/results/"+battle_number;
        OkHttp3CookieHelper cookieHelper = new OkHttp3CookieHelper();
        cookieHelper.setCookie(url, "iksm_session",iksm_session1);
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieHelper.cookieJar())
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //failMessage();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                res = response.body().string();
                try {
                    StringBuffer stringBuffer1 = new StringBuffer();
                    StringBuffer stringBuffer2 = new StringBuffer();
                    JSONObject resJson = new JSONObject(res);
                    //試合システム(ガチorレギュラー)読み込み
                    JSONObject game_mode = resJson.getJSONObject("game_mode");
                    String matchname = game_mode.getString("name");
                    String matchtype = game_mode.getString("key");
                    //試合ルール(エリア、ホコ、ヤグラ)読み込み
                    JSONObject rule = resJson.getJSONObject("rule");
                    String rulename = rule.getString("name");
                    //ステージ読み込み
                    JSONObject stage = resJson.getJSONObject("stage");
                    String stagename = stage.getString("name");
                    //勝ち負け読み込み
                    JSONObject my_team_result = resJson.getJSONObject("my_team_result");
                    String my_team_result_key = my_team_result.getString("key");
                    //自分のチームのプレイヤーの名前を取る Arrayからの型変換する
                    JSONArray my_team_membersArray = resJson.getJSONArray("my_team_members");
                    int counta = my_team_membersArray.length();
                    JSONObject[] my_team_members = new JSONObject[counta];
                    for (int i = 0; i < counta; i++) {
                        my_team_members[i] = my_team_membersArray.getJSONObject(i);
                    }
                    for (int i = 0; i < my_team_members.length; i++) {
                        String playerm_killcount = my_team_members[i].getString("kill_count");
                        String playerm_akillcount = my_team_members[i].getString("assist_count");
                        String playerm_deathcount = my_team_members[i].getString("death_count");
                        JSONObject my_team_members_player = my_team_members[i].getJSONObject("player");
                        String playerm_nickname = my_team_members_player.getString("nickname");
                        stringBuffer1.append(playerm_nickname+" "+playerm_killcount+" "+playerm_akillcount + " "+playerm_deathcount+ "\n");
                    }
                    //相手のチームのプレイヤーの名前を取る Arrayからの型変換する
                    JSONArray other_team_membersArray = resJson.getJSONArray("other_team_members");
                    int countb = other_team_membersArray.length();
                    JSONObject[] other_team_members = new JSONObject[countb];
                    for (int i = 0; i < countb; i++) {
                        other_team_members[i] = other_team_membersArray.getJSONObject(i);
                    }
                    for (int i = 0; i < other_team_members.length; i++) {
                        String playero_killcount = other_team_members[i].getString("kill_count");
                        String playero_akillcount = other_team_members[i].getString("assist_count");
                        String playero_deathcount = other_team_members[i].getString("death_count");
                        JSONObject other_team_members_player = other_team_members[i].getJSONObject("player");
                        String playero_nickname = other_team_members_player.getString("nickname");
                        stringBuffer2.append(playero_nickname+" "+playero_killcount+" "+playero_akillcount+" "+playero_deathcount + "\n");
                    }
                    //自分の名前を取る
                    JSONObject player_result = resJson.getJSONObject("player_result");
                    JSONObject player_result_player = player_result.getJSONObject("player");
                    String player_result_player_nickname = player_result_player.getString("nickname");
                    //敵味方のカウント数をintで取りStringに変換
                    int mycounti = resJson.getInt("my_team_count");
                    String mycount = String.valueOf(mycounti);
                    int otcounti = resJson.getInt("other_team_count");
                    String otcount = String.valueOf(otcounti);
                    des = mycount + "vs" + otcount + "\r\n"
                            + matchname + "\r\n"
                            + rulename + "\r\n"
                            + my_team_result_key + "\r\n"
                            + "味方チームの名前\r\n" + player_result_player_nickname + "\r\n" + stringBuffer1
                            + "敵チームの名前\r\n" + stringBuffer2;

                    //UI
                    runOnUiThread(new Runnable() {
                        public void run() {
                            textViewDes.setText(des);

                        }
                    });
                } catch (JSONException e) {
                    //failMessage();
                    e.printStackTrace();
                }
            }
        });
    }


}
