package com.daemon.utils.jsonlib

trait DefaultJsonProtocol extends BasicFormats with StandardFormats with CollectionFormats with ProductFormats with AdditionalFormats

object DefaultJsonProtocol extends DefaultJsonProtocol
