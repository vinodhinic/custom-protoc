package com.foo.proto.implementor

import com.foo.proto.codegen.FooOptions
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Timestamp
import com.google.protobuf.compiler.PluginProtos

class EventTimeFieldImplementor : KafkaEventFieldImplementor(FooOptions.KafkaEventField.KE_EVENT_TIME) {
    override fun generateImplementation(fileName: String, protoPackage: String, messageName: String, fieldDescriptor: DescriptorProtos.FieldDescriptorProto): List<PluginProtos.CodeGeneratorResponse.File> {
        val javaMethodNameOfKeyField = fieldDescriptor.getterMethodName()
        val templateContext = mapOf("eventTimeFieldAccessor" to javaMethodNameOfKeyField,
                "newJavaEventTimeFieldName" to "ke" + fieldDescriptor.javaName().capitalizeFirstLetter())
        val content = executeTemplate(supportedFieldTypeToTemplateMap[fieldDescriptor.getProtoType()]!!, templateContext)
        return listOf(PluginProtos.CodeGeneratorResponse.File.newBuilder()
                .setName(fileName)
                .setContent(content)
                .setInsertionPoint(getClassScopeInsertionPoint(protoPackage, messageName))
                .build())
    }

    override val supportedFieldTypeToTemplateMap: Map<String, String> = mapOf(
            Timestamp.getDescriptor().fullName to "$templateBasePath/eventTime_instant.mustache"
    )
}
