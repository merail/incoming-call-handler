package merail.calls.handler

import android.content.Context

class PreferenceHelper {
    fun getPreference(ctx: Context, key: String): String? {
        val sharedPreference =  ctx.getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        return sharedPreference.getString(key, "");
    }

    fun setPreference(ctx: Context, key: String, pref: String) {
        val sharedPreference =  ctx.getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        var editor = sharedPreference.edit()
        editor.putString(key, pref)
        editor.commit()
    }
}
