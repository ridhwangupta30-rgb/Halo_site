package app.halo.focus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shown when you open an app that is blocked during a focus session. */
public class BlockActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        String name = getIntent().getStringExtra("name");
        if (name == null) name = "This app";
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setBackgroundColor(Color.parseColor("#12141A"));
        int pad = (int) (28 * getResources().getDisplayMetrics().density);
        l.setPadding(pad, pad, pad, pad);

        TextView h = new TextView(this);
        h.setText("Focus first");
        h.setTextSize(28);
        h.setTextColor(Color.WHITE);
        h.setGravity(Gravity.CENTER);
        l.addView(h);

        TextView t = new TextView(this);
        t.setText(name + " is blocked while your Halo focus timer is running. Pause or finish the timer in Halo and it opens again.");
        t.setTextSize(16);
        t.setTextColor(Color.parseColor("#98A0B0"));
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, pad / 2, 0, pad);
        l.addView(t);

        Button go = new Button(this);
        go.setText("Back to Halo");
        go.setAllCaps(false);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BlockActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            }
        });
        l.addView(go, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(l);
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        setIntent(i);
        recreate();
    }
}
