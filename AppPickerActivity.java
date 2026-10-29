package app.halo.focus;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Pick the apps to block on this device. */
public class AppPickerActivity extends Activity {
    private final Set<String> chosen = new HashSet<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        chosen.addAll(Core.blocked(this));
        final PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = pm.queryIntentActivities(main, 0);
        Set<String> never = new HashSet<>();
        never.add(getPackageName());
        never.add("com.android.settings");
        never.add("com.android.systemui");
        never.add("com.android.dialer");
        never.add("com.google.android.dialer");
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        for (ResolveInfo r : pm.queryIntentActivities(home, 0)) never.add(r.activityInfo.packageName);

        final List<String[]> apps = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ResolveInfo r : list) {
            String pkg = r.activityInfo.packageName;
            if (never.contains(pkg) || !seen.add(pkg)) continue;
            apps.add(new String[]{String.valueOf(r.loadLabel(pm)), pkg});
        }
        Collections.sort(apps, new Comparator<String[]>() {
            @Override
            public int compare(String[] a, String[] c) {
                return a[0].compareToIgnoreCase(c[0]);
            }
        });

        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(pad, pad, pad, pad);
        TextView head = new TextView(this);
        head.setText("Apps to block while the focus timer runs");
        head.setTextSize(20);
        head.setPadding(0, 0, 0, pad);
        col.addView(head);
        for (final String[] a : apps) {
            CheckBox cb = new CheckBox(this);
            cb.setText(a[0]);
            cb.setTextSize(16);
            cb.setChecked(chosen.contains(a[1]));
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean on) {
                    if (on) chosen.add(a[1]);
                    else chosen.remove(a[1]);
                    Core.setBlocked(AppPickerActivity.this, chosen);
                }
            });
            col.addView(cb, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        ScrollView sv = new ScrollView(this);
        sv.addView(col);
        setContentView(sv);
    }
}
