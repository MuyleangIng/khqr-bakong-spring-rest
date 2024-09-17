package com.example.demo.jenkins;

import com.example.demo.jenkins.PipelineRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface JenkinsPipelineService {
    void createPipeline(PipelineRequest pipelineRequest) throws Exception;
    String getJobLog(LogRequest logRequest) throws Exception;

    SseEmitter streamBuildLog(String jobName, int buildNumber);

    int startBuild(BuildRequest buildRequest) throws Exception;

}