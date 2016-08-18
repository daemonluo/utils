package utils.mysql

import java.sql.DriverManager

class Connection(url: String, username: String, password: String){
    lazy val conn = connect

    def this(host: String, port: Int, username: String, password: String, db: String) = {
        this(s"jdbc:mysql://$host:$port/$db", username, password)
    }

    def connect(): java.sql.Connection = {
        println(ON_DUPLICATE_IGNORE)
        val driver = "com.mysql.jdbc.Driver"
        Class.forName(driver)
        DriverManager.getConnection(url, username, password)
    }

    def currentDB(): String = {
        val statement = conn.createStatement()
        try{
            val rs = statement.executeQuery("select database() as db")
            rs.next()
            val db = rs.getString("db")
            rs.close()
            db
        }finally{
            statement.close()
        }
    }

    def selectDB(db: String): Int = {
        val statement = conn.createStatement()
        try{
            statement.executeUpdate(s"use $db")
        }finally{
            statement.close()
        }
    }

    def databases(): List[String] = {
        val statement = conn.createStatement()
        try{
            val rs = statement.executeQuery("show databases")
            var list = collection.mutable.ListBuffer.empty[String]
            while(rs.next()){
                list += rs.getString("Database")
            }
            List[String]() ++ list
        }finally{
            statement.close()
        }
    }

    def tables(db: Option[String] = None): List[String] = {
        val statement = conn.createStatement()
        try{
            var sql = "show tables"
            if(db.isDefined){
                sql += s" from ${db.get}"
            }
            val rs = statement.executeQuery(sql)
            var list = collection.mutable.ListBuffer.empty[String]
            while(rs.next()){
                list += rs.getString(1)
            }
            List[String]() ++ list
        }finally{
            statement.close()
        }
    }

    def schema(table: String): Map[String, Map[String, String]] = {
        val statement = conn.createStatement()
        try{
            val rs = statement.executeQuery(s"desc $table")
            var map = collection.mutable.Map.empty[String, Map[String, String]]
            val columns = List("Field", "Type", "Null", "Key", "Default", "Extra")
            while(rs.next()){
                var values = collection.mutable.ListBuffer.empty[String]
                1 to columns.size foreach { values += rs.getString(_) }
                val record = columns.zip(values).toMap
                map(record("Field")) = record
            }
            Map[String, Map[String, String]]() ++ map
        }finally{
            statement.close()
        }
    }

    def columns(table: String): List[String] = {
        val statement = conn.createStatement()
        try{
            val rs = statement.executeQuery(s"desc $table")
            val list = collection.mutable.ListBuffer.empty[String]
            while(rs.next()){
                list += rs.getString("Field")
            }
            List[String]() ++ list
        }finally{
            statement.close()
        }
    }

    def execute(sql: String, args: List[Any] = List.empty[Any]): Int = {
        val statement = conn.prepareStatement(sql)
        try{
            args.zipWithIndex.foreach(value =>{
                statement.setObject(value._2 + 1, value._1)
            })
            statement.executeUpdate()
        }finally{
            statement.close()
        }
    }

    def query[T](sql: String, args: List[Any] = List.empty[Any])(callback: java.sql.ResultSet => T): T = {
        val statement = conn.prepareStatement(sql)
        try{
            args.zipWithIndex.foreach(value => {
                statement.setObject(value._2 + 1, value._1)
            })
            val rs = statement.executeQuery()
            try{
                callback(rs)
            }finally{
                rs.close()
            }
        }finally{
            statement.close()
        }
    }

    def insert(table: String, record: Map[String, String], replace: Boolean = false, duplicate: Int = 0, updates: Option[Map[String, String]] = None): Int = {
        val columns = record.values.map(quoteColumn)
        var sql = ""
        var args = collection.mutable.ListBuffer(record.values.toList:_*)
        if(replace){
            sql += s"REPLACE INTO ${quoteTable(table)}(${record.keys.map(quoteColumn).mkString(",")}) VALUES(${record.values.map(_=>"?").mkString(",")})"
        }else{
            sql += "INSERT"
            if(duplicate == ON_DUPLICATE_IGNORE){
                sql += " IGNORE"
            }
            sql += s" INTO ${quoteTable(table)}(${record.keys.map(quoteColumn).mkString(",")}) VALUES(${record.values.map(_=>"?").mkString(",")})"
        }

        if(duplicate == ON_DUPLICATE_UPDATE && updates.isDefined){
            sql += " ON DUPLICATE KEY UPDATE"
            val sets = updates.get.collect(kv => kv match{
                case (k, v) if record.contains(k) =>
                    args += record(k)
                    if(v == "value"){
                        s"${quoteColumn(k)}=?"
                    }else if(v == "counter"){
                        s"${quoteColumn(k)}=${quoteColumn(k)}+?"
                    }
            })
            if(sets.size > 0){
                sql += " " + sets.mkString(",")
            }
        }
        execute(sql, List[String](args:_*))
    }

    def batchInsert(table: String, records:Seq[Seq[String]], columns: Seq[String], replace: Boolean = false, duplicate: Int = 0): Int = {
        val args = records.flatMap(_.map(v => v))
        var sql = ""
        if(replace){
            sql += "REPLACE"
        }else{
            sql = "INSERT"
            if(duplicate == ON_DUPLICATE_IGNORE){
                sql += " IGNORE"
            }
        }
        sql += s" INTO ${quoteTable(table)}(${columns.map(quoteColumn).mkString(",")}) VALUES ${records.map("(" + _.map(v => "?").mkString(",") + ")").mkString(",")}"
        execute(sql, List[String](args:_*))
    }

    def update(table: String, value: Map[String, String], condition: Option[Map[String, String]] = None): Int = {
        var args = collection.mutable.ListBuffer.empty[String]
        val sets = value.collect( kv => kv match {
            case (k, v) => 
                args += v
                s"${quoteColumn(k)}=?"
        })
        var sql = s"UPDATE ${quoteTable(table)} SET ${sets.mkString(",")}"
        if(condition.isDefined && condition.get.size > 0){
            val where = condition.get.collect(kv => kv match {
                case (k, v) =>
                    args += v
                    s"${quoteColumn(k)}=?"
            })
            sql += " WHERE " + where.mkString(" AND ")
        }
        execute(sql, List[String](args:_*))
    }

    def updateCounter(table: String, value: Map[String, String], condition: Option[Map[String, String]] = None): Int = {
        var args = collection.mutable.ListBuffer.empty[String]
        val sets = value.collect( kv => kv match {
            case (k, v) =>
                args += v
                s"${quoteColumn(k)}=${quoteColumn(k)}+?"
        })
        var sql = s"UPDATE ${quoteTable(table)} SET ${sets.mkString(",")}"
        if(condition.isDefined && condition.get.size > 0){
            val where = condition.get.collect(kv => kv match {
                case (k, v) =>
                    args += v
                    s"${quoteColumn(k)}=?"
            })
            sql += " WHERE " + where.mkString(" AND ")
        }
        execute(sql, List[String](args:_*))
    }

    def close() = {
        conn.close()
    }
}
