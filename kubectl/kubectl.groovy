public deployKubernetes(env) {
    stage('Deploy Kubernetes Deployment') {
        def logs = require("utils/logs")
        logs.info """
        ***************************************
        ***       START KUBECTL DEPLOY      ***
        ***************************************
        """

        logs.debug """
            Working on Kubectl Deployment
        """
        container('kubectl') {
           
            sh """
                kubectl --kubeconfig=./eval-kubecfg.yml cluster-info
                kubectl -n ${env['NAMESPACE']} get deployments --selector=image=${env['DOCKER_IMAGE_NAME']} -o jsonpath='{.items[*].kind}' || true
                kubectl -n ${env['NAMESPACE']} delete deployments --selector=image=${env['DOCKER_IMAGE_NAME']} || true

                kubectl --kubeconfig=./eval-kubecfg.yml apply -f eval-k8s.yml
            """
        }


        logs.info """
        ***************************************
        ***      FINISH KUBECTL DEPLOY      ***
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
