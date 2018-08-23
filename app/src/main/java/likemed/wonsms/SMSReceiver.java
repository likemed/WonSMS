package likemed.wonsms;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SMSReceiver extends BroadcastReceiver {
    private Context ctx;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Handler handler;
    String id;
    String no_card1;
    String no_card2;
    String no_card3;
    String no_card4;
    String no_card5;
    String tel_card1;
    String tel_card2;
    String tel_card3;
    String tel_card4;
    String tel_card5;
    String txt_card1;
    String txt_card2;
    String txt_card3;
    String txt_card4;
    String txt_card5;

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
                                String register = no_card + "!@" + contents + "!@" + format.format(receivedDate) + "!@!@\n\n";
                                /* SMSReceiver.this.ctx; */
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

                        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
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
                        builder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), android.R.drawable.star_on));
                        builder.setSmallIcon(android.R.drawable.star_on);
                        builder.setTicker("WonSMS [" + response + "]");
                        builder.setContentTitle("WonSMS");
                        builder.setContentText(contents + " [" + response + "]");
                        builder.setWhen(System.currentTimeMillis());
                        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        builder.setContentIntent(pendingIntent);
                        builder.setAutoCancel(true);
                        /*builder.setNumber(count);*/
                        notificationManager.notify(0, builder.build());

                        Intent intent_badge = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
                        intent_badge.putExtra("badge_count_package_name", "likemed.wonokok");
                        intent_badge.putExtra("badge_count_class_name", "likemed.wonokok.MainActivity");
                        intent_badge.putExtra("badge_count", count);
                        SMSReceiver.this.ctx.sendBroadcast(intent_badge);
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
                            .appendQueryParameter("id", SMSReceiver.this.id)
                            .appendQueryParameter("no", no_card)
                            .appendQueryParameter("date", SMSReceiver.this.format.format(receivedDate))
                            .appendQueryParameter("sms", contents).build().getEncodedQuery();
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

    public void onReceive(Context context, Intent intent) {
        ctx = context;
        handler = new Handler();
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        id = settings.getString("id", "");
        no_card1 = settings.getString("no_card1", "");
        if (!no_card1.isEmpty()) {
            tel_card1 = settings.getString("tel_card1", "");
            txt_card1 = settings.getString("txt_card1", "");
        }
        no_card2 = settings.getString("no_card2", "");
        if (!no_card2.isEmpty()) {
            tel_card2 = settings.getString("tel_card2", "");
            txt_card2 = settings.getString("txt_card2", "");
        }
        no_card3 = settings.getString("no_card3", "");
        if (!no_card3.isEmpty()) {
            tel_card3 = settings.getString("tel_card3", "");
            txt_card3 = settings.getString("txt_card3", "");
        }
        no_card4 = settings.getString("no_card4", "");
        if (!no_card4.isEmpty()) {
            tel_card4 = settings.getString("tel_card4", "");
            txt_card4 = settings.getString("txt_card4", "");
        }
        no_card5 = settings.getString("no_card5", "");
        if (!no_card5.isEmpty()) {
            tel_card5 = settings.getString("tel_card5", "");
            txt_card5 = settings.getString("txt_card5", "");
        }
        SmsMessage[] messages = parseSmsMessage(intent.getExtras());
        if (messages != null && messages.length > 0) {
            String sender = messages[0].getOriginatingAddress();
            String contents = messages[0].getMessageBody().toString();
            Date receivedDate = new Date(messages[0].getTimestampMillis());
            String no_card = null;
            String tel_card = null;
            String txt_card = null;
            for (int i = 1; i <= 5; i++) {
                switch (i) {
                    case 1:
                        no_card = no_card1;
                        tel_card = tel_card1;
                        txt_card = txt_card1;
                        break;
                    case 2:
                        no_card = no_card2;
                        tel_card = tel_card2;
                        txt_card = txt_card2;
                        break;
                    case 3:
                        no_card = no_card3;
                        tel_card = tel_card3;
                        txt_card = txt_card3;
                        break;
                    case 4:
                        no_card = no_card4;
                        tel_card = tel_card4;
                        txt_card = txt_card4;
                        break;
                    case 5:
                        no_card = no_card5;
                        tel_card = tel_card5;
                        txt_card = txt_card5;
                        break;
                }
                if (!no_card.isEmpty() && sender.contains(tel_card) && contents.contains(txt_card)) {
                    new ConnectThread(contents, no_card, receivedDate).start();
                    return;
                }
            }
        }
    }

    private SmsMessage[] parseSmsMessage(Bundle bundle) {
        Object[] objs = (Object[]) bundle.get("pdus");
        SmsMessage[] messages = new SmsMessage[objs.length];
        int smsCount = objs.length;
        for (int i = 0; i < smsCount; i++) {
            if (VERSION.SDK_INT >= 23) {
                messages[i] = SmsMessage.createFromPdu((byte[]) objs[i], bundle.getString("format"));
            } else {
                messages[i] = SmsMessage.createFromPdu((byte[]) objs[i]);
            }
        }
        return messages;
    }
}