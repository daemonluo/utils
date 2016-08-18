package utils

package object cfg {
    private val cfgMap = collection.mutable.Map[String, Config]()

    def load(file: String, force: Boolean = false):  Config = {
        if(!utils.path.exists(file) || !utils.path.isFile(file)){
            throw new RuntimeException("no such file " + file)
        }
        if(!force && cfgMap.contains(file.toString)){
            return cfgMap(file.toString)
        }
        println("load " + file)
        val config = new Config(file){
            load()
            parse()
        }
        cfgMap(file.toString) = config
        return config
    }
}
