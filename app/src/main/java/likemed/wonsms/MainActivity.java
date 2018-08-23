package likemed.wonsms;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Context ctx;
    Handler handler;
    String id;
    TextView mTextView;

    class ConnectThread extends Thread {
        String contents;
        String no_card;
        Date receivedDate;

        public ConnectThread(String tcontents, String tno_card, Date treceivedDate) {
            contents = tcontents;
            no_card = tno_card;
            receivedDate = treceivedDate;
        }

        public void run() {
            try {
                final String output = request(contents, no_card, receivedDate);
                handler.post(new Runnable() {
                    public void run() {
                        String response;
                        int count = Integer.parseInt(output);
                        if (count == -1) {
                            response = "전송 실패";
                            try {
                                String register = no_card + "!@" + contents + "!@" + new SimpleDateFormat("yyyy-MM-dd").format(receivedDate) + "!@!@\n\n";
                                /*MainActivity.this.ctx;*/
                                FileOutputStream fosMemo = ctx.openFileOutput("sms_f.txt", Context.MODE_APPEND);
                                fosMemo.write(register.getBytes());
                                fosMemo.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        } else {
                            response = "전송 성공";
                        }

                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            /* Create or update. */
                            NotificationChannel channel = new NotificationChannel("1", "WonSMS", NotificationManager.IMPORTANCE_DEFAULT);
                            notificationManager.createNotificationChannel(channel);
                        }

                        Intent appcall = ctx.getPackageManager().getLaunchIntentForPackage("likemed.wonokok");
                        appcall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        appcall.putExtra("code", "sms");

                        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, appcall, PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, "1");
                        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.star_on));
                        builder.setSmallIcon(android.R.drawable.star_on);
                        builder.setTicker("WonSMS [" + response + "]");
                        builder.setContentTitle("WonSMS, 재전송");
                        builder.setContentText(contents + " [" + response + "]");
                        builder.setWhen(System.currentTimeMillis());
                        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        builder.setContentIntent(pendingIntent);
                        builder.setAutoCancel(true);
                        notificationManager.notify(0, builder.build());
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private String request(String contents, String no_card, Date receivedDate) {
            StringBuilder output = new StringBuilder();
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL("http://wonokok.dothome.co.kr/wonsms.php").openConnection();
                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    String urlParameters = new Uri.Builder()
                            .appendQueryParameter("id", id)
                            .appendQueryParameter("no", no_card)
                            .appendQueryParameter("date", new SimpleDateFormat("yyyy-MM-dd").format(receivedDate))
                            .appendQueryParameter("sms", contents)
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = getApplicationContext();
        handler = new Handler();
        mTextView = (TextView) findViewById(R.id.textView);
        if (!(ContextCompat.checkSelfPermission(this, "android.permission.RECEIVE_SMS") == 0 || ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.RECEIVE_SMS"))) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.RECEIVE_SMS"}, 1);
        }
        SharedPreferences settings = getSharedPreferences("settings", 0);
        id = settings.getString("id", "");
        String no_card1 = settings.getString("no_card1", "");
        Intent intent;
        if (id == null || id == "") {
            intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if (no_card1 == null || no_card1 == "") {
            intent = new Intent(getApplicationContext(), SettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        mTextView.setText(load());
    }

    protected void onRestart() {
        super.onRestart();
        mTextView.setText(load());
    }

    public void onsetbtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void ondelbtnClicked(View v) {
        getApplicationContext().deleteFile("sms_f.txt");
        mTextView.setText("삭제되었습니다.");
    }

    public void ongobtnClicked(View v) {
        try {
            FileInputStream fisMemo = getApplicationContext().openFileInput("sms_f.txt");
            byte[] memoData = new byte[fisMemo.available()];
            do {
            } while (fisMemo.read(memoData) != -1);
            String[] transfer = new String(memoData).replace("\n\n", "").split("!@!@");
            getApplicationContext().deleteFile("sms_f.txt");
            for (String split : transfer) {
                String[] unit_sms = split.split("!@");
                new ConnectThread(unit_sms[1], unit_sms[0], new SimpleDateFormat("yyyy-MM-dd").parse(unit_sms[2])).start();
            }
            mTextView.setText(load());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (ParseException e3) {
            e3.printStackTrace();
        }
    }

    public String load() {
        try {
            FileInputStream fisMemo = getApplicationContext().openFileInput("sms_f.txt");
            byte[] memoData = new byte[fisMemo.available()];
            do {
            } while (fisMemo.read(memoData) != -1);
            return new String(new String(memoData).replace("!@", " "));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e2) {
            e2.printStackTrace();
            return "";
        }
    }
}