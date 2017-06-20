package de.ciaccodavi.sitechecker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static int num_urls;
    public static NavigationView navigationView;
    public static Menu menu;
    public static Menu websitesMenu;
    public static TextView outputText;
    public static HTMLDownloader downloader;
    public static ProgressBar loadingBar;

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences sharedPreferencesData;
    public static SharedPreferences.Editor sharedPreferencesEditor;

    public static int checkingID = -1;

    public static int currentMenuItems;

    public static ConnectionChecker connectionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputForm();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mainView = this.navigationView;

        // aggiungere voci al menu
        menu = navigationView.getMenu();
        websitesMenu = menu.addSubMenu("Websites:");
        navigationView.invalidate();

        ShowPopUpWindow spuw = new ShowPopUpWindow();
        spuw.init(navigationView, websitesMenu);

        outputText = (TextView) findViewById(R.id.text00);
        downloader = new HTMLDownloader();
        Log.d("d", " added " + MainActivity.num_urls);

        loadingBar = (ProgressBar) findViewById(R.id.progressBar);
        loadingBar.setVisibility(View.INVISIBLE);


        sharedPreferences = this.getSharedPreferences("urls", Context.MODE_PRIVATE);
        sharedPreferencesData = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        // load websites
        downloader.loadSavedWebsites();

        int i = 0;
        for (String s : downloader.savedUrls) {
            addMenuVoice(i++, s);
            num_urls++;
        }
        currentMenuItems = i;

        // RESET
        Button buttonReset = (Button) findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkingID == -1) {
                    sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.clear();
                    sharedPreferencesEditor.apply();
                    sharedPreferencesEditor = sharedPreferencesData.edit();
                    sharedPreferencesEditor.clear();
                    sharedPreferencesEditor.apply();
                    Snackbar.make(view, "SAVEFILES DELETED!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {
                    if (HTMLDownloader.savedUrls.size() > checkingID) {
                        Snackbar.make(view, HTMLDownloader.savedUrls.get(checkingID) + " DELETED!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        HTMLDownloader.savedUrls.remove(checkingID);
                        HTMLDownloader.savedWebsitesData.remove(checkingID);
                        HTMLDownloader.saveWebsites();
                        resetMenuList();
                    }
                }
            }
        });

        MainActivity.connectionChecker = new ConnectionChecker(this);
        // CHECK
        Button buttonCheck = (Button) findViewById(R.id.button_check);
        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.connectionChecker.isNetworkAvailable()) {
                    if (checkingID != -1) {
                        if (HTMLDownloader.savedUrls.size() > checkingID) {
                            HTMLDownloader.check(checkingID);
                            Snackbar.make(view, "checking " + HTMLDownloader.savedUrls.toArray()[checkingID], Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }
                    } else {
                        if (HTMLDownloader.savedUrls.size() > 0) {
                            for (int i = 0; i < HTMLDownloader.savedUrls.size(); i++)
                                HTMLDownloader.check(i);
                        }
                    }
                    Log.d("CONNECTION !PROBLEM", "you should be connected");
                } else {
                    Log.d("CONNECTION PROBLEM", "u not connected bro");
                }
            }
        });
        // OPEN
        Button buttonOpen = (Button) findViewById(R.id.button_open);
        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkingID != -1 && HTMLDownloader.savedUrls.size() > checkingID && MainActivity.connectionChecker.isNetworkAvailable()) {
                    Uri uri = Uri.parse(HTMLDownloader.savedUrls.get(checkingID));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        DrawerLayout mDrawer;
        View drawerView = findViewById(R.id.drawer_layout);
        if (drawerView != null && drawerView instanceof DrawerLayout) {
            mDrawer = (DrawerLayout) drawerView;
            mDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View view, float v) {

                }

                @Override
                public void onDrawerOpened(View view) {

                }

                @Override
                public void onDrawerClosed(View view) {
                    // your refresh code can be called from here
                }

                @Override
                public void onDrawerStateChanged(int i) {
                    if (currentMenuItems != MainActivity.downloader.savedUrls.size())
                        resetMenuList();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        switch (item.getItemId()) {
            case R.id.action_0:
                cancelNotifications();
                return true;
            case R.id.action_1:
                cancelNotifications();
                scheduleNotification(getNotification("1 minute delay"), 60000);
                return true;
            case R.id.action_5:
                cancelNotifications();
                scheduleNotification(getNotification("5 minutes delay"), 300000);
                return true;
            case R.id.action_15:
                cancelNotifications();
                scheduleNotification(getNotification("15 minutes delay"), 900000);
                return true;
            case R.id.action_30:
                cancelNotifications();
                scheduleNotification(getNotification("30 minutes delay"), 1800000);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        // return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        TextView text = (TextView) findViewById(R.id.text00);
        Button buttonReset = (Button) findViewById(R.id.button_reset);
        Button buttonCheck = (Button) findViewById(R.id.button_check);
        if (id == R.id.nav_camera) {
            text.setText("Delete or check ALL");
            checkingID = -1;
            buttonReset.setText("DELETE ALL");
            buttonCheck.setText("CHECK ALL");
        } else {
            Log.d("accessing", "id: " + id);
            checkingID = id;
            text.setText("Website data: " + downloader.savedWebsitesData.toArray()[id]);
            buttonReset.setText("DELETE\n" + downloader.savedUrls.toArray()[id]);
            buttonCheck.setText("CHECK\n" + downloader.savedUrls.toArray()[id]);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public static View mainView;
    public static View popupView;
    public static PopupWindow pw;

    public void showInputForm() {

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.popup_layout, null);
        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.show();

        Button addButton = (Button) dialogView.findViewById(R.id.button_addSiteURL);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vviw) {
                EditText editText_url = (EditText) dialogView.findViewById(R.id.editText_newUrl);
                MainActivity.downloader.download(editText_url.getText().toString());
                alertDialog.cancel();
            }
        });
        // alert.show();

        // TODO: add website name, add website button, notification every 5 minutes
    }

    /*
        public static void registerAlarm(Context context) {
            Intent i = new Intent(context, BroadcastReceiver.class);

            //PendingIntent sender = PendingIntent.getBroadcast(context, 1337, i, 0);
            // We want the alarm to go off 3 seconds from now.
            long firstTime = SystemClock.elapsedRealtime();
            firstTime += 10 * 1000;//start 10 seconds after first register.

            // Schedule the alarm!
            AlarmManager am = (AlarmManager) context
                    .getSystemService(ALARM_SERVICE);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
                    300000, sender); // 5min interval
        }
    */
    public static void addMenuVoice(int id, String url) {
        websitesMenu.add(0, id, Menu.NONE, url);
        // menu.add(0, MENU_LOGIN, Menu.NONE, R.string.your-login-text).setIcon(R.drawable.your-login-icon);
        navigationView.invalidate();
        Log.d("a", " added " + MainActivity.num_urls);
    }

    public static void resetMenuList() {
        websitesMenu.clear();
        int i = 0;
        for (String s : downloader.savedUrls) {
            addMenuVoice(i++, s);
        }
    }


    public static Boolean siteHasChangedForNotification = false;
    public static int idThatHasChanged = 0;

    private void scheduleNotification(Notification notification, int delay) {
        if (HTMLDownloader.savedUrls.size() > 0) {
            for (int i = 0; i < HTMLDownloader.savedUrls.size(); i++)
                HTMLDownloader.check(i);
            notification = getNotification("SITE CHANGE DETECTED! \n " + downloader.savedUrls.toArray()[idThatHasChanged]);
        }
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, delay, pendingIntent); // 5min interval 300000

    }

    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Site Checker Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_menu_manage);
        return builder.build();
    }

    private void cancelNotifications() {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
