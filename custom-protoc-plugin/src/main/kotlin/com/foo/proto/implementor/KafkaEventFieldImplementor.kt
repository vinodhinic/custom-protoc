package com.foo.proto.implementor

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.compiler.PluginProtos
import com.foo.proto.CodeGenUtils
import com.foo.proto.codegen.FooOptions

internal const val templateBasePath = "kafkaevent"

abstract class KafkaEventFieldImplementor(private val kafkaEventField: FooOptions.KafkaEventField) : CodeGenUtils {
  protected fun DescriptorProtos.FieldDescriptorProto.getProtoType() = getCurrentType(this)
  abstract fun generateImplementation(
    fileName: String,
    protoPackage: String,
    messageName: String,
    fieldDescriptor: DescriptorProtos.FieldDescriptorProto
  ): List<PluginProtos.CodeGeneratorResponse.File>

  abstract val supportedFieldTypeToTemplateMap: Map<String, String>

  fun validate(fileName: String, protoPackage: String, messageName: String, fieldDescriptor: DescriptorProtos.FieldDescriptorProto) {
    if (!supportedFieldTypeToTemplateMap.containsKey(fieldDescriptor.getProtoType())) {
      throw IllegalArgumentException("Proto Package : $protoPackage File : $fileName - File ${fieldDescriptor.name} is annotated with $kafkaEventField " +
          "but it is not of supported type ${supportedFieldTypeToTemplateMap.keys} $messageName")
    }
  }
}
