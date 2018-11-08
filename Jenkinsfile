def label = "jenkins-slave-${UUID.randomUUID().toString()}"

podTemplate(label: label, containers: [
  containerTemplate(name: 'maven', image: 'maven', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),  
  containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:latest', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:latest', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'jenkins-k8s-job-builder', image: 'amitkshirsagar13/jenkins-k8s-job-builder:latest', 
  workingDir: '/home/jenkins', command: 'cat', ttyEnabled: true)
],envVars: [
  envVar(key: 'BUILD_NUMBER', value: env.BUILD_NUMBER)
],
volumes: [
    //configMapVolume(mountPath: '/etc/jenkins_jobs/jenkins_jobs.ini', subPath: 'jenkins_jobs.ini', configMapName: 'jenkins-ini'),
    configMapVolume(mountPath: '/etc/jenkins_jobs', configMapName: 'jenkins-ini'),
	hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
	persistentVolumeClaim(mountPath: '/root/.m2/repository', claimName: 'jenkins-persistent-repository-storage-claim', readOnly: false)
]) {
    stage("Assign Node") {
        node(label) {
            def pipe= require("pipe/app")
            pipe.start(env)
        }
    }
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