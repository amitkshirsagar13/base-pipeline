public build(env) {
    stage('Build Gradle') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***          START GRADLE           ***
        ***************************************
        """

        logs.debug """
            Working on GRADLE Build
        """

        container('gradle') {
            sh "gradle build"
        }

        logs.info """
        ***************************************
        ***         FINISH GRADLE           ***
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