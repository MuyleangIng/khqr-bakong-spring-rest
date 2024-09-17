package com.example.demo.jenkins;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Repository
public class JenkinsRepository {

    private final JenkinsServer jenkins;

    public JenkinsRepository(
            @Value("${jenkins.url}") String jenkinsUrl,
            @Value("${jenkins.username}") String jenkinsUsername,
            @Value("${jenkins.password}") String jenkinsPassword) throws URISyntaxException {
        JenkinsHttpClient client = new JenkinsHttpClient(new URI(jenkinsUrl), jenkinsUsername, jenkinsPassword);
        this.jenkins = new JenkinsServer(client);
    }

    public void createJob(String jobName, String jobConfig) throws IOException {
        jenkins.createJob(jobName, jobConfig);
    }
}