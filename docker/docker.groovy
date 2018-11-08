public buildDocker(env) {
    stage('Build Docker') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***         START DOCKER            ***
        ***************************************
        """

        logs.debug """
            Working on Docker Build
        """
        
        container('docker') {
            withCredentials([[$class: 'UsernamePasswordMultiBinding',
            credentialsId: 'docker-hub-credentials',
            usernameVariable: 'DOCKER_HUB_USER',
            passwordVariable: 'DOCKER_HUB_PASSWORD']]) {
                sh """
                    docker login -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PASSWORD}
                    docker build -t amitkshirsagar13/${env['application']}:${env['shortGitCommit']} -t amitkshirsagar13/${env['application']}:latest .
                    docker push amitkshirsagar13/${env['application']}:${env['shortGitCommit']}
                    docker push amitkshirsagar13/${env['application']}:latest
                """
            }
        }
    

        logs.info """
        ***************************************
        ***          FINISH DOCKER          ***
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