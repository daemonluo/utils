package utils;

import java.io.{File}
import java.nio.file.{Files,Path,Paths}

object path {
    def mkdir(dir: String, parents: Boolean = false) : Boolean = {
        mkdir(Paths.get(dir), parents)
    }

    def mkdir(dir: Path, parents: Boolean) : Boolean = {
        if(parents){
            val arr = dir.toString.split(File.separator)
            val parent = Paths.get(join(arr.slice(0, arr.length - 1): _*))
            if(!Files.exists(parent)){
                mkdir(parent, parents)
            }
        }
        dir.toFile.mkdir()
    }

    def rmdir(dir: String, recursive: Boolean = false) : Boolean = {
        rmdir(Paths.get(dir), recursive)
    }

    def rmdir(dir: Path, recursive: Boolean) : Boolean = {
        if(!Files.exists(dir)){
            throw new RuntimeException("no such file " + dir)
        }
        val file = new File(dir.toString)
        if(recursive){
            listDir(dir, false).foreach( rmdir(_, recursive=true))
        }
        file.delete()
    }

    def createTempDir(path: String = "/tmp", prefix: Array[String] = Array(), parents: Boolean = false) : Option[String] = {
        val pid = util.getPID()
        val stamp = System.currentTimeMillis()
        val name = util.md5sum(prefix, pid, stamp)
        val dir = join(path, name)
        val flag = mkdir(dir, parents=parents)
        if(flag){
            return Some(dir)
        }else{
            return None
        }
    }

    def createTempFile(suffix: String, path: String = "/tmp", prefix: Array[String] = Array()) : String = {
        val pid = util.getPID()
        val stamp = System.currentTimeMillis()
        val name = "%s.%s".format(util.md5sum(prefix, pid, stamp), suffix)
        join(path, name)
    }

    def listDir(dir:Path, recursive: Boolean) : Array[String] = {
        if(Files.isDirectory(dir)){
            val list = dir.toFile.list.map(join(dir.toString, _))
            if(recursive){
                list.foldLeft(Array[String]())((accum, d) => {
                    if(Files.isDirectory(Paths.get(d))){
                        accum ++ Array(d) ++ listDir(d, true)
                    }else{
                        accum ++ Array(d)
                    }
                })
            }else{
                return list
            }
        }else{
            return Array(dir.toString)
        }
    }

    def listDir(dir:String, recursive: Boolean = false) : Array[String] = {
        listDir(Paths.get(dir), recursive)
    }

    def join(args:String*) : String = {
        val separator = File.separator
        args.map( arg => if(arg.endsWith(separator)){arg.dropRight(1)}else{arg} ).mkString(separator)
    }

    def exists(path: String) : Boolean = {
        Files.exists(Paths.get(path))
    }

    def isFile(path: String) : Boolean = {
        Files.isRegularFile(Paths.get(path))
    }

    def isDir(path: String) : Boolean = {
        Files.isDirectory(Paths.get(path))
    }

    /*
     * todo
     */
    def absolutePath(path: String) : Unit = {
    }

    /*
     * todo
     */
    def realPath(path: String) : Unit = {
    }
}
