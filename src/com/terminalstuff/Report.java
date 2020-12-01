package com.terminalstuff;

public class Report {

    private String modId;
    private String reportId;
    private String reporterId;
    private String timestamp;

    public Report() {
    }

    public Report(String reporterId, String modId, String reportId, String timestamp) {
        super();
        this.modId = modId;
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.timestamp = timestamp;
    }

    public String getMod() {
        return modId;
    }
    public void setMod(String modId) {
        this.modId = modId;
    }

    public String getReporter() {
        return reporterId;
    }
    public void setReporter(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getReportId() {
        return reportId;
    }
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}