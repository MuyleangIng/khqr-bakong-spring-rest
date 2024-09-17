package com.example.demo.jenkins;

import com.example.demo.jenkins.PipelineRequest;
import com.example.demo.jenkins.JenkinsPipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/jenkins")
public class JenkinsPipelineController {

    private final JenkinsPipelineService pipelineService;
    private static final Logger logger = LoggerFactory.getLogger(JenkinsPipelineServiceImpl.class);

    public JenkinsPipelineController(JenkinsPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }
    @PostMapping("/create-pipeline")
    public ResponseEntity<String> createPipeline(@RequestBody PipelineRequest pipelineRequest) {
        try {
            pipelineService.createPipeline(pipelineRequest);
            return ResponseEntity.ok("Pipeline created successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating pipeline: " + e.getMessage());
        }
    }
    @GetMapping("/job-log")
    public ResponseEntity<String> getJobLog(@RequestBody LogRequest logRequest) {
        try {
            String log = pipelineService.getJobLog(logRequest);
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching job log: " + e.getMessage());
        }
    }
    @PostMapping("/start-build")
    public ResponseEntity<?> startBuild(@RequestBody BuildRequest buildRequest) {
        try {
            int buildNumber = pipelineService.startBuild(buildRequest);
            return ResponseEntity.ok(Map.of("message", "Build started successfully", "buildNumber", buildNumber));
        } catch (Exception e) {
            logger.error("Error starting build", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to start build", "details", e.getMessage()));
        }
    }
    @GetMapping(value = "/stream-log/{jobName}/{buildNumber}", produces = "text/event-stream")
    public SseEmitter streamLog(@PathVariable String jobName, @PathVariable int buildNumber) {
        return pipelineService.streamBuildLog(jobName, buildNumber);
    }
}