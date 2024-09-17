package com.example.demo.jenkins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class JenkinsPipelineServiceImpl implements JenkinsPipelineService {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsPipelineServiceImpl.class);
    private final JenkinsRepository jenkinsRepository;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public JenkinsPipelineServiceImpl(JenkinsRepository jenkinsRepository) {
        this.jenkinsRepository = jenkinsRepository;
    }
    @Override
    public SseEmitter streamBuildLog(String jobName, int buildNumber) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        executorService.execute(() -> {
            try {
                jenkinsRepository.streamBuildLog(jobName, buildNumber,
                        log -> {
                            try {
                                emitter.send(SseEmitter.event().data(log));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        }
                );
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
    @Override
    public int startBuild(BuildRequest buildRequest) throws Exception {
        logger.info("Starting build for job: {}", buildRequest.getJobName());
        try {
            int buildNumber = jenkinsRepository.startBuild(buildRequest.getJobName(), buildRequest.getParameters());
            logger.info("Build started for job: {}, build number: {}", buildRequest.getJobName(), buildNumber);
            return buildNumber;
        } catch (Exception e) {
            logger.error("Error starting build for job: {}. Error: {}", buildRequest.getJobName(), e.getMessage(), e);
            throw new Exception("Failed to start build: " + e.getMessage(), e);
        }
    }

    @Override
    public void createPipeline(PipelineRequest pipelineRequest) throws Exception {
        logger.info("Creating pipeline: {}", pipelineRequest.getName());
        String jobConfig = createPipelineConfig(pipelineRequest);
        jenkinsRepository.createJob(pipelineRequest.getName(), jobConfig);
        logger.info("Pipeline '{}' created successfully", pipelineRequest.getName());
    }

    @Override
    public String getJobLog(LogRequest logRequest) throws Exception {
        logger.info("Fetching log for job: {}, build: {}", logRequest.getJobName(), logRequest.getBuildNumber());
        return jenkinsRepository.getJobLog(logRequest.getJobName(), logRequest.getBuildNumber());
    }

    private String createPipelineConfig(PipelineRequest pipelineRequest) {
        String pipelineScript = createPipelineScript(pipelineRequest);
        return String.format(
                "<?xml version='1.1' encoding='UTF-8'?>\n" +
                        "<flow-definition plugin=\"workflow-job@2.40\">\n" +
                        "  <description>%s</description>\n" +
                        "  <keepDependencies>false</keepDependencies>\n" +
                        "  <properties>\n" +
                        "    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>\n" +
                        "      <triggers/>\n" +
                        "    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>\n" +
                        "  </properties>\n" +
                        "  <definition class=\"org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition\" plugin=\"workflow-cps@2.90\">\n" +
                        "    <script>%s</script>\n" +
                        "    <sandbox>true</sandbox>\n" +
                        "  </definition>\n" +
                        "  <triggers/>\n" +
                        "  <disabled>false</disabled>\n" +
                        "</flow-definition>",
                pipelineRequest.getDescription(),
                pipelineScript
        );
    }

    private String createPipelineScript(PipelineRequest pipelineRequest) {
        return String.format(
                "pipeline {\n" +
                        "    agent any\n" +
                        "    environment {\n" +
                        "        GIT_REPO_URL = '%s'\n" +
                        "        GIT_BRANCH = '%s'\n" +
                        "        DOCKER_IMAGE_NAME = '%s'\n" +
                        "        DOCKER_IMAGE_TAG = '${BUILD_NUMBER}'\n" +
                        "        DEPLOY_PORT = '%s'\n" +
                        "        CONTAINER_NAME = '%s'\n" +
                        "    }\n" +
                        "    stages {\n" +
                        "        stage('Checkout') {\n" +
                        "            steps {\n" +
                        "                git branch: env.GIT_BRANCH, url: env.GIT_REPO_URL\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Build') {\n" +
                        "            steps {\n" +
                        "                sh 'docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} .'\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Deploy') {\n" +
                        "            steps {\n" +
                        "                sh '''\n" +
                        "                    docker stop ${CONTAINER_NAME} || true\n" +
                        "                    docker rm ${CONTAINER_NAME} || true\n" +
                        "                    docker run -d --name ${CONTAINER_NAME} -p ${DEPLOY_PORT}:3000 ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}\n" +
                        "                '''\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}",
                pipelineRequest.getGitRepoUrl(),
                pipelineRequest.getGitBranch(),
                pipelineRequest.getDockerImageName(),
                pipelineRequest.getDeployPort(),
                pipelineRequest.getContainerName()
        );
    }
}