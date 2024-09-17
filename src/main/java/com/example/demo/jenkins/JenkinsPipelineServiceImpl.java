package com.example.demo.jenkins;

import com.example.demo.jenkins.PipelineRequest;
import com.example.demo.jenkins.JenkinsRepository;
import com.example.demo.jenkins.JenkinsPipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JenkinsPipelineServiceImpl implements JenkinsPipelineService {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsPipelineServiceImpl.class);
    private final JenkinsRepository jenkinsRepository;

    public JenkinsPipelineServiceImpl(JenkinsRepository jenkinsRepository) {
        this.jenkinsRepository = jenkinsRepository;
    }

    @Override
    public void createPipeline(PipelineRequest pipelineRequest) throws Exception {
        logger.info("Creating pipeline: {}", pipelineRequest.getName());
        String jobConfig = createPipelineConfig(pipelineRequest);
        jenkinsRepository.createJob(pipelineRequest.getName(), jobConfig);
        logger.info("Pipeline '{}' created successfully", pipelineRequest.getName());
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