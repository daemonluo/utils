package utils

package object mysql {
    val DEFAULT_HOST = "127.0.0.1"
    val DEFAULT_PORT = 3306
    val DEFAULT_USER = "root"
    val DEFAULT_PASS = ""
    val DEFAULT_DB = ""
    val IDENTIFIER_QUOTE_STRING = "`"//com.mysql.jdbc.DatabaseMetaData.getIdentifierQuoteString()

    val ON_DUPLICATE_IGNORE = 1
    val ON_DUPLICATE_UPDATE = 2

    def connect(host: String, port: Int, username: String, password: String, db: String): Connection = {
        new Connection(host, port, username, password, db)
    }

    def connect(url: String, username: String, password: String): Connection = {
        new Connection(url, username, password)
    }

    def quoteColumn(column: String): String = IDENTIFIER_QUOTE_STRING + column + IDENTIFIER_QUOTE_STRING

    def quoteTable(table: String): String = IDENTIFIER_QUOTE_STRING + table + IDENTIFIER_QUOTE_STRING

    def quoteDatabase(database: String): String = IDENTIFIER_QUOTE_STRING + database + IDENTIFIER_QUOTE_STRING
}
