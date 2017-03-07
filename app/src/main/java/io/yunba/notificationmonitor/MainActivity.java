package io.yunba.notificationmonitor;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import io.yunba.notificationmonitor.service.NotificationMonitor;
import io.yunba.notificationmonitor.utils.CommonUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static final int MSG_LIST_NOTIFICATIONS = 0x10;
    private LinearLayout console;
    private ScrollView scrollView;
    private Button listNotificationBtn;
    private Button enableBtn;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private boolean isEnabledNLS = false;
    private NotificationReceiver receiver = null;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Object obj = msg.obj;
            int color = CommonUtils.getColorFromLogLevel(MainActivity.this, msg.arg1);
            switch (msg.what) {
                case MSG_LIST_NOTIFICATIONS:
                    TextView textView = new TextView(MainActivity.this);
                    textView.setTextColor(color);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                            (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    textView.setLayoutParams(params);
                    Typeface font = Typeface.createFromAsset(getAssets(), "fonts/consolab.ttf");
                    textView.setTypeface(font);
                    textView.setTextSize(14);
                    textView.setText((String) obj);
                    console.addView(textView);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        receiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("io.yunba.notificationmonitor.NOTIFICATION_EVENT");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isEnabledNLS = isEnabled();
        if (!isEnabledNLS) {
            showConfirmDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void initView() {
        console = (LinearLayout) findViewById(R.id.screen);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        listNotificationBtn = (Button) findViewById(R.id.btn_list);
        enableBtn = (Button) findViewById(R.id.btn_enable);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_enable:
                openNotificationAccess();
                break;
            case R.id.btn_list:
                getCurrentNotificationDetails();
                break;
        }
    }

    private String getCurrentNotificationDetails() {
        String notificationDetails = "";
        StatusBarNotification[] currentNos = NotificationMonitor.getCurrentNotifications();
        if (currentNos != null) {
            for (int i = 0; i < currentNos.length; i++) {
                notificationDetails = notificationDetails +  "\n" + "[notification "  + i + "]"
                        + currentNos[i].getPackageName();
            }
        }
        Message message = Message.obtain();
        message.what = MSG_LIST_NOTIFICATIONS;
        message.obj = notificationDetails;
        message.arg1 = ILogger.info;
        handler.sendMessage(message);
        return notificationDetails;
    }

    private boolean isEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Please enable NotificationMonitor access")
                .setTitle("Notification Access")
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                openNotificationAccess();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // do nothing
                            }
                        })
                .create().show();
    }

    private void openNotificationAccess() {
        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int event = intent.getIntExtra(NotificationMonitor.PAR_EVENT, -1);
            switch (event) {
                case NotificationMonitor.EVENT_POST:
                    String pkgName = intent.getStringExtra(NotificationMonitor.PAR_EVENT_EXTRA);
                    String title = intent.getStringExtra(NotificationMonitor.PAR_NOTIFICATION_TITLE);
                    String notice = "[notification post]" + pkgName + "[" + title + "]";
                    Message message = Message.obtain();
                    message.what = MSG_LIST_NOTIFICATIONS;
                    message.obj = notice;
                    message.arg1 = ILogger.success;
                    handler.sendMessage(message);
                    break;
                case NotificationMonitor.EVENT_REMOVE:
                    break;
            }
        }
    }
}
