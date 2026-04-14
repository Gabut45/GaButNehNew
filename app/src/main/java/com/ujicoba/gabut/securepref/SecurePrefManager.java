package com.ujicoba.gabut.securepref;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.ujicoba.gabut.R;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecurePrefManager implements SecurePrefManagerInterface{
    private SharedPreferences sharedPreferences;
    public static final String PREF_NAME = "Santa_Coder";

    public SecurePrefManager(Context context){
        try {

            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    context.getString(R.string.app_name)+PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getString(String prefName) {
        if (sharedPreferences.contains(prefName)) {
            return sharedPreferences.getString(prefName, null);
        }
        return "";
    }

    @Override
    public void setString(String prefName, String prefValue) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(prefName, prefValue);
        editor.apply();
    }
}
