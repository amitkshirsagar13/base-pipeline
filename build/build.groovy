public buildMaven(env) {
    stage('Build Maven') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***          START MAVEN            ***
        ***************************************
        """

        logs.debug """
            Working on Maven Build
        """

        container('maven') {
            sh "mvn -Dmaven.test.skip=true clean install"
        }

        logs.info """
        ***************************************
        ***          FINISH MAVEN           ***
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