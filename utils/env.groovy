public printEnv(env) {
    stage('Print Env') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***         START PRINT ENV         ***
        ***************************************
        """

        logs.debug """
            Printing ENV
        """

        env.getEnvironment().each { name, value -> println "Name: $name -> Value $value" }

        logs.info """
        ***************************************
        ***            FINISH ENV           ***
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