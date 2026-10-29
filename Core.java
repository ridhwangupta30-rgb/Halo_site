package app.halo.focus;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/** Shared state: which apps are blocked, whether a Halo focus session is running, and the saved sign-in. */
final class Core {
    static final String SITE = "https://jeeadvhalo.netlify.app/";
    private static final long LOCAL_MAX_MS = 8L * 3600 * 1000;

    /** True while another device of yours is running a focus session (set by BlockService). */
    static volatile boolean remote = false;

    private Core() {}

    static SharedPreferences p(Context c) {
        return c.getSharedPreferences("halo", Context.MODE_PRIVATE);
    }

    static Set<String> blocked(Context c) {
        return new HashSet<>(p(c).getStringSet("blocked", new HashSet<String>()));
    }

    static void setBlocked(Context c, Set<String> s) {
        p(c).edit().putStringSet("blocked", new HashSet<>(s)).apply();
    }

    /** Called from the Halo page: the timer on this phone is running (on) or not. */
    static void setLocal(Context c, boolean on, boolean guardOn) {
        SharedPreferences sp = p(c);
        boolean was = sp.getBoolean("local", false);
        SharedPreferences.Editor e = sp.edit();
        e.putBoolean("local", on);
        e.putBoolean("guard", guardOn);
        if (on && !was) e.putLong("localAt", System.currentTimeMillis());
        e.apply();
    }

    static boolean local(Context c) {
        SharedPreferences sp = p(c);
        return sp.getBoolean("local", false) && sp.getBoolean("guard", false)
                && System.currentTimeMillis() - sp.getLong("localAt", 0) < LOCAL_MAX_MS;
    }

    static boolean paused(Context c) {
        return System.currentTimeMillis() < p(c).getLong("pausedUntil", 0);
    }

    static void pauseFor(Context c, long ms) {
        p(c).edit().putLong("pausedUntil", System.currentTimeMillis() + ms).apply();
    }

    static void clearPause(Context c) {
        p(c).edit().putLong("pausedUntil", 0).apply();
    }

    static boolean blocking(Context c) {
        if (paused(c)) return false;
        boolean guard = p(c).getBoolean("guard", false);
        return local(c) || (remote && guard);
    }

    static void saveAuth(Context c, String json) {
        p(c).edit().putString("auth", json).putLong("authAt", System.currentTimeMillis()).apply();
    }

    static String auth(Context c) {
        return p(c).getString("auth", null);
    }
}
