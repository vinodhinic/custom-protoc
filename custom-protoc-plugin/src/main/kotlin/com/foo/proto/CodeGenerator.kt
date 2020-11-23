package com.foo.proto

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.MustacheFactory
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.compiler.PluginProtos
import java.io.InputStreamReader
import java.io.StringWriter

private const val JAVA_EXTENSION = ".java"

interface CodeGenUtils {

    fun getImplementsInsertionPoint(protoPackage: String, messageName: String): String =
            if (protoPackage.isBlank()) {
                "message_implements:$messageName"
            } else {
                "message_implements:$protoPackage.$messageName"
            }

    // Note : This insertion point does not change as per the Java package name. It is always proto package + proto messageName
    fun getClassScopeInsertionPoint(protoPackageName: String, messageName: String): String = if (protoPackageName.isBlank()) {
        "class_scope:$messageName"
    } else {
        "class_scope:$protoPackageName.$messageName"
    }

    fun getBuilderScopeInsertionPoint(protoPackageName: String, messageName: String): String = if (protoPackageName.isBlank()) {
        "builder_scope:$messageName"
    } else {
        "builder_scope:$protoPackageName.$messageName"
    }

    fun getOuterClassScopeInsertionPoint(): String = "outer_class_scope"

    fun getHasAccessorForGetter(getterMethodName: String): String {
        val hasMethodName = getterMethodName.replace("get", "has")
        if (!hasMethodName.startsWith("has")) {
            return "has" + getterMethodName.capitalizeFirstLetter()
        }
        return hasMethodName
    }

    fun getSetAccessorFromGetter(getterMethodName: String): String {
        val setterMethodName = getterMethodName.replace("get", "set")
        if (!setterMethodName.startsWith("set")) {
            return "set" + getterMethodName.capitalizeFirstLetter()
        }
        return setterMethodName
    }

    fun String.convertToJavaClassName() = this.underscoreToCamel().capitalizeFirstLetter()

    fun String.capitalizeFirstLetter() = this.substring(0, 1).toUpperCase() + this.substring(1)

    fun String.uncapitalizeFirstLetter() = this.substring(0, 1).toLowerCase() + this.substring(1)

    fun String.underscoreToCamel(): String {
        if (this.contains("_")) {
            return this.split("_").joinToString(separator = "") { it.capitalizeFirstLetter() }
        } else {
            return this
        }
    }

    fun DescriptorProtos.FieldDescriptorProto.javaName() = this.name.underscoreToCamel().uncapitalizeFirstLetter() + "_"

    fun DescriptorProtos.FieldDescriptorProto.getterMethodName() = "get" + this.name.underscoreToCamel().capitalizeFirstLetter()

    fun DescriptorProtos.FieldDescriptorProto.setterMethodName() = "set" + this.name.underscoreToCamel().capitalizeFirstLetter()

    fun DescriptorProtos.FieldDescriptorProto.hasMethodName() = "has" + this.name.underscoreToCamel().capitalizeFirstLetter()

    fun getCurrentType(fieldDescriptor: DescriptorProtos.FieldDescriptorProto): String {
        var currentType: String
        if (fieldDescriptor.getType() == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE ||
                fieldDescriptor.getType() == DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM
        ) {
            currentType = fieldDescriptor.typeName
        } else {
            currentType = fieldDescriptor.type.name
        }
        if (currentType.startsWith(".")) {
            currentType = currentType.substring(1)
        }
        return currentType
    }

    val mustacheFactory: DefaultMustacheFactory
        get() = DefaultMustacheFactory()

    fun executeTemplate(templateResource: String, templateContext: Map<String, String> = emptyMap()): String {
        val resource = MustacheFactory::class.java.classLoader.getResourceAsStream(templateResource)
                ?: throw RuntimeException("Could not find resource $templateResource")
        val resourceReader = InputStreamReader(resource, Charsets.UTF_8)
        val template = mustacheFactory.compile(resourceReader, templateResource)
        return template.execute(StringWriter(), templateContext).toString()
    }

    fun determineFileName(packageName: String, className: String): String =
            packageName.replace(".", "/") + "/" + className + JAVA_EXTENSION

    fun getProtoFileNameWithoutDotProtoExtension(fileDescriptor: DescriptorProtos.FileDescriptorProto): String =
            fileDescriptor.name.split(".")[0]

    fun areThereAnyMessageWithSameNameAsOuterClassName(
            outerClassName: String,
            fileDescriptor: DescriptorProtos.FileDescriptorProto
    ): Boolean {
        return fileDescriptor.messageTypeList.any { it.getName().equals(outerClassName) }
    }
}

interface CodeGenerator : CodeGenUtils {
    fun generateFiles(request: PluginProtos.CodeGeneratorRequest): List<PluginProtos.CodeGeneratorResponse.File>
}
