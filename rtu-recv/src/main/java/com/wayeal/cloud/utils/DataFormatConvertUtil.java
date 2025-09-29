package com.wayeal.cloud.utils;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

public class DataFormatConvertUtil {

    public static byte[] intToByteArray(long res, int length) {
        byte[] byteArray = new byte[length];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = ((byte) (int) (res >> (byteArray.length - i - 1) * 8 & 0xFF));
        }
        return byteArray;
    }

    public static int byteArrayToSignedInt(byte[] res) {
        byte[] bytes = new byte[4];
        if ((res[0] & 0x80) == 128) {
            for (int i = 0; i < bytes.length - res.length; i++) {
                bytes[i] = -1;
            }
            for (int i = bytes.length - res.length; i < bytes.length; i++)
                bytes[i] = res[(i - (bytes.length - res.length))];
        } else {
            for (int i = bytes.length - res.length; i < bytes.length; i++) {
                bytes[i] = res[(i - (bytes.length - res.length))];
            }
        }
        int nResult = getResult(bytes);
        return nResult;
    }

    private static int getResult(byte[] res) {
        int nResult = 0;
        for (int i = 0; i < res.length; i++) {
            int nTemp = res[i] & 0xFF;
            nTemp <<= (res.length - i - 1) * 8;
            nResult |= nTemp;
        }
        return nResult;
    }

    public static int byteArrayToUnsignedInt(byte[] res) {
        byte[] bytes = new byte[4];
        for (int i = bytes.length - res.length; i < bytes.length; i++) {
            bytes[i] = res[(i - (bytes.length - res.length))];
        }
        int nResult = getResult(bytes);
        return nResult;
    }

    public static long byteArrayToSignedlong(byte[] res) {
        byte[] bytes = new byte[8];
        if ((res[0] & 0x80) == 128) {
            for (int i = 0; i < bytes.length - res.length; i++) {
                bytes[i] = -1;
            }
            for (int i = bytes.length - res.length; i < bytes.length; i++)
                bytes[i] = res[(i - (bytes.length - res.length))];
        } else {
            for (int i = bytes.length - res.length; i < bytes.length; i++) {
                bytes[i] = res[(i - (bytes.length - res.length))];
            }
        }
        long nResult = getResult(bytes);
        return nResult;
    }

    public static long byteArrayToUnsignedlong(byte[] res) {
        byte[] bytes = new byte[8];
        if (res.length < 8) {
            for (int i = bytes.length - res.length; i < bytes.length; i++) {
                bytes[i] = res[(i - (bytes.length - res.length))];
            }
        }
        long nResult = getResult(bytes);
        return nResult;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if ((hexString == null) || (hexString.equals(""))) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = ((byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[(pos + 1)])));
        }
        return d;
    }

    public static String stringTohexString(String string) {
        if ((string == null) || (string.equals(""))) {
            return null;
        }
        if (string.length() < 2) {
            return "0" + string;
        }
        return string.toUpperCase();
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static int hexToInt(String str) {
        return Integer.parseInt(str, 16);
    }

    public static String intToHexString(int value) {
        StringBuilder str = new StringBuilder("");
        String hv = Integer.toHexString(value);
        if (hv.length() < 2) {
            str.append(0);
        }
        str.append(hv);
        return str.toString();
    }

    public static String intToHexString(int value, int length) {
        String v = "";
        StringBuilder str = new StringBuilder("");
        int maxInt = getMaxIntByByteSigned(length);
        int mi = maxInt / 2;
        if ((value < 0) &&
                (value < -mi)) {
            value = -mi;
        }

        if ((value > 0) &&
                (value > mi)) {
            value = mi;
        }

        String hv = Integer.toHexString(value);
        int i = length * 2 - hv.length();
        for (int j = 0; j < i; j++) {
            str.append(0);
        }
        str.append(hv);
        if (value < 0) {
            v = str.substring(str.length() - length * 2, str.length());
            return v;
        }
        return str.toString();
    }

    private static int getMaxIntByByteSigned(int length) {
        StringBuffer maxHex = new StringBuffer();
        for (int i = 0; i < length; i++) {
            maxHex.append("FF");
        }
        return hexToInt(maxHex.toString());
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder str = new StringBuilder("");
        for (int i = 0; i < byteArray.length; i++) {
            int v = byteArray[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                str.append(0);
            }
            str.append(hv);
        }
        return str.toString();
    }

    public static String shortToHex(short in) {
        byte[] b = new byte[2];
        for (int i = 0; i < 2; i++) {
            b[i] = ((byte) (in >> (1 - i) * 8 & 0xFF));
        }
        String bstr = byteArrayToHexString(b);
        return bstr;
    }

    public static String floatToHexString(float f) {
        return Float.toHexString(f);
    }

    public static float byteToFloat(byte[] by) {
        ByteBuffer buffer = ByteBuffer.wrap(by);
        FloatBuffer fb = buffer.asFloatBuffer();
        return fb.get();
    }

    public static double byteToDouble(byte[] by) {
        ByteBuffer buffer = ByteBuffer.wrap(by);
        DoubleBuffer fb = buffer.asDoubleBuffer();
        return fb.get();
    }

    public static String getBinaryStrFromByte(byte b) {
        String result = "";
        byte a = b;
        ;
        for (int i = 0; i < 8; i++) {
            byte c = a;
            a = (byte) (a >> 1);//每移一位如同将10进制数除以2并去掉余数。
            a = (byte) (a << 1);
            if (a == c) {
                result = "0" + result;
            } else {
                result = "1" + result;
            }
            a = (byte) (a >> 1);
        }
        return result;

    }

    public static byte bit2byte(String bString) {
        byte result = 0;
        for (int i = bString.length() - 1, j = 0; i >= 0; i--, j++) {
            result += (Byte.parseByte(bString.charAt(i) + "") * Math.pow(2, j));
        }
        return result;
    }
    public static String[] stringToStringArray(String src, int length) {
        //检查参数是否合法
        if (null == src || src.equals("")) {
            return null;
        }

        if (length <= 0) {
            return null;
        }
        int n = (src.length() + length - 1) / length; //获取整个字符串可以被切割成字符子串的个数
        String[] split = new String[n];
        for (int i = 0; i < n; i++) {
            if (i < (n - 1)) {
                split[i] = src.substring(i * length, (i + 1) * length);
            } else {
                split[i] = src.substring(i * length);
            }
        }
        return split;

    }
}
