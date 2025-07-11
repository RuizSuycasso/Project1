// File: src/main/java/com/example/myapplication/ResultActivity.java
// << PHIÊN BẢN ĐÃ SỬA LỖI CÚ PHÁP JAVA 11 >>

package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT_TEXT = "com.example.myapplication.RESULT_TEXT";
    public static final String EXTRA_DRUG_LIST = "DRUG_LIST_EXTRA";

    private TextView resultDetailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultDetailTextView = findViewById(R.id.resultDetailTextView);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent == null) {
            showError("Không nhận được dữ liệu.");
            return;
        }

        ArrayList<DrugInfo> drugList = intent.getParcelableArrayListExtra(EXTRA_DRUG_LIST);
        String adviceText = intent.getStringExtra(EXTRA_RESULT_TEXT);

        displayCombinedResults(drugList, adviceText);
    }

    private void displayCombinedResults(ArrayList<DrugInfo> drugList, String adviceText) {
        setTitle("Kết quả Phân tích & Tư vấn");
        StringBuilder htmlBuilder = new StringBuilder();
        boolean hasContent = false;

        if (drugList != null && !drugList.isEmpty()) {
            hasContent = true;
            htmlBuilder.append("<h1><font color='#2E7D32'>💊 THÔNG TIN ĐƠN THUỐC</font></h1>");
            for (int i = 0; i < drugList.size(); i++) {
                DrugInfo drug = drugList.get(i);
                htmlBuilder.append("<br>");
                htmlBuilder.append("<b><font color='#1976D2'>🔹 Thuốc ").append(i + 1).append(": ").append(escapeHtml(drug.getDrug())).append("</font></b><br>");
                htmlBuilder.append("📋 <i><font color='#F57C00'>Liều lượng: ").append(escapeHtml(drug.getDosage())).append("</font></i><br>");
            }
        }

        if (!TextUtils.isEmpty(adviceText)) {
            hasContent = true;
            if (drugList != null && !drugList.isEmpty()) {
                htmlBuilder.append("<br><hr><br>");
            }
            htmlBuilder.append("<h1><font color='#673AB7'>💡 LỜI KHUYÊN TỪ TRỢ LÝ AI</font></h1>");
            htmlBuilder.append(escapeHtml(adviceText).replace("\n", "<br>"));
        }

        if (hasContent) {
            htmlBuilder.append("<br><br><br>");
            htmlBuilder.append("<h2><font color='#D32F2F'>⚠️ LƯU Ý QUAN TRỌNG</font></h2>");
            htmlBuilder.append("• Thông tin trên chỉ mang tính chất tham khảo.<br>");
            htmlBuilder.append("• Luôn tuân thủ chỉ định và tư vấn của bác sĩ hoặc dược sĩ chuyên môn.");
            resultDetailTextView.setText(HtmlCompat.fromHtml(htmlBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            showError("Không nhận được dữ liệu để hiển thị.");
        }
    }

    private void showError(String message) {
        setTitle("Lỗi");
        resultDetailTextView.setText(message);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}