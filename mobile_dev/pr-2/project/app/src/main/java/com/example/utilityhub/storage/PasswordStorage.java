package com.example.utilityhub.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.utilityhub.models.PasswordItem;
import com.example.utilityhub.security.CryptoManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class PasswordStorage {
    private static final String PREFS = "vault_data";
    private static final String KEY_ITEMS = "password_items_json";
    private final SharedPreferences prefs;
    private final CryptoManager crypto = new CryptoManager();

    public PasswordStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<PasswordItem> load() {
        List<PasswordItem> items = new ArrayList<>();
        String json = prefs.getString(KEY_ITEMS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                items.add(new PasswordItem(
                        obj.getString("id"),
                        crypto.decrypt(obj.getString("service")),
                        crypto.decrypt(obj.getString("login")),
                        obj.getString("encryptedPassword"),
                        crypto.decrypt(obj.getString("note"))
                ));
            }
        } catch (Exception ignored) {
            // Не виводимо в Log вміст сховища або паролі.
        }
        return items;
    }

    public void save(List<PasswordItem> items) {
        JSONArray array = new JSONArray();
        try {
            for (PasswordItem item : items) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.getId());
                // Додаткове завдання: service, login і note теж не лежать відкрито.
                obj.put("service", crypto.encrypt(item.getService()));
                obj.put("login", crypto.encrypt(item.getLogin()));
                obj.put("encryptedPassword", item.getEncryptedPassword());
                obj.put("note", crypto.encrypt(item.getNote()));
                array.put(obj);
            }
            prefs.edit()
                    .putString(KEY_ITEMS, array.toString())
                    .apply();
        } catch (Exception ignored) {
            // Для навчальної роботи помилку не логувати разом із даними.
        }
    }
}
