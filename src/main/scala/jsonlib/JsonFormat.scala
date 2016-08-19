package com.daemon.utils.jsonlib

import annotation.implicitNotFound
import scala.language.implicitConversions

/**
 * Provides the JSON deserialization for type T.
 */
@implicitNotFound(msg = "Cannot find JsonReader or JsonFormat type class for ${T}")
trait JsonReader[T] {
    def read(json: JsValue) : T
}

object JsonReader {
    implicit def func2Reader[T](f: JsValue => T): JsonReader[T] = new JsonReader[T] {
        def read(json: JsValue) = f(json)
    }
}

/**
 * Provides the JSON serialization for type T.
 */
@implicitNotFound(msg = "Cannot find JsonWriter or JsonFormat type class for ${T}")
trait JsonWriter[T] {
    def write(obj: T): JsValue
}

object JsonWriter {
    implicit def func2Writer[T](f: T => JsValue): JsonWriter[T] = new JsonWriter[T] {
        def write(obj: T) = f(obj)
    }
}

/**
 * Provides the JSON deserialization and serialization for type T.
 */
trait JsonFormat[T] extends JsonReader[T] with JsonWriter[T]

/**
 * A special JsonReader capable of reading a legal JSON root object, i.e. either a JSON array or a JSON object.
 */
@implicitNotFound(msg = "Cannot find RootJsonReader or RootJsonFormat type class for ${T}")
trait RootJsonReader[T] extends JsonReader[T]

/**
 * A special JsonWriter capable of writing a legal JSON root object, i.e. either a JSON array or a JSON object.
 */
@implicitNotFound(msg = "Cannot find RootJsonWriter or RootJsonFormat type class for ${T}")
trait RootJsonWriter[T] extends JsonWriter[T]

/**
 * A special JsonFormat signaling that the format produces a legal JSON root object, i.e. either a JSON array
 * or a JSON object.
 */
trait RootJsonFormat[T] extends JsonFormat[T] with RootJsonReader[T] with RootJsonWriter[T]
