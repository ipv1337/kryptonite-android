package co.krypt.kryptonite.me;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArraySet;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.HashSet;
import java.util.Set;

import co.krypt.kryptonite.crypto.KeyManager;
import co.krypt.kryptonite.exception.CryptoException;
import co.krypt.kryptonite.log.SignatureLog;
import co.krypt.kryptonite.pairing.Pairing;
import co.krypt.kryptonite.protocol.JSON;
import co.krypt.kryptonite.protocol.Profile;

/**
 * Created by Kevin King on 12/3/16.
 * Copyright 2016. KryptCo, Inc.
 */

public class MeStorage {
    private static final String TAG = "MeStorage";
    private static Object lock = new Object();
    private SharedPreferences preferences;

    public MeStorage(Context context) {
        preferences = context.getSharedPreferences("ME_MANAGER_PREFERENCES", Context.MODE_PRIVATE);
    }

    public Profile load() {
        synchronized (lock) {
            String meJSON = preferences.getString("ME", null);
            if (meJSON == null) {
                Log.i(TAG, "no profile found");
                return null;
            }
            Profile me = JSON.fromJson(meJSON, Profile.class);
            if (me == null) {
                Log.i(TAG, "no profile found");
                return null;
            }
            try {
                me.sshWirePublicKey = KeyManager.loadOrGenerateKeyPair(KeyManager.MY_RSA_KEY_TAG).publicKeySSHWireFormat();
            } catch (InvalidKeyException | IOException | CryptoException e) {
                e.printStackTrace();
            }
            return me;
        }
    }

    public void delete() {
        synchronized (lock) {
            preferences.edit().putString("ME", "").commit();
        }
    }

    public void set(Profile profile) {
        synchronized (lock) {
            preferences.edit().putString("ME", JSON.toJson(profile)).commit();
        }
    }

    public void setEmail(String email) {
        synchronized (lock) {
            Profile me = load();
            if (me == null) {
                me = new Profile();
            }
            me.email = email;
            set(me);
        }
    }
}
