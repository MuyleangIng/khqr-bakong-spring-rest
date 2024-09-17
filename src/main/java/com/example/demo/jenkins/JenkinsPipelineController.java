package com.example.demo.jenkins;

import com.example.demo.jenkins.PipelineRequest;
import com.example.demo.jenkins.JenkinsPipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jenkins")
public class JenkinsPipelineController {

    private final JenkinsPipelineService pipelineService;

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
}