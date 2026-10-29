package app.halo.focus;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

/** Exposed to the Halo page as window.HaloNative. */
final class HaloBridge {
    private final Context ctx;

    HaloBridge(Context c) {
        ctx = c;
    }

    /** The focus timer on this device is running (on) or stopped; guardOn is the Focus guard switch. */
    @JavascriptInterface
    public void focus(boolean on, boolean guardOn) {
        Core.setLocal(ctx, on, guardOn);
    }

    /** Sign-in details so this app can read the focus session of your other devices. */
    @JavascriptInterface
    public void setAuth(String json) {
        if (json != null && json.length() < 20000) Core.saveAuth(ctx, json);
    }

    @JavascriptInterface
    public void openSetup() {
        Intent i = new Intent(ctx, SetupActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    @JavascriptInterface
    public String status() {
        try {
            JSONObject o = new JSONObject();
            o.put("access", BlockService.isEnabled(ctx));
            o.put("apps", Core.blocked(ctx).size());
            o.put("blocking", Core.blocking(ctx));
            o.put("v", 1);
            return o.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
}
