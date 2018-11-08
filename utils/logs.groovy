@NonCPS
public debug(msg) {
    try {
            echo "\033[1;34m[Debug] \033[0m ${msg}"
    } catch (e) {
        error "${e}"
    }
}

public info(msg) {
    ansiColor('xterm') {
        echo "\033[1;32m${msg}\033[0m"
    }
}
 
public error(msg) {
    ansiColor('xterm') {
        echo "\033[1;31m[Error] \033[0m ${msg}"
    }
}
 
public warning(msg) {
    ansiColor('xterm') {
        echo "\033[1;33m[Warning] \033[0m ${msg}"
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

return this