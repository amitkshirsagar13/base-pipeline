public deployChart(env) {
    stage('Deploy Helm Chart') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***         START HELM CHART        ***
        ***************************************
        """

        logs.debug """
            Working on Docker Build
        """
        
        container('helm') {
            echo "Project: ${env['project']} | Application: ${env['application']} | tag: ${env['shortGitCommit']}"
            container('helm') {
                sh "helm upgrade --install ${env['application']} --namespace ${env['gitBranch']} ./cicd/charts/ --set profile=${env['profile']} --set branch=${env['gitBranch']} --set commit=${env['shortGitCommit']} --set application=${env['application']}"
            }
        }


        logs.info """
        ***************************************
        ***        FINISH  HELM CHART       ***
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