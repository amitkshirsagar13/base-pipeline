public restart(env) {
    stage('Reset Jenkins') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***        START RESET JENKINS      ***
        ***************************************
        """
        container('maven') {
            def url = "http://10.106.6.202:8080/safeRestart"
            sh """#!/bin/sh -e
            curl -s -X POST "${url}" --user "admin:admin"
            """
        }
        logs.info """
        ***************************************
        ***            FINISH RESET         ***
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