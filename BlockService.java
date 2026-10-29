package app.halo.focus;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Watches which app comes to the front. While a Halo focus session runs (here or on another of your devices),
 * apps you chose are sent back to the home screen. Also syncs the session through your Halo cloud account.
 */
public class BlockService extends AccessibilityService {
    private volatile boolean running;
    private Thread poller;
    private long lastBlock;
    private long lastWrite;
    private boolean wrote;
    private long seenN = -1;
    private long seenAt;
    private final String dev = "android" + Long.toString(System.nanoTime() % 1000000, 36);

    static boolean isEnabled(Context c) {
        try {
            String s = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return s != null && s.contains(c.getPackageName() + "/" + BlockService.class.getName());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onServiceConnected() {
        running = true;
        poller = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        sync();
                    } catch (Exception ignored) {
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        poller.start();
    }

    @Override
    public void onDestroy() {
        running = false;
        if (poller != null) poller.interrupt();
        super.onDestroy();
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e == null || e.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence cs = e.getPackageName();
        if (cs == null) return;
        String pkg = cs.toString();
        if (pkg.equals(getPackageName())) return;
        if (!Core.blocking(this)) return;
        Set<String> blocked = Core.blocked(this);
        if (!blocked.contains(pkg)) return;
        long now = System.currentTimeMillis();
        if (now - lastBlock < 700) return;
        lastBlock = now;
        performGlobalAction(GLOBAL_ACTION_HOME);
        String name = pkg;
        try {
            PackageManager pm = getPackageManager();
            name = String.valueOf(pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)));
        } catch (Exception ignored) {
        }
        Intent i = new Intent(this, BlockActivity.class);
        i.putExtra("name", name);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        try {
            startActivity(i);
        } catch (Exception ignored) {
        }
    }

    /* ---- cloud sync of the focus session (same 'focus' record the Halo site uses) ---- */

    private void sync() throws Exception {
        String raw = Core.auth(this);
        if (raw == null) {
            Core.remote = false;
            return;
        }
        JSONObject a = new JSONObject(raw);
        if (a.optLong("expires_at", 0) * 1000 < System.currentTimeMillis()) {
            Core.remote = false;
            return;
        }
        boolean guard = a.optBoolean("guard", false);
        boolean local = Core.local(this);
        long now = System.currentTimeMillis();
        if (local) {
            Core.remote = false;
            if (now - lastWrite > 15000) {
                lastWrite = now;
                wrote = true;
                put(a, true);
            }
        } else {
            if (wrote) {
                wrote = false;
                put(a, false);
            }
            if (guard) read(a);
            else Core.remote = false;
        }
    }

    private HttpURLConnection open(JSONObject a, String path, String method) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(a.getString("url") + path).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        c.setRequestMethod(method);
        c.setRequestProperty("apikey", a.getString("key"));
        c.setRequestProperty("Authorization", "Bearer " + a.getString("access_token"));
        c.setRequestProperty("Content-Type", "application/json");
        return c;
    }

    private void put(JSONObject a, boolean on) throws Exception {
        HttpURLConnection c = open(a, "/rest/v1/jh_docs?on_conflict=uid,id", "POST");
        c.setRequestProperty("Prefer", "resolution=merge-duplicates,return=minimal");
        c.setDoOutput(true);
        JSONObject data = new JSONObject();
        data.put("on", on);
        data.put("n", System.currentTimeMillis());
        data.put("dev", dev);
        data.put("sites", new JSONArray());
        JSONObject row = new JSONObject();
        row.put("uid", a.getString("uid"));
        row.put("id", "focus");
        row.put("data", data);
        byte[] body = new JSONArray().put(row).toString().getBytes(StandardCharsets.UTF_8);
        OutputStream os = c.getOutputStream();
        os.write(body);
        os.close();
        c.getResponseCode();
        c.disconnect();
    }

    private void read(JSONObject a) throws Exception {
        HttpURLConnection c = open(a, "/rest/v1/jh_docs?select=data&id=eq.focus", "GET");
        int code = c.getResponseCode();
        if (code != 200) {
            Core.remote = false;
            c.disconnect();
            return;
        }
        InputStream is = c.getInputStream();
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) > 0) bo.write(buf, 0, n);
        is.close();
        c.disconnect();
        JSONArray arr = new JSONArray(bo.toString("UTF-8"));
        boolean r = false;
        if (arr.length() > 0) {
            JSONObject d = arr.getJSONObject(0).getJSONObject("data");
            String from = d.optString("dev", "");
            if (!from.equals(dev) && !from.equals(a.optString("dev", ""))) {
                long dn = d.optLong("n", 0);
                if (dn != seenN) {
                    boolean first = seenN == -1;
                    seenN = dn;
                    seenAt = first ? 0 : System.currentTimeMillis();
                }
                r = d.optBoolean("on", false) && seenAt > 0 && System.currentTimeMillis() - seenAt < 45000;
            }
        }
        Core.remote = r;
    }
}
