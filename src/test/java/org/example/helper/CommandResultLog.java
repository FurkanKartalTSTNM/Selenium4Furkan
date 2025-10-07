package org.example.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandResultLog {
    private String screenshotName;
    private String method;       // HTTP method bilmiyorsan command name'i yaz
    private String requestData;
    private String responseData;
    private String requestPath;  // command name
    private long startDate;
    private int runtime;         // ms
    private long endDate;
    private String level;        // PASS/FAIL/ERROR/INFO

    // getters & setters ...
}
