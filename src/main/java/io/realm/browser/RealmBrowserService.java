package io.realm.browser;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;

import io.realm.browser.R.color;
import io.realm.browser.R.string;

public class RealmBrowserService extends Service {
    private static final int NOTIFICATION_ID = 9696;

    public RealmBrowserService() {
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, RealmBrowserService.class);
        context.startService(intent);
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        String appName = this.getApplicationInfo().loadLabel(this.getPackageManager()).toString();
        Intent notifyIntent = new Intent(this, BrowserActivity.class);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = (new Builder(this)).setSmallIcon(R.drawable.realm_browser_notification_icon).setColor(this.getResources().getColor(color.realm_browser_notification_color)).setContentTitle(TextUtils.isEmpty(appName)?this.getString(string.realm_browser_notification_title):appName).setContentText(this.getString(string.realm_browser_notification_text)).setAutoCancel(false).setLocalOnly(true).setContentIntent(notifyPendingIntent).build();
        this.startForeground(9696, notification);
        return Service.START_STICKY;
    }
}
