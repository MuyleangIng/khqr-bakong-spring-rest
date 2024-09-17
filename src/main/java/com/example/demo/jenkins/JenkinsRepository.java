package com.example.demo.jenkins;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.model.*;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.Consumer;

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
    public int startBuild(String jobName, Map<String, String> parameters) throws IOException, InterruptedException {
        JobWithDetails job = jenkins.getJob(jobName);
        QueueReference queueReference;

        if (parameters != null && !parameters.isEmpty()) {
            queueReference = job.build(parameters);
        } else {
            queueReference = job.build();
        }

        return getBuildNumberFromQueue(queueReference);
    }
    private int getBuildNumberFromQueue(QueueReference queueReference) throws IOException, InterruptedException {
        QueueItem queueItem = jenkins.getQueueItem(queueReference);
        while (queueItem.getExecutable() == null) {
            Thread.sleep(100);
            queueItem = jenkins.getQueueItem(queueReference);
        }
        return queueItem.getExecutable().getNumber().intValue();
    }
    public String getJobLog(String jobName, int buildNumber) throws IOException {
        JobWithDetails job = jenkins.getJob(jobName);
        return job.getBuildByNumber(buildNumber).details().getConsoleOutputText();
    }
    public void streamBuildLog(String jobName, int buildNumber, Consumer<String> logConsumer) throws IOException, InterruptedException {
        JobWithDetails job = jenkins.getJob(jobName);
        Build build = job.getBuildByNumber(buildNumber);
        BuildWithDetails buildDetails = build.details(); // Get the details of the build

        long lastPosition = 0;
        boolean isBuilding = true;

        while (isBuilding) {
            String logText = buildDetails.getConsoleOutputText(); // Use BuildWithDetails to get the log

            if (logText.length() > lastPosition) {
                String newContent = logText.substring((int) lastPosition);
                logConsumer.accept(newContent);
                lastPosition = logText.length();
            }

            isBuilding = buildDetails.isBuilding();
            if (isBuilding) {
                Thread.sleep(1000); // Wait for 1 second before polling again
            }
        }
    }


}