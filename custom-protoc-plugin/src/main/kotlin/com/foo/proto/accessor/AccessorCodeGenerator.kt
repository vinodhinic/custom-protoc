package com.foo.proto.accessor

import com.foo.proto.CodeGenerator
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.compiler.PluginProtos

private const val basePath = "accessor"

interface AccessorCodeGenerator : CodeGenerator {

    override fun generateFiles(request: PluginProtos.CodeGeneratorRequest): List<PluginProtos.CodeGeneratorResponse.File> {
        val protoFileList = request.protoFileList
        val responseFiles: MutableList<PluginProtos.CodeGeneratorResponse.File> = ArrayList()
        for (fileDescriptor in protoFileList) {
            var outerClassName = if (fileDescriptor.options.hasJavaOuterClassname()) {
                fileDescriptor.options.javaOuterClassname
            } else {
                getProtoFileNameWithoutDotProtoExtension(fileDescriptor).convertToJavaClassName()
            }

            if (areThereAnyMessageWithSameNameAsOuterClassName(outerClassName, fileDescriptor)) {
                // if the proto file and the message inside this .proto file are same, then the class name is generated with "OuterClass" appended to it.
                outerClassName += "OuterClass"
            }

            val packageName = if (fileDescriptor.options.hasJavaPackage()) {
                fileDescriptor.options.javaPackage
            } else {
                fileDescriptor.getPackage()
            }

            val hasJavaMultipleFiles = fileDescriptor.options.hasJavaMultipleFiles()
            for (messageTypeDescriptor in fileDescriptor.messageTypeList) {

                val javaFileName: String = if (hasJavaMultipleFiles) {
                    determineFileName(packageName, messageTypeDescriptor.name)
                } else {
                    determineFileName(packageName, outerClassName)
                }

                for (fieldDescriptor in messageTypeDescriptor.fieldList) {
                    handleField(fieldDescriptor, javaFileName,
                            messageTypeDescriptor.name,
                            fileDescriptor.getPackage(),
                            fileDescriptor.name)
                            ?.let { responseFiles.addAll(it) }
                }
            }
        }
        return responseFiles
    }

    override fun executeTemplate(templateResource: String, templateContext: Map<String, String>): String {
        return super.executeTemplate("$basePath/$templateResource", templateContext)
    }

    fun handleField(
            fieldDescriptor: DescriptorProtos.FieldDescriptorProto,
            javaFileName: String,
            messageName: String,
            protoPackage: String,
            protoFileName: String
    ): List<PluginProtos.CodeGeneratorResponse.File>?

    fun isSupportedType(fieldDescriptor: DescriptorProtos.FieldDescriptorProto): Boolean
}
