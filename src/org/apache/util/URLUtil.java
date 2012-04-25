package org.apache.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.TimeZone;

public final class URLUtil {

	private static SimpleDateFormat format = new SimpleDateFormat(" EEEE, dd-MMM-yy kk:mm:ss zz");
    protected static BitSet safeCharacters;
    protected static final char[] hexadecimal = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        safeCharacters = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            safeCharacters.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            safeCharacters.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            safeCharacters.set(i);
        }
        safeCharacters.set('-');
        safeCharacters.set('_');
        safeCharacters.set('.');
        safeCharacters.set('*');
        safeCharacters.set('/');
    }
    
    public static String URLDecode(String str) {
        return URLDecode(str, null);
    }
    
    public static String URLDecode(String str, String enc) {
        if (str == null) return (null);
        byte[] bytes;
        if (enc == null) {
            bytes = str.getBytes();
        } else  {
            try {
                bytes = str.getBytes(enc);
            } catch (UnsupportedEncodingException ex) {
                bytes = str.getBytes();
            }
        }
        return URLDecode(bytes, enc);
    }
    
    public static String URLDecode(byte[] bytes) {
        return URLDecode(bytes, null);
    }
    
    public static String URLDecode(byte[] bytes, String enc) {
        if (bytes == null) return (null);
        int len = bytes.length;
        int ix = 0;
        int ox = 0;
        while (ix < len) {
            byte b = bytes[ix++];
            if (b == '%') {
                b = (byte)((convertHexDigit(bytes[ix++]) << 4) + convertHexDigit(bytes[ix++]));
            }
            if (b == '+') {
                b = (byte)' ';
            }
            bytes[ox++] = b;
        }
        if (enc != null) {
            try {
                return new String(bytes, 0, ox, enc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new String(bytes, 0, ox);
    }
    
    private static byte convertHexDigit(byte b) {
        if ((b >= '0') && (b <= '9')) return (byte)(b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte)(b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte)(b - 'A' + 10);
        return 0;
    }
    
    public static String URLEncode(String path, String enc) {
        int maxBytesPerChar = 10;
        StringBuffer rewrittenPath = new StringBuffer(path.length());
        ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(buf, enc);
        } catch (Exception e) {
            e.printStackTrace();
            writer = new OutputStreamWriter(buf);
        }
        for (int i = 0; i < path.length(); i++) {
            int c = (int)path.charAt(i);
            if (safeCharacters.get(c)) {
                rewrittenPath.append((char)c);
            } else  {
                try {
                    writer.write(c);
                    writer.flush();
                } catch (IOException e) {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for (int j = 0; j < ba.length; j++) {
                    byte toEncode = ba[j];
                    rewrittenPath.append('%');
                    int low = (int)(toEncode & 15);
                    int high = (int)((toEncode & 240) >> 4);
                    rewrittenPath.append(hexadecimal[high]);
                    rewrittenPath.append(hexadecimal[low]);
                }
                buf.reset();
            }
        }
        return rewrittenPath.toString();
    }
}