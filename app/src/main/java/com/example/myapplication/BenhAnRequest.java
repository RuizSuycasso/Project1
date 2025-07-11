package com.example.myapplication; // <-- Thay đổi package name này cho khớp với project của bạn

/**
 * Data Transfer Object (DTO) cho việc gửi thông tin bệnh án lên server.
 * Class này không chứa các annotation của Room và chỉ dùng cho việc giao tiếp mạng.
 * Các tên trường phải khớp với các key mà server mong đợi trong JSON.
 */
public class BenhAnRequest {

    // Các trường dữ liệu sẽ được Gson serialize thành JSON
    private int userId;
    private String diagnosis;
    private String medicalHistory;
    private String labResults;
    private String allergies;
    private String currentMedications;
    private String diseaseStage;

    public BenhAnRequest(int userId, String diagnosis, String medicalHistory, String labResults,
                         String allergies, String currentMedications, String diseaseStage) {
        this.userId = userId;
        this.diagnosis = diagnosis;
        this.medicalHistory = medicalHistory;
        this.labResults = labResults;
        this.allergies = allergies;
        this.currentMedications = currentMedications;
        this.diseaseStage = diseaseStage;
    }

    public BenhAnRequest(BenhAn benhAnEntity) {
        this.userId = benhAnEntity.userId;
        this.diagnosis = benhAnEntity.diagnosis;
        this.medicalHistory = benhAnEntity.medicalHistory;
        this.labResults = benhAnEntity.labResults;
        this.allergies = benhAnEntity.allergies;
        this.currentMedications = benhAnEntity.currentMedications;
        this.diseaseStage = benhAnEntity.diseaseStage;
    }

    // --- Getter và Setter ---
    // Cần thiết để thư viện Gson có thể truy cập và serialize các trường private.
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getLabResults() { return labResults; }
    public void setLabResults(String labResults) { this.labResults = labResults; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getCurrentMedications() { return currentMedications; }
    public void setCurrentMedications(String currentMedications) { this.currentMedications = currentMedications; }

    public String getDiseaseStage() { return diseaseStage; }
    public void setDiseaseStage(String diseaseStage) { this.diseaseStage = diseaseStage; }
}