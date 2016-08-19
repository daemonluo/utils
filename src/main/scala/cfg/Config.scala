package com.daemon.utils.cfg

import com.daemon.utils.util
import com.daemon.utils.NumbericString
import com.daemon.utils.jsonlib

protected class Config(url: java.net.URL){
    private var lines = collection.mutable.ListBuffer.empty[String]
    private val kvs = collection.mutable.Map[String, Map[String, String]]()

    private def isComment(line: String): Boolean = line.startsWith(";") || line.startsWith("#")

    private def isSection(line: String): Boolean = line.startsWith("[") && line.endsWith("]")

    def load() = {
        val source = io.Source.fromURL(url)
        var lineContinue = ""
        source.getLines.foreach{ line => {
            var str = ""
            if(line.endsWith("\\")){
                lineContinue += line.slice(0, line.length - 1)
            }else{
                str = (lineContinue + line).trim()
                lineContinue = ""
            }
            if(!isComment(str) && str != ""){
                lines += str.trim()
            }
        }}
    }

    def display(pretty: Boolean = true) = {
        import jsonlib._
        import jsonlib.DefaultJsonProtocol._
        val jst = (Map[String, Map[String, String]]() ++ kvs).toJson
        if(pretty){
            util.log(jst.prettyPrint)
        }else{
            util.log(jst.compactPrint)
        }
    }

    def parse() = {
        var sectionName = ""
        var sectionValues = collection.mutable.Map[String, String]()
        lines.foreach( line => {
            if(isSection(line)){
                if(sectionName != ""){
                    kvs(sectionName) = Map[String, String]() ++ sectionValues
                }
                sectionValues = collection.mutable.Map[String, String]()
                sectionName = line.slice(1, line.length - 1)
            }else{
                if(line.contains("=")){
                    val Array(key, value) = line.split("=", 2)
                    sectionValues(key.trim) = value.trim
                }
            }
        })
        if(sectionName != ""){
            kvs(sectionName) = Map[String, String]() ++ sectionValues
        }
    }

    def getSection(section: String): Option[Map[String, String]] = {
        kvs.get(section)
    }

    def getItem(section: String, item: String): Option[String] = {
        if(!kvs.contains(section)){
            return None
        }
        val items = kvs(section)
        if(items.contains(item)){
            Some(items(item))
        }else{
            None
        }
    }

    def getStr(section: String, item: String): Option[String] = {
        getItem(section, item)
    }

    def getInt(section: String, item: String): Option[Int] = {
        val raw = getItem(section, item)
        import NumbericString.str2Numberic
        if(raw.isDefined && raw.get.isInteger){ Some(raw.get.toInt) } else None
    }

    def getLong(section: String, item: String): Option[Long] = {
        val raw = getItem(section, item)
        import NumbericString.str2Numberic
        if(raw.isDefined && raw.get.isLong){
            if(raw.get.endsWith("l") || raw.get.endsWith("L")){
                Some(raw.get.slice(0, raw.get.length - 1).toLong)
            }else{
                Some(raw.get.toLong)
            }
        } else {
            None
        }
    }

    def getDouble(section: String, item: String): Option[Double] = {
        val raw = getItem(section, item)
        import NumbericString.str2Numberic
        if(raw.isDefined && raw.get.isDouble){
            Some(raw.get.toDouble)
        } else {
            None
        }
    }

    def getList(section: String, item: String, delimiter: String = ","): Option[List[String]] = {
        val raw = getItem(section, item)
        if(raw.isDefined){
            Some(raw.get.split(delimiter).toList)
        }else{
            None
        }
    }
}
