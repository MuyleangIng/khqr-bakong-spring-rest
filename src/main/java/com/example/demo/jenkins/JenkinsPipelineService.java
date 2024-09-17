package com.example.demo.jenkins;

import com.example.demo.jenkins.PipelineRequest;

public interface JenkinsPipelineService {
    void createPipeline(PipelineRequest pipelineRequest) throws Exception;
}