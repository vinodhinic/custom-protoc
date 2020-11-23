package com.foo.proto.accessor

import com.foo.proto.codegen.FooOptions
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.StringValue
import com.google.protobuf.compiler.PluginProtos
import java.math.BigDecimal

class BigDecimalAccessorCodeGenerator : AccessorCodeGenerator {

    private val supportedTypeToCanBeEnabledByDefaultMap = mapOf<String, Boolean>(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING.name to false,
            StringValue.getDescriptor().fullName to false)

    override fun handleField(fieldDescriptor: DescriptorProtos.FieldDescriptorProto, javaFileName: String, messageName: String, protoPackage: String, protoFileName: String): List<PluginProtos.CodeGeneratorResponse.File>? {
        val fooJavaAccessorOptions = fieldDescriptor.options.getExtension(FooOptions.javaAccessor)

        val protoFieldName = fieldDescriptor.name
        val (getterMethodForAuxiliaryField, hasMethodForAuxiliaryField, setterMethodForAuxiliaryField) = when {
            fooJavaAccessorOptions.hasBigDecimalAccessor() -> {
                listOf(fooJavaAccessorOptions.bigDecimalAccessor,
                        getHasAccessorForGetter(fooJavaAccessorOptions.bigDecimalAccessor),
                        getSetAccessorFromGetter(fooJavaAccessorOptions.bigDecimalAccessor)
                )
            }
            fooJavaAccessorOptions.hasBigDecimal() || canBeEnabledByDefault(fieldDescriptor) -> {
                listOf("getBigDecimal" + protoFieldName.underscoreToCamel().capitalizeFirstLetter(),
                        "hasBigDecimal" + protoFieldName.underscoreToCamel().capitalizeFirstLetter(),
                        "setBigDecimal" + protoFieldName.underscoreToCamel().capitalizeFirstLetter()
                )
            }
            else -> null
        } ?: return null

        val getterMethodForParentField = fieldDescriptor.getterMethodName()
        if (getterMethodForAuxiliaryField == getterMethodForParentField) {
            var errorMessage = "Unable to process bigDecimalAccessor annotation on field $protoFieldName at proto package : $protoPackage File : $protoFileName."
            if (fooJavaAccessorOptions.hasBigDecimalAccessor()) {
                errorMessage += "Value passed is same as the original accessor : $getterMethodForParentField"
            }
            throw IllegalArgumentException(errorMessage)
        }

        if (!isSupportedType(fieldDescriptor)) {
            throw IllegalArgumentException("Unable to process bigDecimalAccessor annotation on field $protoFieldName at proto package : $protoPackage File : $protoFileName. bigDecimalAccessor annotation can only be applied types : ${supportedTypeToCanBeEnabledByDefaultMap.keys} But the field is of type : ${getCurrentType(fieldDescriptor)}"
            )
        }

        if (fieldDescriptor.hasLabel() && fieldDescriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
            throw IllegalArgumentException("Unable to process instantAccessor annotation on field $protoFieldName at proto package : $protoPackage File : $protoFileName." +
                    " bigDecimalAccessor annotation cannot be applied on repeated fields. Support coming soon.")
        }

        val getterCodeBlock = generateGetterCodeBlock(fieldDescriptor, getterMethodForAuxiliaryField, hasMethodForAuxiliaryField)
        val setterCodeBlock = generateSetterCodeBlock(fieldDescriptor, setterMethodForAuxiliaryField)
        return listOf(
                PluginProtos.CodeGeneratorResponse.File.newBuilder()
                        .setName(javaFileName)
                        .setContent(getterCodeBlock)
                        .setInsertionPoint(getClassScopeInsertionPoint(protoPackage, messageName))
                        .build(),
                PluginProtos.CodeGeneratorResponse.File.newBuilder()
                        .setName(javaFileName)
                        .setContent(setterCodeBlock)
                        .setInsertionPoint(getBuilderScopeInsertionPoint(protoPackage, messageName))
                        .build()
        )
    }

    private fun canBeEnabledByDefault(fieldDescriptor: DescriptorProtos.FieldDescriptorProto): Boolean =
            if (fieldDescriptor.hasLabel() && fieldDescriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
                false
            } else {
                supportedTypeToCanBeEnabledByDefaultMap[getCurrentType(fieldDescriptor)] ?: false
            }

    override fun isSupportedType(fieldDescriptor: DescriptorProtos.FieldDescriptorProto): Boolean =
            supportedTypeToCanBeEnabledByDefaultMap.containsKey(getCurrentType(fieldDescriptor))

    private fun generateGetterCodeBlock(
            fieldDescriptor: DescriptorProtos.FieldDescriptorProto,
            getterMethodForAuxField: String,
            hasMethodNameForAuxField: String
    ):
            String {
        return when (getCurrentType(fieldDescriptor)) {
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING.name ->
                generateCodeBlockForStringToBigDecimal(
                        javaFieldName = fieldDescriptor.javaName(),
                        getterMethodForParentField = fieldDescriptor.getterMethodName(),
                        getterMethodForAuxField = getterMethodForAuxField,
                        hasMethodNameForAuxField = hasMethodNameForAuxField
                )

            StringValue.getDescriptor().fullName -> generateCodeBlockForStringValueToBigDecimal(
                    javaFieldName = fieldDescriptor.javaName(),
                    getterMethodForParentField = fieldDescriptor.getterMethodName(),
                    hasMethodForParentField = fieldDescriptor.hasMethodName(),
                    getterMethodForAuxField = getterMethodForAuxField,
                    hasMethodNameForAuxField = hasMethodNameForAuxField
            )
            else -> return ""
        }
    }

    private fun generateCodeBlockForStringToBigDecimal(
            javaFieldName: String,
            getterMethodForParentField: String,
            getterMethodForAuxField: String,
            hasMethodNameForAuxField: String
    ): String {
        val templateResource = "string-to-bigDecimal.mustache"
        val templateContext = mapOf(
                "auxType" to BigDecimal::class.java.canonicalName,
                "auxField" to "td" + javaFieldName.capitalizeFirstLetter(),
                "auxGetter" to getterMethodForAuxField,
                "auxHas" to hasMethodNameForAuxField,
                "parentGetter" to getterMethodForParentField
        )
        return executeTemplate(templateResource, templateContext)
    }

    private fun generateCodeBlockForStringValueToBigDecimal(
            javaFieldName: String,
            getterMethodForParentField: String,
            getterMethodForAuxField: String,
            hasMethodForParentField: String,
            hasMethodNameForAuxField: String
    ): String {
        val templateResource = "stringValue-to-bigDecimal.mustache"
        val templateContext = mapOf(
                "auxType" to BigDecimal::class.java.canonicalName,
                "auxField" to "td" + javaFieldName.capitalizeFirstLetter(),
                "auxGetter" to getterMethodForAuxField,
                "auxHas" to hasMethodNameForAuxField,
                "parentGetter" to getterMethodForParentField,
                "parentHas" to hasMethodForParentField
        )
        return executeTemplate(templateResource, templateContext)
    }

    private fun generateSetterCodeBlock(
            fieldDescriptor: DescriptorProtos.FieldDescriptorProto,
            setterMethodForAuxField: String
    ):
            String {
        return when (getCurrentType(fieldDescriptor)) {
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING.name ->
                generateCodeBlockForBigDecimalToString(
                        setterMethodForParentField = fieldDescriptor.setterMethodName(),
                        setterMethodForAuxField = setterMethodForAuxField
                )

            StringValue.getDescriptor().fullName -> generateCodeBlockForBigDecimalToStringValue(
                    setterMethodForParentField = fieldDescriptor.setterMethodName(),
                    setterMethodForAuxField = setterMethodForAuxField
            )
            else -> return ""
        }
    }

    private fun generateCodeBlockForBigDecimalToString(
            setterMethodForParentField: String,
            setterMethodForAuxField: String
    ): String {
        val templateResource = "bigDecimal-to-string.mustache"
        val templateContext = mapOf(
                "auxType" to BigDecimal::class.java.canonicalName,
                "auxSetter" to setterMethodForAuxField,
                "parentSetter" to setterMethodForParentField
        )
        return executeTemplate(templateResource, templateContext)
    }

    private fun generateCodeBlockForBigDecimalToStringValue(
            setterMethodForParentField: String,
            setterMethodForAuxField: String
    ): String {
        val templateResource = "bigDecimal-to-stringValue.mustache"
        val templateContext = mapOf(
                "auxType" to BigDecimal::class.java.canonicalName,
                "auxSetter" to setterMethodForAuxField,
                "parentSetter" to setterMethodForParentField
        )
        return executeTemplate(templateResource, templateContext)
    }
}
