package com.daemon.utils

import com.daemon.utils
import java.nio.file.{Files,Paths}

package cfg {
    class ResourceNotFoundException(msg: String) extends java.io.FileNotFoundException(msg)
}

package object cfg {
    private val cfgMap = collection.mutable.Map[String, Config]()

    def load(url: java.net.URL, force: Boolean = false):  Config = {
        if(url == null){
            throw new ResourceNotFoundException("no such resource " + url)
        }
        if(!force && cfgMap.contains(url.toString)){
            return cfgMap(url.toString)
        }
        val config = new Config(url){
            load()
            parse()
        }
        cfgMap(url.toString) = config
        return config
    }

    def loadFromPath(path: java.nio.file.Path, force: Boolean = false): Config = {
        if(!Files.exists(path) || !Files.isRegularFile(path)){
            throw new ResourceNotFoundException("no such file " + path.toString)
        }
        val url = path.toUri.toURL
        load(url, force)
    }

    def loadFromFile(file: String, force: Boolean = false): Config = {
        val path = Paths.get(file)
        loadFromPath(path, force)
    }
}
