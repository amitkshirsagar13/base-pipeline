

public buildGitEnv(env) {
    stage('Git Branch Fetch') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***          START GIT ENV          ***
        ***************************************
        """

        logs.debug """
            Setting Git Branch Environment
        """

        def proRepo = checkout scm
        env.gitCommit = proRepo.GIT_COMMIT
        env.gitBranch = proRepo.GIT_BRANCH.split('/')[1]
        env.shortGitCommit = "${gitCommit[0..10]}"

        logs.info """
        ***************************************
        ***          FINISH GIT ENV         ***
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