package com.example.demo.jenkins;

public class PipelineRequest {
    private String name;
    private String description;
    private String gitRepoUrl;
    private String gitBranch;
    private String dockerImageName;
    private String deployPort;
    private String containerName;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGitRepoUrl() { return gitRepoUrl; }
    public void setGitRepoUrl(String gitRepoUrl) { this.gitRepoUrl = gitRepoUrl; }
    public String getGitBranch() { return gitBranch; }
    public void setGitBranch(String gitBranch) { this.gitBranch = gitBranch; }
    public String getDockerImageName() { return dockerImageName; }
    public void setDockerImageName(String dockerImageName) { this.dockerImageName = dockerImageName; }
    public String getDeployPort() { return deployPort; }
    public void setDeployPort(String deployPort) { this.deployPort = deployPort; }
    public String getContainerName() { return containerName; }
    public void setContainerName(String containerName) { this.containerName = containerName; }
}