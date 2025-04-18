package com.example.myapplication; // Đảm bảo package đúng

import android.util.Base64; // <-- THAY ĐỔI IMPORT NÀY
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
// import java.util.Base64; // <-- XÓA HOẶC COMMENT IMPORT NÀY

public class PasswordHasher {

    private static final String TAG = "PasswordHasher";
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    public static String hashPassword(String password, byte[] salt) {
        if (password == null || salt == null) { // Thêm kiểm tra null cơ bản
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes()); // Nên xử lý mã hóa ký tự (e.g., UTF-8)

            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            // --- THAY ĐỔI CÁCH GỌI BASE64 ---
            return Base64.encodeToString(combined, Base64.NO_WRAP); // Sử dụng android.util.Base64

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Lỗi không tìm thấy thuật toán băm: " + ALGORITHM, e);
            return null;
        } catch (Exception e) { // Bắt các lỗi khác có thể xảy ra
            Log.e(TAG, "Lỗi không mong đợi khi băm mật khẩu", e);
            return null;
        }
    }

    public static boolean verifyPassword(String inputPassword, String storedHash) {
        if (storedHash == null || inputPassword == null) {
            return false;
        }
        try {
            // --- THAY ĐỔI CÁCH GỌI BASE64 ---
            byte[] combined = Base64.decode(storedHash, Base64.NO_WRAP); // Sử dụng android.util.Base64

            if (combined.length < SALT_LENGTH) {
                Log.e(TAG, "Stored hash không hợp lệ (quá ngắn)");
                return false;
            }

            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);

            // Băm lại mật khẩu nhập vào với salt đã lấy được
            String inputHash = hashPassword(inputPassword, salt);

            // So sánh hash mới tạo (dạng Base64) với hash đã lưu trữ
            // Phải chắc chắn hashPassword luôn trả về kết quả hoặc null
            return storedHash.equals(inputHash);

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Lỗi giải mã Base64 từ hash đã lưu: " + storedHash, e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi không mong đợi khi xác thực mật khẩu", e);
            return false;
        }
    }
}