package com.foo.proto.accessor

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Timestamp
import com.google.protobuf.compiler.PluginProtos
import com.foo.proto.codegen.FooOptions

class InstantAccessorCodeGenerator : AccessorCodeGenerator {
  private val supportedTypeToCanBeEnabledByDefaultMap = mapOf<String, Boolean>(
      Timestamp.getDescriptor().fullName to true)

  override fun handleField(
    fieldDescriptor: DescriptorProtos.FieldDescriptorProto,
    javaFileName: String,
    messageName: String,
    protoPackage: String,
    protoFileName: String
  ): List<PluginProtos.CodeGeneratorResponse.File>? {
    val fooJavaAccessorOptions =
        fieldDescriptor.options.getExtension(FooOptions.javaAccessor)

    val protoFieldName = fieldDescriptor.name

    val (getterMethodForAuxiliaryField, hasMethodForAuxiliaryField, setterMethodForAuxiliaryField) =
        when {
            fooJavaAccessorOptions.hasInstantAccessor() -> listOf(
                fooJavaAccessorOptions.instantAccessor,
                getHasAccessorForGetter(fooJavaAccessorOptions.instantAccessor),
                getSetAccessorFromGetter(fooJavaAccessorOptions.instantAccessor)
            )
            fooJavaAccessorOptions.hasInstant() || canBeEnabledByDefault(fieldDescriptor) -> listOf(
                "getInstant" + protoFieldName.underscoreToCamel().capitalizeFirstLetter(),
                "hasInstant" + protoFieldName.underscoreToCamel().capitalizeFirstLetter(),
                "setInstant" + protoFieldName.underscoreToCamel().capitalizeFirstLetter())
            else -> null
        } ?: return null

    val getterMethodForParentField = fieldDescriptor.getterMethodName()
    if (getterMethodForAuxiliaryField == getterMethodForParentField) {
      var errorMessage = "Unable to process instant accessor annotation on field $protoFieldName at proto package : $protoPackage File : $protoFileName."
      if (fooJavaAccessorOptions.hasInstantAccessor()) {
        errorMessage += "Value passed is same as the original accessor : $getterMethodForParentField"
      }
      throw IllegalArgumentException(errorMessage)
    }

    if (!isSupportedType(fieldDescriptor)) {
      throw IllegalArgumentException("Unable to process instant accessor annotation on field $protoFieldName at proto package : $protoPackage File : $protoFileName." +
          " instant accessor annotation can only be applied types : ${supportedTypeToCanBeEnabledByDefaultMap.keys} But the field is of type : ${getCurrentType(fieldDescriptor)}"
      )
    }

    if (fieldDescriptor.hasLabel() && fieldDescriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
      throw IllegalArgumentException("Unable to process instant accessor annotation on field $protoFieldName at proto package : $protoPackage File : $protoFileName." +
          " instant accessor annotation cannot be applied on repeated fields. Support coming soon.")
    }

    val codeBlockForGetterAndHasMethods = generateCodeBlock(fieldDescriptor, getterMethodForAuxiliaryField, hasMethodForAuxiliaryField)
    val codeBlockForSetterMethod = generateCodeBlock(fieldDescriptor, setterMethodForAuxiliaryField)

    return listOf(
        PluginProtos.CodeGeneratorResponse.File.newBuilder()
            .setName(javaFileName)
            .setContent(codeBlockForGetterAndHasMethods)
            .setInsertionPoint(getClassScopeInsertionPoint(protoPackage, messageName))
            .build(),
        PluginProtos.CodeGeneratorResponse.File.newBuilder()
            .setName(javaFileName)
            .setContent(codeBlockForSetterMethod)
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

  private fun generateCodeBlock(
    fieldDescriptor: DescriptorProtos.FieldDescriptorProto,
    getterMethodForAuxField: String,
    hasMethodNameForAuxField: String
  ): String? {
    return when (getCurrentType(fieldDescriptor)) {
      Timestamp.getDescriptor().fullName ->
        generateGetterCodeBlockForGoogleTimestampToInstant(fieldDescriptor.javaName(),
            getterMethodForParentField = fieldDescriptor.getterMethodName(),
            hasMethodForParentField = fieldDescriptor.hasMethodName(),
            getterMethodForAuxField = getterMethodForAuxField, hasMethodNameForAuxField = hasMethodNameForAuxField)
      else -> ""
    }
  }

  private fun generateCodeBlock(
    fieldDescriptor: DescriptorProtos.FieldDescriptorProto,
    setterMethodForAuxField: String
  ): String? {
    return when (getCurrentType(fieldDescriptor)) {
      Timestamp.getDescriptor().fullName ->
        generateSetterCodeBlockForInstantToGoogleTimestamp(
            setterMethodForParentField = fieldDescriptor.setterMethodName(),
            setterMethodForAuxField = setterMethodForAuxField)
      else -> ""
    }
  }

  private fun generateSetterCodeBlockForInstantToGoogleTimestamp(
    setterMethodForParentField: String,
    setterMethodForAuxField: String
  ): String? {
    val templateResource = "javaInstant-to-googleTimestamp.mustache"
    val templateContext = mapOf(
        "auxSetter" to setterMethodForAuxField,
        "parentSetter" to setterMethodForParentField
    )
    return executeTemplate(templateResource, templateContext)
  }

  private fun generateGetterCodeBlockForGoogleTimestampToInstant(
    javaFieldName: String,
    getterMethodForParentField: String,
    getterMethodForAuxField: String,
    hasMethodForParentField: String,
    hasMethodNameForAuxField: String
  ): String? {
    val templateResource = "googleTimestamp-to-javaInstant.mustache"
    val templateContext = mapOf(
        "auxField" to "instant" + javaFieldName.capitalizeFirstLetter(),
        "auxGetter" to getterMethodForAuxField,
        "auxHas" to hasMethodNameForAuxField,
        "parentGetter" to getterMethodForParentField,
        "parentHas" to hasMethodForParentField
    )
    return executeTemplate(templateResource, templateContext)
  }

}
