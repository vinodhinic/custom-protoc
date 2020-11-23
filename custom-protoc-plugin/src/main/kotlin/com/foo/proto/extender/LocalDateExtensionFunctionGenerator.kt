package com.foo.proto.extender

import com.foo.types.LocalDateProto
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.compiler.PluginProtos

class LocalDateExtensionFunctionGenerator : ExtensionFunctionGenerator {

    override fun handleMessage(
            messageTypeDescriptor: DescriptorProtos.DescriptorProto,
            javaFileName: String,
            protoPackage: String,
            protoFileName: String
    ):
            List<PluginProtos.CodeGeneratorResponse.File>? {
        if (messageTypeDescriptor.name == LocalDateProto.LocalDate.getDescriptor().fullName) {
            val templateResource = "localdate-extensions.mustache"
            val codeBlock = executeTemplate(templateResource)
            return listOf(PluginProtos.CodeGeneratorResponse.File.newBuilder()
                    .setName(javaFileName)
                    .setContent(codeBlock)
                    .setInsertionPoint(getClassScopeInsertionPoint(protoPackage, messageTypeDescriptor.name))
                    .build(),
                    PluginProtos.CodeGeneratorResponse.File.newBuilder()
                            .setName(javaFileName)
                            .setContent(codeBlock)
                            .setInsertionPoint(getOuterClassScopeInsertionPoint())
                            .build()
            )
        } else {
            return null
        }
    }
}
