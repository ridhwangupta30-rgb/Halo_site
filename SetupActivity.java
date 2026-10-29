package app.halo.focus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Status and the three things needed for app blocking: the permission, the app list, and a pause switch. */
public class SetupActivity extends Activity {
    private TextView status;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        int pad = (int) (18 * getResources().getDisplayMetrics().density);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(pad, pad, pad, pad);

        TextView h = new TextView(this);
        h.setText("App blocking");
        h.setTextSize(24);
        col.addView(h);

        status = new TextView(this);
        status.setTextSize(15);
        status.setPadding(0, pad / 2, 0, pad);
        col.addView(status);

        col.addView(button("1. Turn on the Halo Focus permission", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        }));
        TextView n1 = new TextView(this);
        n1.setText("In the list, open Halo Focus (it may be under Installed apps) and switch it on. Android shows a warning because this permission can read the screen; Halo Focus only checks which app is in front and never reads what is inside it.");
        n1.setTextSize(13);
        n1.setTextColor(Color.GRAY);
        n1.setPadding(0, 0, 0, pad);
        col.addView(n1);

        col.addView(button("2. Choose apps to block", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SetupActivity.this, AppPickerActivity.class));
            }
        }));
        col.addView(space(pad));

        col.addView(button("Pause blocking for 15 minutes", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Core.pauseFor(SetupActivity.this, 15 * 60 * 1000L);
                refresh();
            }
        }));
        col.addView(button("Resume blocking now", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Core.clearPause(SetupActivity.this);
                refresh();
            }
        }));

        TextView n2 = new TextView(this);
        n2.setText("\nBlocking only happens while a Halo focus session is running on this phone or on another device you are signed in on, and only if \"Guard my focus sessions\" is on in Halo's Settings. If Android turns the permission off after an update or a restart, switch it on again in step 1.");
        n2.setTextSize(13);
        n2.setTextColor(Color.GRAY);
        col.addView(n2);
        setContentView(col);
    }

    private Button button(String t, View.OnClickListener l) {
        Button x = new Button(this);
        x.setText(t);
        x.setAllCaps(false);
        x.setOnClickListener(l);
        return x;
    }

    private View space(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h));
        return v;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        boolean on = BlockService.isEnabled(this);
        int n = Core.blocked(this).size();
        String s = "Permission: " + (on ? "on" : "OFF (do step 1)") + "\nApps chosen: " + n
                + "\nRight now: " + (Core.paused(this) ? "paused" : Core.blocking(this) ? "blocking" : "waiting for a focus session");
        status.setText(s);
    }
}
