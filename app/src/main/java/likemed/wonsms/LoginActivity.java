package likemed.wonsms;

import android.content.Intent;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    Handler handler;

    class ConnectThread extends Thread {
        String loginID;
        String loginPW;

        public ConnectThread(String tloginID, String tloginPW) {
            this.loginID = tloginID;
            this.loginPW = tloginPW;
        }

        public void run() {
            try {
                final String output = request(loginID, loginPW);
                handler.post(new Runnable() {
                    public void run() {
                        Editor editor = getSharedPreferences("settings", 0).edit();
                        if (output.equals("1")) {
                            Toast.makeText(LoginActivity.this, "로그인 성공 : " + loginID, Toast.LENGTH_LONG).show();
                            editor.putString("id", loginID);
                            editor.commit();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                            return;
                        }
                        Toast.makeText(LoginActivity.this, "@-------- 로그인 실패 --------@\n 아이디와 비밀번호를 확인해주십시오.", Toast.LENGTH_LONG).show();
                        editor.putString("id", "");
                        editor.commit();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private String request(String loginID, String loginPW) {
            StringBuilder output = new StringBuilder();
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL("http://wonokok.dothome.co.kr/wonsms_login.php").openConnection();
                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    String urlParameters = new Builder()
                            .appendQueryParameter("code", "iloveyou")
                            .appendQueryParameter("id", loginID)
                            .appendQueryParameter("pw", loginPW)
                            .build()
                            .getEncodedQuery();
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
                            output.append(line);
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
        handler = new Handler();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText etID = (EditText) findViewById(R.id.etID);
        final EditText etPW = (EditText) findViewById(R.id.etPW);
        ((Button) findViewById(R.id.button)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                new ConnectThread(etID.getText().toString().trim(), etPW.getText().toString().trim()).start();
            }
        });
    }
}