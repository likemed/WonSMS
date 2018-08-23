package likemed.wonsms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingActivity extends AppCompatActivity {
    TextView cardlist;
    Handler handler = new Handler();

    class ConnectThread extends Thread {
        String loginID;

        public ConnectThread(String tloginID) {
            loginID = tloginID;
        }

        public void run() {
            try {
                final String output = request(loginID);
                handler.post(new Runnable() {
                    public void run() {
                        if (output.isEmpty()) {
                            Toast.makeText(SettingActivity.this, "WonOkOk에 카드 정보가 없습니다.", 1).show();
                            cardlist.setText("WonOkOk에 카드 정보가 없습니다.");
                            return;
                        }
                        cardlist.setText(output.trim());
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private String request(String loginID) {
            StringBuilder output = new StringBuilder();
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL("http://wonokok.dothome.co.kr/wonsms_card.php").openConnection();
                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    String urlParameters = new Builder().appendQueryParameter("code", "iloveyou").appendQueryParameter("id", loginID).build().getEncodedQuery();
                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();
                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while (true) {
                            String line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            output.append(line + "\n");
                        }
                        reader.close();
                    }
                    conn.disconnect();
                }
            } catch (Exception ex) {
                Log.e("WonSMS", "Exception in processing response.", ex);
                ex.printStackTrace();
            }
            return output.toString();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        final EditText etno_card1 = (EditText) findViewById(R.id.no_card1);
        final EditText ettel_card1 = (EditText) findViewById(R.id.tel_card1);
        final EditText ettxt_card1 = (EditText) findViewById(R.id.txt_card1);
        final EditText etno_card2 = (EditText) findViewById(R.id.no_card2);
        final EditText ettel_card2 = (EditText) findViewById(R.id.tel_card2);
        final EditText ettxt_card2 = (EditText) findViewById(R.id.txt_card2);
        final EditText etno_card3 = (EditText) findViewById(R.id.no_card3);
        final EditText ettel_card3 = (EditText) findViewById(R.id.tel_card3);
        final EditText ettxt_card3 = (EditText) findViewById(R.id.txt_card3);
        final EditText etno_card4 = (EditText) findViewById(R.id.no_card4);
        final EditText ettel_card4 = (EditText) findViewById(R.id.tel_card4);
        final EditText ettxt_card4 = (EditText) findViewById(R.id.txt_card4);
        final EditText etno_card5 = (EditText) findViewById(R.id.no_card5);
        final EditText ettel_card5 = (EditText) findViewById(R.id.tel_card5);
        final EditText ettxt_card5 = (EditText) findViewById(R.id.txt_card5);
        cardlist = (TextView) findViewById(R.id.cardlist);
        SharedPreferences settings = getSharedPreferences("settings", 0);
        final String id = settings.getString("id", "");
        String no_card1 = settings.getString("no_card1", "");
        if (!no_card1.isEmpty()) {
            String tel_card1 = settings.getString("tel_card1", "");
            String txt_card1 = settings.getString("txt_card1", "");
            etno_card1.setText(no_card1);
            ettel_card1.setText(tel_card1);
            ettxt_card1.setText(txt_card1);
        }
        String no_card2 = settings.getString("no_card2", "");
        if (!no_card2.isEmpty()) {
            String tel_card2 = settings.getString("tel_card2", "");
            String txt_card2 = settings.getString("txt_card2", "");
            etno_card2.setText(no_card2);
            ettel_card2.setText(tel_card2);
            ettxt_card2.setText(txt_card2);
        }
        String no_card3 = settings.getString("no_card3", "");
        if (!no_card3.isEmpty()) {
            String tel_card3 = settings.getString("tel_card3", "");
            String txt_card3 = settings.getString("txt_card3", "");
            etno_card3.setText(no_card3);
            ettel_card3.setText(tel_card3);
            ettxt_card3.setText(txt_card3);
        }
        String no_card4 = settings.getString("no_card4", "");
        if (!no_card1.isEmpty()) {
            String tel_card4 = settings.getString("tel_card4", "");
            String txt_card4 = settings.getString("txt_card4", "");
            etno_card4.setText(no_card4);
            ettel_card4.setText(tel_card4);
            ettxt_card4.setText(txt_card4);
        }
        String no_card5 = settings.getString("no_card5", "");
        if (!no_card1.isEmpty()) {
            String tel_card5 = settings.getString("tel_card5", "");
            String txt_card5 = settings.getString("txt_card5", "");
            etno_card5.setText(no_card5);
            ettel_card5.setText(tel_card5);
            ettxt_card5.setText(txt_card5);
        }
        new ConnectThread(id).start();
        ((Button) findViewById(R.id.button)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Editor editor = getSharedPreferences("settings", 0).edit();
                String no_card1 = etno_card1.getText().toString().trim();
                String tel_card1 = ettel_card1.getText().toString().trim();
                String txt_card1 = ettxt_card1.getText().toString().trim();
                editor.putString("no_card1", no_card1);
                editor.putString("tel_card1", tel_card1);
                editor.putString("txt_card1", txt_card1);
                String no_card2 = etno_card2.getText().toString().trim();
                String tel_card2 = ettel_card2.getText().toString().trim();
                String txt_card2 = ettxt_card2.getText().toString().trim();
                editor.putString("no_card2", no_card2);
                editor.putString("tel_card2", tel_card2);
                editor.putString("txt_card2", txt_card2);
                String no_card3 = etno_card3.getText().toString().trim();
                String tel_card3 = ettel_card3.getText().toString().trim();
                String txt_card3 = ettxt_card3.getText().toString().trim();
                editor.putString("no_card3", no_card3);
                editor.putString("tel_card3", tel_card3);
                editor.putString("txt_card3", txt_card3);
                String no_card4 = etno_card4.getText().toString().trim();
                String tel_card4 = ettel_card4.getText().toString().trim();
                String txt_card4 = ettxt_card4.getText().toString().trim();
                editor.putString("no_card4", no_card4);
                editor.putString("tel_card4", tel_card4);
                editor.putString("txt_card4", txt_card4);
                String no_card5 = etno_card5.getText().toString().trim();
                String tel_card5 = ettel_card5.getText().toString().trim();
                String txt_card5 = ettxt_card5.getText().toString().trim();
                editor.putString("no_card5", no_card5);
                editor.putString("tel_card5", tel_card5);
                editor.putString("txt_card5", txt_card5);
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Intent sendintent = getPackageManager().getLaunchIntentForPackage("likemed.wonokok");
                sendintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                sendintent.putExtra("id", id);
                startActivity(intent);
                startActivity(sendintent);
                finish();
            }
        });

        ((Button) findViewById(R.id.resetbtn)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Editor editor = getSharedPreferences("settings", 0).edit();
                editor.clear();
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Intent sendintent = getPackageManager().getLaunchIntentForPackage("likemed.wonokok");
                sendintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                sendintent.putExtra("id", "@reset");
                startActivity(intent);
                startActivity(sendintent);
                finish();
            }
        });
    }
}