import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK
import groovy.json.JsonSlurper
import hudson.EnvVars
import hudson.model.*

def setProperty(env, key, value) {
    env["${key}"]="${value}"
}

public setEnv(env) {
    def log = require("utils/logs")

    log.debug "*** SET ENV"
    def vv = pullVaultParameters(env)

    for (prop in vv) {
        def key="${prop.key}"
        def value="${prop.value}"
        setProperty(env, key,value)
    }

    log.debug "****************** ENV **********************"
    sh "env"
    log.debug "*********************************************"

}

public envsubst(env) {
    stage('k8s cluster eval') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***          START K8S EVAL         ***
        ***************************************
        """
        logs.debug """
            Working on k8s cluster evaluation
        """
        container('alpine-utils') {
            sh """
                envsubst < k8s-deploy.yml > eval-k8s.yml
                envsubst < kubecfg > eval-kubecfg.yml
                cat eval-k8s.yml
            """
        }
        logs.info """
        ***************************************
        ***        FINISH  K8S EVAL         ***
        ***************************************
        """
    }
}

public pullVaultParameters(env){
    def log = require("utils/logs")
    def urls = [""]
    withCredentials([[$class: 'UsernamePasswordMultiBinding',
            credentialsId: 'github',
            usernameVariable: 'GITHUB_USER',
            passwordVariable: 'GITHUB_PASSWORD']]) {
            urls = [
                    "https://${GITHUB_USER}:${GITHUB_PASSWORD}@github.com/raw/${env['git-project-name']}/vault/${env['vault-branch']}/default.yml",
                    "https://${GITHUB_USER}:${GITHUB_PASSWORD}@github.com/raw/${env['git-project-name']}/vault/${env['vault-branch']}/default-${env['vault-environment']}.yml",
                    "https://${GITHUB_USER}:${GITHUB_PASSWORD}@github.com/raw/${env['git-project-name']}/vault/${env['vault-branch']}/${env['vault-configuration']}.yml",
                    "https://${GITHUB_USER}:${GITHUB_PASSWORD}@github.com/raw/${env['git-project-name']}/vault/${env['vault-branch']}/${env['vault-configuration']}-${env['vault-environment']}.yml",
                    "https://${GITHUB_USER}:${GITHUB_PASSWORD}@github.com/raw/${env['git-project-name']}/${env['git-repository-name']}/${env['git-repository-branch']}/default.yml"
                ]
    }


    def vv = [:]
    for (ii = 0; ii < urls.size(); ii++) {
        def url=urls[ii]
        try{
            container('jenkins-k8s-job-builder') {
                sh """
                    rm vault.json||true &>/dev/null
                    STATUSCODE=\$(curl -s -o ./vaults.yml --write-out "%{http_code}" ${url})||true &>/dev/null
                    echo "CODE: \$STATUSCODE" 
                    if test \$STATUSCODE -ne 200; then
                        echo "FILE DOESN EXISTS"
                    else
                        yaml2json vaults.yml > vault.json||true &>/dev/null
                    fi
                """
            }
            def data = readFile "vault.json"
            log.debug data

            def jsonSlurper1 = new JsonSlurper()
            def props = jsonSlurper1.parseText(data)

            for (pp  in props) {
                def module = pp.value
                if (module == null) {
                    module = [:]
                } 

                for (prop in module) {
                    def propA=prop.split("=")
                    def key="${propA[0]}"
                    def value=""
                    if (propA.size()>1) {
                        value = "${propA[1]}"
                    }
                    vv["${key}"]="${value}";
                }
            }

            jsonSlurper = null
            jsonObject = null

            log.debug "==========="
        } catch (e) {
            //log.error "Error reading ${url} ${e}"

        }

    }

    return vv;
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
