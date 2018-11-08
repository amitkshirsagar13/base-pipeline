#!groovy


import jenkins.model.*
import hudson.security.*
import jenkins.security.s2m.AdminWhitelistRule
import groovy.json.JsonSlurperClassic

public build(env){
    stage('Build users') {
        def onlineUserList = getUsersFromGit()
        buildUsers(onlineUserList)
    }
}

@NonCPS
public buildUsers(onlineUserList){

    def instance = Jenkins.getInstance()

    def userList = new JsonSlurperClassic().parseText(onlineUserList)
    def hudsonRealm = new HudsonPrivateSecurityRealm(false)
    userList.users.each{
        def user = it.user.name
        def pass = it.user.password
        println "Creating user " + user + "..."
        hudsonRealm.createAccount(user, pass)
        println "Security User " + user	 + " was created"
    }
    instance.setSecurityRealm(hudsonRealm)
        
    def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
    instance.setAuthorizationStrategy(strategy)
    instance.save()
    Jenkins.instance.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false)
}

def getUsersFromGit(){
    withCredentials([[$class: 'UsernamePasswordMultiBinding',
    credentialsId: 'user-builder',
    usernameVariable: 'SECRET_USER',
    passwordVariable: 'SECRET_PASSWORD']]) {
        println "Accessing Git users with: ${SECRET_USER}"
        return new URL ("https://raw.githubusercontent.com/amitkshirsagar13/kube-cicd/master/jenkins/config/users/jenkins-user.json").getText()
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