public scan(env) {
    stage('SonarQube Scan') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***       START SONAR ANALYSIS      ***
        ***************************************
        """

        logs.debug """
            Working on Sonar Analysis
        """
        
        if (  env['flag-feature-maven-build'] != null 
            && "${env['flag-feature-maven-build']}" == "yes" ) {
            container('maven') {
                withSonarQubeEnv('sonarqube') {
                    sh 'mvn clean package sonar:sonar'
                }
            }
        }
        if (  env['flag-feature-gradle-build'] != null 
            && "${env['flag-feature-gradle-build']}" == "yes" ) {
            container('gradle') {
                withSonarQubeEnv('sonarqube') {
                    sh 'gradle sonarqube'
                }
            }
        }

        logs.info """
        ***************************************
        ***       FINISH  SONAR CHART       ***
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