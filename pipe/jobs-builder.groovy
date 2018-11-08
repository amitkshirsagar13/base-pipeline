public buildJenkinsJobs(env) {
    stage('Build Jenkins Jobs') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***     START Jenkins Job Build     ***
        ***************************************
        """

        logs.debug """
            Building Jenkins Jobs
        """

        def repository = "${env['git-repository']}"
        def target_env = "${env['target-environment']}"
        
        def jobs_config = "https://raw.githubusercontent.com/${repository}/base-pipeline/master/config/pipeline.jobs.template.yml"
        
        container('jenkins-k8s-job-builder') {
            sh """#!/bin/sh
                echo ${jobs_config}
                curl -s -o ./pipeline.jobs.template.yml "${jobs_config}"
                cat kube-job-pipeline.yml >> pipeline.jobs.template.yml
                cat pipeline.jobs.template.yml
                jenkins-jobs --conf /etc/jenkins_jobs/jenkins_jobs.ini update pipeline.jobs.template.yml:defaults/${target_env}.yml
                #jenkins-jobs --conf /etc/jenkins_jobs/jenkins_jobs.ini update pipeline.jobs.template.yml
            """
        }

        logs.info """
        ***************************************
        ***    FINISH Jenkins Job Build     ***
        ***************************************
        """
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

return this;