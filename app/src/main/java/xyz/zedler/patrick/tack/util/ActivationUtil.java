/*
 * This file is part of Tack Android.
 *
 * Tack Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tack Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tack Android. If not, see http://www.gnu.org/licenses/.
 *
 * Copyright (c) 2020-2026 by Patrick Zedler
 */

package xyz.zedler.patrick.tack.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ActivationUtil {

  public static final String MASTER_PASSWORD = "321654";
  private static final String SECRET_KEY = "TackMetronome2026SecretKey";
  private static final String PREF_ACTIVATED = "app_activated";
  private static final String PREF_ACTIVATION_CODE = "app_activation_code";
  private static final String PREF_USED_CODES = "app_used_codes";
  private static final String CODE_PREFIX = "TACK";
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  /**
   * Check if the app is activated.
   */
  public static boolean isActivated(@NonNull Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getBoolean(PREF_ACTIVATED, false);
  }

  /**
   * Attempt to activate with the given code.
   * Returns true if activation succeeded, false otherwise.
   */
  public static boolean activate(@NonNull Context context, @NonNull String code) {
    String normalized = normalizeCode(code);
    if (!isValidCode(normalized)) {
      return false;
    }
    String serial = extractSerial(normalized);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    Set<String> usedCodes = new HashSet<>(prefs.getStringSet(PREF_USED_CODES, new HashSet<>()));
    if (usedCodes.contains(serial)) {
      return false;
    }
    usedCodes.add(serial);
    prefs.edit()
        .putBoolean(PREF_ACTIVATED, true)
        .putString(PREF_ACTIVATION_CODE, normalized)
        .putStringSet(PREF_USED_CODES, usedCodes)
        .apply();
    return true;
  }

  /**
   * Generate a batch of activation codes.
   */
  public static List<String> generateCodes(int count) {
    List<String> codes = new ArrayList<>();
    long baseTime = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      String serial = generateSerial(baseTime + i);
      String signature = computeSignature(serial);
      String code = formatCode(serial, signature);
      codes.add(code);
    }
    return codes;
  }

  /**
   * Validate a code against the HMAC signature.
   */
  public static boolean isValidCode(@NonNull String code) {
    String normalized = normalizeCode(code);
    if (!normalized.startsWith(CODE_PREFIX)) {
      return false;
    }
    String body = normalized.substring(CODE_PREFIX.length());
    if (body.length() != 12) {
      return false;
    }
    String serial = body.substring(0, 6);
    String signature = body.substring(6, 12);
    String expected = computeSignature(serial);
    return signature.equals(expected);
  }

  private static String normalizeCode(@NonNull String code) {
    return code.replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
  }

  private static String extractSerial(@NonNull String normalizedCode) {
    return normalizedCode.substring(CODE_PREFIX.length(), CODE_PREFIX.length() + 6);
  }

  private static String generateSerial(long seed) {
    String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    StringBuilder sb = new StringBuilder();
    long value = Math.abs(seed);
    for (int i = 0; i < 6; i++) {
      int idx = (int) (Math.abs(value) % chars.length());
      sb.append(chars.charAt(idx));
      value = Math.abs(value / chars.length() + (value * 31 + 17));
    }
    return sb.toString();
  }

  private static String computeSignature(@NonNull String serial) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      SecretKeySpec keySpec = new SecretKeySpec(
          SECRET_KEY.getBytes(), HMAC_ALGORITHM
      );
      mac.init(keySpec);
      byte[] hash = mac.doFinal(serial.getBytes());
      String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 6; i++) {
        int idx = (hash[i] & 0xFF) % chars.length();
        sb.append(chars.charAt(idx));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException("HMAC computation failed", e);
    }
  }

  private static String formatCode(String serial, String signature) {
    String full = CODE_PREFIX + serial + signature;
    return full.substring(0, 4) + "-" + full.substring(4, 8) + "-"
        + full.substring(8, 12) + "-" + full.substring(12, 16);
  }
}
