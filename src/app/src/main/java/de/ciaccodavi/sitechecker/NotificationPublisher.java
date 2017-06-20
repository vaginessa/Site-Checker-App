package de.ciaccodavi.sitechecker;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
if(MainActivity.connectionChecker.isNetworkAvailable()) {
    if (HTMLDownloader.savedUrls.size() > 0) {
        for (int i = 0; i < HTMLDownloader.savedUrls.size(); i++)
            HTMLDownloader.check(i);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Site Checker Notification");
        builder.setContentText("SITE CHANGE DETECTED for " + HTMLDownloader.savedUrls.toArray()[MainActivity.idThatHasChanged]);
        builder.setSmallIcon(R.drawable.ic_menu_manage);
        notification = builder.build();
    }


        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        Log.d("NOTIFICATION TEST"," has changed = "+MainActivity.siteHasChangedForNotification+" #"+MainActivity.idThatHasChanged);
        if (MainActivity.siteHasChangedForNotification) {
            notificationManager.notify(id, notification);
        MainActivity.siteHasChangedForNotification=false;
        }
}
    }
}