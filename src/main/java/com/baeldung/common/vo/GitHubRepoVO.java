package com.baeldung.common.vo;

public class GitHubRepoVO {
    private String repoName;
    private String repoUrl;
    private String repoLoalPath;
    private String repoMasterHttpPath;
          
    public GitHubRepoVO(String repoName, String repoUrl, String repoLoalPath, String repoMasterHttpPath) {
        super();
        this.repoName = repoName;
        this.repoUrl = repoUrl;
        this.repoLoalPath = repoLoalPath;
        this.repoMasterHttpPath = repoMasterHttpPath;
    }
    public String getRepoName() {
        return repoName;
    }
    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }
    public String getRepoUrl() {
        return repoUrl;
    }
    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }
    public String getRepoLoalPath() {
        return repoLoalPath;
    }
    public void setRepoLoalPath(String repoLoalPath) {
        this.repoLoalPath = repoLoalPath;
    }
    public String getRepoMasterHttpPath() {
        return repoMasterHttpPath;
    }
    public void setRepoMasterHttpPath(String repoMasterHttpPath) {
        this.repoMasterHttpPath = repoMasterHttpPath;
    }
    
}
