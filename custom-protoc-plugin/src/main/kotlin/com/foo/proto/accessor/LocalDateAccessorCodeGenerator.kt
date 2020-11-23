package com.foo.proto.accessor

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.compiler.PluginProtos
import com.foo.proto.codegen.FooOptions
import com.foo.proto.codegen.FooOptions.FooJavaAccessorOptions
import com.foo.types.LocalDateProto

class LocalDateAccessorCodeGenerator : AccessorCodeGenerator {

  private val supportedTypeToCanBeEnabledByDefaultMap = mapOf<String, Boolean>(LocalDateProto.LocalDate.getDescriptor().fullName to true)

  override fun handleField(
    fieldDescriptor: DescriptorProtos.FieldDescriptorProto,
    javaFileName: String,
    messageName: String,
    protoPackage: String,
    protoFileName: String
  ): List<PluginProtos.CodeGeneratorResponse.File>? {
    val fooJavaAccessorOptions: FooJavaAccessorOptions = fieldDescriptor.options.getExtension(FooOptions.javaAccessor)

    val protoFieldName: String = fieldDescriptor.name

    val (getterMethodForAuxiliaryField, hasMethodForAuxiliaryField, setterMethodForAuxiliaryField) = when {
          fooJavaAccessorOptions.hasLocalDateAccessor() -> {
            listOf(fooJavaAccessorOptions.localDateAccessor,
                getHasAccessorForGetter(fooJavaAccessorOptions.localDateAccessor),
                getSetAccessorFromGetter(fooJavaAccessorOptions.localDateAccessor)
            )
          }
          fooJavaAccessorOptions.hasLocalDate() || canBeEnabledByDefault(fieldDescriptor) -> {
            listOf("getLocalDate" + protoFieldName.underscoreToCamel().capitalizeFirstLetter(),
                "hasLocalDate" + protoFieldName.underscoreToCamel().capitalizeFirstLetter(),
                "setLocalDate" + protoFieldName.underscoreToCamel().capitalizeFirstLetter()
            )
          }
          else -> null
        } ?: return null

    val getterMethodForParentField = fieldDescriptor.getterMethodName()

    if (getterMethodForAuxiliaryField == getterMethodForParentField) {
      var errorMessage = ("Unable to process localDateAccessor annotation on field $protoFieldName at proto package : $protoPackage File : $protoFileName.")
      if (fooJavaAccessorOptions.hasLocalDateAccessor()) {
        errorMessage += "Value passed is same as the original accessor : $getterMethodForParentField"
      }
      throw IllegalArgumentException(errorMessage)
    }

    require(isSupportedType(fieldDescriptor)) {
      ("Unable to process localDateAccessor annotation on field $protoFieldName at proto package : $protoPackage File : $protoFileName. " +
          "localDateAccessor annotation can only be applied types : ${supportedTypeToCanBeEnabledByDefaultMap.keys} But the field is of type : ${getCurrentType(fieldDescriptor)}")
    }

    if (fieldDescriptor.hasLabel() && fieldDescriptor.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
      throw IllegalArgumentException("Unable to process instantAccessor annotation on field $protoFieldName at proto package : $protoPackage File : $protoFileName." +
          " localDateAccessor annotation cannot be applied on repeated fields. Support coming soon.")
    }

    val codeBlockForGetterMethod = generateGetterCodeBlock(fieldDescriptor, getterMethodForAuxiliaryField, hasMethodForAuxiliaryField)
    val codeBlockForSetterMethod = generateSetterCodeBlock(fieldDescriptor, setterMethodForAuxiliaryField)

    return listOf(
        PluginProtos.CodeGeneratorResponse.File.newBuilder()
        .setName(javaFileName)
        .setContent(codeBlockForGetterMethod)
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

  private fun generateSetterCodeBlock(
    fieldDescriptor: DescriptorProtos.FieldDescriptorProto,
    setterMethodForAuxField: String
  ): String {
    return when (getCurrentType(fieldDescriptor)) {
      LocalDateProto.LocalDate.getDescriptor().fullName ->
        generateSetterCodeBlockForJavaLocalDateToFooLocalDate(
            setterMethodForParentField = fieldDescriptor.setterMethodName(),
            setterMethodForAuxField = setterMethodForAuxField)
      else -> ""
    }
  }

  private fun generateSetterCodeBlockForJavaLocalDateToFooLocalDate(
    setterMethodForParentField: String,
    setterMethodForAuxField: String
  ): String {
    val templateResource = "javaLocalDate-to-fooLocalDate.mustache"
    val templateContext = mapOf(
        "auxSetter" to setterMethodForAuxField,
        "parentSetter" to setterMethodForParentField
    )
    return executeTemplate(templateResource, templateContext)
  }

  private fun generateGetterCodeBlock(
    fieldDescriptor: DescriptorProtos.FieldDescriptorProto,
    getterMethodForAuxField: String,
    hasMethodNameForAuxField: String
  ): String {
    return when (getCurrentType(fieldDescriptor)) {
      LocalDateProto.LocalDate.getDescriptor().fullName ->
        generateGetterCodeBlockForFooLocalDateToJavaLocalDate(javaFieldName = fieldDescriptor.javaName(),
            getterMethodForParentField = fieldDescriptor.getterMethodName(),
            hasMethodForParentField = fieldDescriptor.hasMethodName(),
            getterMethodForAuxField = getterMethodForAuxField,
            hasMethodNameForAuxField = hasMethodNameForAuxField)
      else -> ""
    }
  }

  private fun generateGetterCodeBlockForFooLocalDateToJavaLocalDate(
    javaFieldName: String,
    getterMethodForParentField: String,
    getterMethodForAuxField: String,
    hasMethodForParentField: String,
    hasMethodNameForAuxField: String
  ): String {
    val templateResource = "fooLocalDate-to-javaLocalDate.mustache"
    val templateContext = mapOf(
        "auxField" to "ld" + javaFieldName.capitalizeFirstLetter(),
        "auxGetter" to getterMethodForAuxField,
        "auxHas" to hasMethodNameForAuxField,
        "parentGetter" to getterMethodForParentField,
        "parentHas" to hasMethodForParentField
    )
    return executeTemplate(templateResource, templateContext)
  }
}
