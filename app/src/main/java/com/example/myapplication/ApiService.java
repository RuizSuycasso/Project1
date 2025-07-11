// File: src/main/java/com/example/myapplication/ApiService.java
// << PHIÊN BẢN ĐÃ SỬA LỖI >>

package com.example.myapplication;

import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    /**
     * Endpoint để tải lên một file ảnh.
     * Phần này đã đúng, giữ nguyên.
     */
    @Multipart
    @POST("ocr")
    Call<List<DrugInfo>> uploadImage(@Part MultipartBody.Part file);

    /**
     * Endpoint để gửi bệnh án và nhận lời khuyên từ AI.
     * Sửa đổi ở đây để nó trả về đúng đối tượng AdviceResponse.
     */
    @POST("advise_from_record")
    // =================================================================
    // === SỬA LỖI TẠI ĐÂY: Thay ResponseBody bằng AdviceResponse ===
    // =================================================================
    Call<AdviceResponse> getAdviceFromRecord(@Body BenhAnRequest benhAnRequest);

}