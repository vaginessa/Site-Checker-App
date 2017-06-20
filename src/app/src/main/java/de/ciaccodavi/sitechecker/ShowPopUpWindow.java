package de.ciaccodavi.sitechecker;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

public class ShowPopUpWindow extends Activity {


    private NavigationView navigationView;
    private Menu websitesMenu;
    private boolean initialized = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_layout);
        // button add url

    }

    public void init(NavigationView nv, Menu wm) {
        navigationView = nv;
        websitesMenu = wm;
        initialized = true;
    }

    public void addMenuVoice(int id, String url) {
        if (initialized) {
            websitesMenu.add(0, id, Menu.NONE, url);
            // menu.add(0, MENU_LOGIN, Menu.NONE, R.string.your-login-text).setIcon(R.drawable.your-login-icon);
            navigationView.invalidate();
        }
    }


}

