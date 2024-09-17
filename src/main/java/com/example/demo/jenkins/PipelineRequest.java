package com.example.demo.jenkins;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PipelineRequest {
    private String name;
    private String description;
    private String gitRepoUrl;
    private String gitBranch;
    private String dockerImageName;
    private String deployPort;
    private String containerName;

}