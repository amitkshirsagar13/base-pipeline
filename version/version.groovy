def version(version) {
    def jsonSlurper = new JsonSlurper()
    def builder = new JsonBuilder()
    builder.version = version
    new File("./version.json").write(builder.toPrettyString())
    jsonSlurper = null
    jsondata = null
    builder = null
}

