package com.daemon.utils

import scala.language.implicitConversions

package jsonlib {
    case class DeserializationException(msg: String, cause: Throwable = null, fieldNames: List[String] = Nil) extends RuntimeException(msg, cause)
    class SerializationException(msg: String) extends RuntimeException(msg)

    private[jsonlib] class PimpedAny[T](any: T){
        def toJson(implicit writer: JsonWriter[T]): JsValue = writer.write(any)
    }

    private[jsonlib] class PimpedString(string: String) {
        def parseJson: JsValue = JsonParser(string)
    }
}

package object jsonlib {
    type JsField = (String, JsValue)

    def deserializationError(msg: String, cause: Throwable = null, fieldNames: List[String] = Nil) = throw new DeserializationException(msg, cause, fieldNames)

    def serializationError(msg: String) = throw new SerializationException(msg)

    def jsonReader[T](implicit reader: JsonReader[T]) = reader
    def jsonWriter[T](implicit writer: JsonWriter[T]) = writer
  
    implicit def pimpAny[T](any: T) = new PimpedAny(any)
    implicit def pimpString(string: String) = new PimpedString(string)

    def decode[T : JsonReader](data: String): T = {
        val jst = data.parseJson
        jst.convertTo[T]
    }
}
