package com.foo.proto.implementor

import com.foo.proto.CodeGenerator
import com.foo.proto.codegen.FooOptions
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.compiler.PluginProtos

class ImplementsKafkaEventCodeGenerator : CodeGenerator {

    private val kafkaEventFieldImplementorFactory = mapOf(
            FooOptions.KafkaEventField.KE_EVENT_TIME to EventTimeFieldImplementor()
    )

    override fun generateFiles(request: PluginProtos.CodeGeneratorRequest): List<PluginProtos.CodeGeneratorResponse.File> {
        val protoFileList = request.protoFileList
        val responseFiles: MutableList<PluginProtos.CodeGeneratorResponse.File> = mutableListOf()

        for (fileDescriptor in protoFileList) {
            var outerClassName = if (fileDescriptor.options.hasJavaOuterClassname()) {
                fileDescriptor.options.javaOuterClassname
            } else {
                getProtoFileNameWithoutDotProtoExtension(fileDescriptor).convertToJavaClassName()
            }

            if (areThereAnyMessageWithSameNameAsOuterClassName(outerClassName, fileDescriptor)) {
                outerClassName += "OuterClass"
            }

            val protoPackage = fileDescriptor.getPackage()
            val resolvedPackageName: String = if (fileDescriptor.options.hasJavaPackage()) {
                fileDescriptor.options.javaPackage
            } else {
                protoPackage
            }

            val hasJavaMultipleFiles = fileDescriptor.options.hasJavaMultipleFiles()

            for (messageTypeDescriptor in fileDescriptor.messageTypeList) {
                if (!isMessageAnnotatedWithKafkaInputEventInterface(messageTypeDescriptor)) {
                    continue
                }

                val messageName = messageTypeDescriptor.name
                val fileName = if (hasJavaMultipleFiles) {
                    determineFileName(resolvedPackageName, messageName)
                } else {
                    determineFileName(resolvedPackageName, outerClassName)
                }

                val kafkaEventFieldToProtoField: MutableMap<FooOptions.KafkaEventField, DescriptorProtos.FieldDescriptorProto> = mutableMapOf()

                for (fieldDescriptor in messageTypeDescriptor.fieldList) {
                    val kafkaEventInterfaceOptions = fieldDescriptor.options.getExtension(FooOptions.kafkaEvent)

                    var annotationValues: List<FooOptions.KafkaEventField> = listOf()
                    if (kafkaEventInterfaceOptions.hasField()) {
                        annotationValues = listOf(kafkaEventInterfaceOptions.field)
                    }
                    if (kafkaEventInterfaceOptions.hasFields()) {
                        annotationValues = kafkaEventInterfaceOptions.fields.valuesList
                    }
                    if (annotationValues.isEmpty()) {
                        continue
                    }

                    for (kafkaEventField in annotationValues) {
                        require(!kafkaEventFieldToProtoField.containsKey(kafkaEventField)) {
                            ("KafkaEventField " + kafkaEventField + " is being mapped to more than one proto field. At proto package :" +
                                    protoPackage + "File :" + fileDescriptor.name)
                        }
                        kafkaEventFieldToProtoField[kafkaEventField] = fieldDescriptor
                    }
                }

                val optionalInputEventFields = setOf(FooOptions.KafkaEventField.KE_UNKNOWN)
                val mandatoryInputEventFields = FooOptions.KafkaEventField.values()
                        .filter { e: FooOptions.KafkaEventField? -> !optionalInputEventFields.contains(e) }
                        .toSet()
                if (!kafkaEventFieldToProtoField.keys.containsAll(mandatoryInputEventFields)) {
                    throw IllegalArgumentException("Proto package :$protoPackage File :${fileDescriptor.name} implements KafkaEvent interface but not all mandatory fields are provided." +
                            " Please annotate fields for these following ones too : ${mandatoryInputEventFields.minus(kafkaEventFieldToProtoField.keys)}")
                }

                // *************************************HANDLE THE ACTUAL IMPLEMENTATION**************************************************
                responseFiles.add(PluginProtos.CodeGeneratorResponse.File.newBuilder()
                        .setName(fileName)
                        .setContent(com.foo.KafkaEvent::class.java.canonicalName + "<$messageName>,")
                        .setInsertionPoint(getImplementsInsertionPoint(protoPackage, messageName))
                        .build())
                for (inputEventField in FooOptions.KafkaEventField.values()) {
                    val files = kafkaEventFieldImplementorFactory[inputEventField]?.let {
                        it.validate(fileName, protoPackage, messageName, kafkaEventFieldToProtoField[inputEventField]!!)
                        it.generateImplementation(fileName, protoPackage, messageName, kafkaEventFieldToProtoField[inputEventField]!!)
                    }
                    files?.let { responseFiles.addAll(it) }
                }

                // **********************************************END**********************************************************************
            }
        }
        return responseFiles
    }

    private fun isMessageAnnotatedWithKafkaInputEventInterface(messageTypeDescriptor: DescriptorProtos.DescriptorProto): Boolean {
        if (messageTypeDescriptor.hasOptions()) {
            if (messageTypeDescriptor.options.hasExtension(FooOptions.fooInterface)) {
                val fooInterfaceOptions = messageTypeDescriptor.options
                        .getExtension(FooOptions.fooInterface)
                return fooInterfaceOptions.interfaces.valuesList.contains(FooOptions.FooInterface.FI_KAFKA_EVENT)
            }
        }
        return false
    }
}
