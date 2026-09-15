package com.qihoo360.mobilesafe.report.qdas;

import android.text.TextUtils;
import android.util.Base64;
import com.qihoo360.mobilesafe.support.log.Logger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) return null;
        String table = "0123456789abcdef";
        StringBuilder ret = new StringBuilder(2 * bytes.length);

        for (int i = 0; i < bytes.length; i++) {
            int b;
            b = 0x0f & (bytes[i] >> 4);
            ret.append(table.charAt(b));
            b = 0x0f & bytes[i];
            ret.append(table.charAt(b));
        }

        return ret.toString();
    }

    /** 计算给定 byte [] 串的 MD5 */
    public static byte[] MD5(byte[] input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (md != null) {
            md.update(input);
            return md.digest();
        } else {
            return null;
        }
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuilder buf = new StringBuilder();
            for (byte element : b) {
                i = element;
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String formatMac(String mac) {
        if (TextUtils.isEmpty(mac)) {
            return "";
        }
        try {
            String regex = "(([a-f0-9]{2}:)|([a-f0-9]{2}-)){5}[a-f0-9]{2}";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(mac);

            if (!matcher.matches()) {
                return "";
            }
            return mac.replaceAll("[:-]", "");
        } catch (Exception e) {
        }
        return "";
    }

    public static String sha1UpperCase(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(data.getBytes());
            StringBuffer buf = new StringBuffer();
            byte[] bits = md.digest();
            for (int i = 0; i < bits.length; i++) {
                int a = bits[i];
                if (a < 0) a += 256;
                if (a < 16) buf.append("0");
                buf.append(Integer.toHexString(a));
            }
            return buf.toString().toUpperCase();
        } catch (Throwable e) {

        }
        return "";
    }

    public static String encryptCpm(int sSrc) {
        try {
            String cpm = String.valueOf(sSrc);
            if (Logger.sDebug) {
                Logger.d("加密", "===========加密前的cpm=========" + cpm);
            }
            return encrypt(cpm, "5fea9a7ed7e9f4a11a1877773198d370");
        } catch (Exception e) {
            if (Logger.sDebug) {
                Logger.handleException(e);
            }
        }
        return "";
    }

    public static String encrypt(String sSrc, String sKey) throws Exception {
        if (sKey == null) {
            return null;
        }
        byte[] raw = sKey.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // "算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));

        String s = Base64.encodeToString(encrypted, Base64.NO_WRAP);
        if (Logger.sDebug) {
            Logger.d("加密", "===========base64后:" + s);
        }
        String encode = URLEncoder.encode(s, "utf-8");
        if (Logger.sDebug) {
            Logger.d("加密", "===========URLEncoder后:" + encode);
        }
        return encode; // 此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }
}
