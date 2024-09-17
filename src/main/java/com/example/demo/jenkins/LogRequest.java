package com.example.demo.jenkins;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogRequest {
    private String jobName;
    private int buildNumber;
}
