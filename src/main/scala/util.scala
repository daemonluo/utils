package com.daemon.utils;

import com.github.nscala_time.time.Imports._
import scala.io.Source
import java.nio.file.{Paths, Files}

object util {
    object Color extends Enumeration {
        val red, gre, yel, blu = Value
    }

    val colors = Map(
        "red" -> "\u001b[1;31m",
        "gre" -> "\u001b[1;32m",
        "yel" -> "\u001b[1;33m",
        "blu" -> "\u001b[1;34m",
        "end" -> "\u001b[0m"
    )
    def getPID() : Int = {
        val name = java.lang.management.ManagementFactory.getRuntimeMXBean().getName()
        name.split("@")(0).toInt
    }

    def md5sum(args:Any*) : String = {
        import com.roundeights.hasher._
        val hex = args.foldLeft(Algo.md5.foldable){ (accum, arg) => accum(arg.toString()) }.done.hex
        hex
    }
    def sha1sum(filepath: String) : String = {
        import com.roundeights.hasher._
        val path = Paths.get(filepath)
        if(!Files.exists(path) || !Files.isRegularFile(path)){
            throw new RuntimeException("no such file " + filepath)
        }
        val source = Algo.sha1.tap(Source.fromFile(filepath))
        source.mkString
        val hash = source.hash
        hash.hex
    }

    def parseDate(date: String) : String = {
        val re = """\d+""".r
        val it = re.findAllIn(date)
        val arr = it.map(_.toInt).toList
        val len = arr.length
        var (year: Int, month: Int, day: Int) = (0, 0, 0)
        if(len == 1){
            day = arr(0)
            val dt = DateTime.now
            year = dt.year.get()
            month = dt.month.get()
        }else if(len == 2){
            month = arr(0)
            day = arr(1)
            val dt = DateTime.now
            year = dt.year.get()
        }else if(len == 3){
            val List(year, month, day) = arr
        }else{
            val dt = DateTime.now
            year = dt.year.get()
            month = dt.month.get()
            day = dt.day.get()
        }
        val dateStr = f"$year%04d-$month%02d-$day%02d"
        return dateStr
    }
    
    def timedelta(delta: Int) : Tuple4[Int, Int, Int, Int] = {
        var d = delta
        val l = List(86400, 3600, 60).map( v => {
            val r = d/v
            d = d%v
            r
        })
        val List(days, hours, minutes) = l
        val seconds = d
        (days, hours, minutes, seconds)
    }

    def timedelta(delta: Int, format:Boolean) : String = {
        val (days, hours, minutes, seconds) = timedelta(delta)
        val l = (List(days, hours, minutes, seconds), List("day", "hour", "minute", "second")).zipped.map((amount, unit) => {
            amount match {
                case 0 => None
                case 1 => Some(s"$amount $unit")
                case _ => Some(s"$amount $unit" + "s")
            }
        })
        l.filter(_.isDefined).map(_.get).mkString(" ")
    }

    def countLines(file: String) : Int = {
        if(!path.exists(file) || !path.isFile(file)){
            throw new RuntimeException("no such file " + file)
        }
        val src = Source.fromFile(file)
        try{
            src.getLines.size
        }finally{
            src.close()
        }
    }

    def isChinese(char: Char) : Boolean = {
        val value = char.toInt
        value >= 19968 && value <= 171941
    }

    def chinseStringLength(str: String) : Int = {
        str.toList.foldLeft(0)((sum, c) => {sum + (if(isChinese(c)){2}else{1})})
    }

    def formatTable(rows: List[List[String]], headLine: Boolean = true, pad: String = " ") : List[String] = {
        if(rows.length == 0){
            return List()
        }
        val first = rows(0)
        val columnWidh = collection.mutable.Map[Int, Int]()
        rows.foreach(row => {
            row.zipWithIndex.foreach(p => {
                val w = chinseStringLength(p._1)
                val k = p._2
                columnWidh.update(k, w max columnWidh.getOrElse(k, w))
            })
        })
        def fillPad(str: String, pad: String, width: Int) : String = {
            val cn = chinseStringLength(str) - str.length
            str + pad * (width - cn - str.length)
        }
        val start = "| "
        val end = " |"
        val separator = " | "
        val outputs = rows.map(row => {row.zipWithIndex.map(p => fillPad(p._1, pad, columnWidh(p._2))).mkString("| ", " | ", " |")})
        if(headLine){
            val middle = Range(0, columnWidh.size).map(k => fillPad("", "-", columnWidh(k))).mkString("+-", "-+-", "-+")
            List(outputs(0), middle) ++ outputs.slice(1, outputs.length)
        }else{
            return outputs
        }
    }

    def renderMsgWithColor(msg: String, color: Color.Value): String = {
        colors(color.toString) + msg + colors("end")
    }

    def printWithColor(msg: String, color:Color.Value) = {
        println(renderMsgWithColor(msg, color))
    }

    def log(msg: String) = {
        printWithColor(msg, Color.gre)
    }

    def warn(msg: String) = {
        System.err.println(renderMsgWithColor(msg, Color.yel))
    }

    def error(msg: String) = {
        System.err.println(renderMsgWithColor(msg, Color.red))
    }
}
