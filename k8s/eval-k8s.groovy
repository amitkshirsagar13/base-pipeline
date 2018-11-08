@Grab('org.yaml:snakeyaml:1.17')

import org.yaml.snakeyaml.Yaml
Yaml yml = new Yaml()
def envYml = yml.load(new File("vault.yml").newDataInputStream())

envYml.vault.DEV.config.each{it.each{key,value -> println "${key}:${value}"}}