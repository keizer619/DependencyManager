package org.wso2.sample.library;

/**
 * Created by tharik on 1/29/15.
 */
public class Dependency {

    private String groupId;
    private String artifactId;
    private String version;
    private String repositoryDepends;
    private String repositorySource;


    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRepositoryDepends() {
        return repositoryDepends;
    }

    public void setRepositoryDepends(String repository) {
        this.repositoryDepends = repository;
    }

    public String getRepositorySource() {
        return repositorySource;
    }

    public void setRepositorySource(String repositorySource) {
        this.repositorySource = repositorySource;
    }
}
