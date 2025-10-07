package org.example.helper;

public class CommandResultLog {
    private String screenshotName;
    private String method;
    private String requestData;
    private String responseData;
    private String requestPath;
    private long   startDate;
    private int    runtime;
    private long   endDate;
    private String level;

    public String getScreenshotName() { return screenshotName; }
    public void setScreenshotName(String screenshotName) { this.screenshotName = screenshotName; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getRequestData() { return requestData; }
    public void setRequestData(String requestData) { this.requestData = requestData; }

    public String getResponseData() { return responseData; }
    public void setResponseData(String responseData) { this.responseData = responseData; }

    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public int getRuntime() { return runtime; }
    public void setRuntime(int runtime) { this.runtime = runtime; }

    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
