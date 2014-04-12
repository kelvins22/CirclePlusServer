package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    /**
     * Calculate a hash key for quick access
     * 
     * @param key
     *            original key to hash
     * @return the hash key
     */
    public static int hashKey(String key) {
        int cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = Integer.parseInt(bytesToHexString(mDigest.digest()));
        } catch (NoSuchAlgorithmException e) {
            cacheKey = key.hashCode();
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String generateLocKey(String name, long lat, long lng,
            String nation, String province, String city, String address) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(lat);
        sb.append(lng);
        sb.append(nation);
        sb.append(province);
        sb.append(city);
        sb.append(address);
        return sb.toString();
    }
}
