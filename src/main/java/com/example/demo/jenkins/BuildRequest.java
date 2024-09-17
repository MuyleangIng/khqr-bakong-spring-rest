package com.example.demo.jenkins;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class BuildRequest {
    private String jobName;
    private Map<String, String> parameters;
}
