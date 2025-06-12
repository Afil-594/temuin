package com.example.temuin.lost;

public class LostItem {
    private String id;
    private String name;
    private String noHp;
    private String namaBarang;
    private String location;
    private String date;
    private String time;
    private String imagePath;
    private String status;     // Untuk verifikasi: "belum diverifikasi", "diverifikasi", "ditolak"
    private String progress;   // Untuk tampilan UI: "hilang", "dikembalikan"
    private String userId;

    public LostItem() {
        // Default constructor untuk Firebase
    }

    public LostItem(String id, String name, String noHp, String namaBarang, String location, String date, String time, String imagePath, String status, String progress, String userId) {
        this.id = id;
        this.name = name;
        this.noHp = noHp;
        this.namaBarang = namaBarang;
        this.location = location;
        this.date = date;
        this.time = time;
        this.imagePath = imagePath;
        this.status = status;
        this.progress = progress;
        this.userId = userId;
    }

    // Getter dan Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProgress() { return progress; }
    public void setProgress(String progress) { this.progress = progress; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
