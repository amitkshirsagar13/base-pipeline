public start(env) {
    def logs = require("utils/logs")
    logs.info """
    ***************************************
    ***            START APP            ***
    ***************************************
    """

    def git = require("git/git")
    git.buildGitEnv(env)
    
    
    def environment = require("environment/environment")
    environment.setEnv(env)
    
    if (  env['flag-feature-job-build'] != null 
        && "${env['flag-feature-job-build']}" == "yes" ) {
        def jobBuilder = require("pipe/jobs-builder")
        jobBuilder.buildJenkinsJobs(env)
    }

    def envs = require("utils/env")

    envs.printEnv(env)

    def build = require("build/build")
    def gradle = require("gradle/gradle")
    def sonar = require("sonar/sonar")
    def docker = require("docker/docker")
    def compose = require("docker/docker-compose")
    def helm = require("deploy/helm")
    //def k8s = require("k8s/k8s")

    if (  env['flag-feature-sonar-scan'] != null 
        && "${env['flag-feature-sonar-scan']}" == "yes" ) {
        sonar.scan(env)
    }

    if (  env['flag-feature-maven-build'] != null 
        && "${env['flag-feature-maven-build']}" == "yes" ) {
        build.buildMaven(env)
    }

    if (  env['flag-feature-gradle-build'] != null 
        && "${env['flag-feature-gradle-build']}" == "yes" ) {
        gradle.build(env)
    }
    
    if (  env['flag-feature-docker-build'] != null 
        && "${env['flag-feature-docker-build']}" == "yes" ) {
        docker.buildDocker(env)
    }
    
    if (  env['flag-feature-docker-compose-build'] != null 
        && "${env['flag-feature-docker-compose-build']}" == "yes" ) {
        compose.buildDocker(env)
    }

    // if (  env['flag-feature-k8s-eval'] != null 
    //     && "${env['flag-feature-k8s-eval']}" == "yes" ) {
    //     k8s.eval(env)
    // }

    if (  env['flag-feature-helm-deploy'] != null 
        && "${env['flag-feature-helm-deploy']}" == "yes" ) {
        helm.deployChart(env)
    }

    logs.info """
    ***************************************
    ***           FINISH APP            ***
    ***************************************
    """
}

def require(moduleName) {
    def branch = "master"
    if ( "${env['flag-feature-toggling']}" == "yes" ) {
        if ( env['flag-feature-toggling-branch'] != null ) {
            branch = "${env['flag-feature-toggling-branch']}"
        }
    }

    container('maven') {
        def url = "https://raw.githubusercontent.com/amitkshirsagar13/base-pipeline/${branch}/${moduleName}.groovy"
        sh """#!/bin/sh -e
        curl -s -o ./pipeline.base.groovy "${url}"
        """
        def func = load("./pipeline.base.groovy")
        return func
    }
}

return this;
