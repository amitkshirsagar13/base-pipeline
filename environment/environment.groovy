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

public eval(env) {
    stage('k8s cluster eval') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***          START K8S EVAL         ***
        ***************************************
        """

        def vaultEnv = "vault"
        if ( env['vault-environment'] != null ) {
            vaultEnv = "${env['vault-environment']}"
        }
        container('alpine-utils') {
            sh """
                set -o allexport
                source vault.yml
                source vault.${vaultEnv}.yml
                set +o allexport
                envsubst < k8s-deploy.yml > eval-k8s.yml

                env
            """
        }

        logs.debug """
            Working on k8s cluster evaluation
        """

        logs.info """
        ***************************************
        ***        FINISH  K8S EVAL         ***
        ***************************************
        """
    }
}


public pullVaultParameters(env){
    def log = require("utils/logs")
    log.debug "pullVaultParameters"
    def urls = [
        "https://raw.githubusercontent.com/amitkshirsagar13/vault/master/default.yml",
        "https://raw.githubusercontent.com/amitkshirsagar13/vault/master/default-DEV.yml"
    ]

    def vv = [:]
    for (ii = 0; ii < urls.size(); ii++) {
        def url=urls[ii]

        log.debug "${url}"
        try{
            container('alpine-utils') {
                sh """
                    rm vault.json||true
                    STATUSCODE=\$(curl -s -o ./vaults.yml --write-out "%{http_code}" ${url})||true
                    echo "CODE: \$STATUSCODE"
                    if test \$STATUSCODE -ne 200; then
                        echo "FILE DOESN EXISTS"
                    else
                        which yaml2json
                        ls -ltr
                        yaml2json vaults.yml > vault.json||true
                    fi
                """
            }
            def data = readFile "vault.json"
   
            def jsonSlurper1 = new JsonSlurper()
            def props = jsonSlurper1.parseText(data)

            log.debug props
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
        } catch (e) {
            log.error e
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
        def url = "https://github.com/raw/k8s-cicd/${branch}/k8s-cicd/${moduleName}.groovy"
        sh """#!/bin/sh -e
        curl -s -o ./pipeline.base.groovy "${url}"
        """
        def func = load("./pipeline.base.groovy")
        return func
    }
}

return this;
