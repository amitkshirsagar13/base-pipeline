public buildDocker(env) {
    stage('Build Docker-Compose') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***      START DOCKER-COMPOSE       ***
        ***************************************
        """

        logs.debug """
            Working on Docker Compose Build
        """
        
        container('docker') {
            withCredentials([[$class: 'UsernamePasswordMultiBinding',
            credentialsId: 'docker-hub-credentials',
            usernameVariable: 'DOCKER_HUB_USER',
            passwordVariable: 'DOCKER_HUB_PASSWORD']]) {
                sh """
                    docker login -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PASSWORD}
                    docker-compose build
                    docker-compose push
                    // docker-compose push amitkshirsagar13/${env['application']}:${env['shortGitCommit']}
                """
            }
        }
    

        logs.info """
        ***************************************
        ***      FINISH DOCKER-COMPOSE      ***
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