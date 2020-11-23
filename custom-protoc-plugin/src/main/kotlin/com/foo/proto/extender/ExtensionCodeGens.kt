package com.foo.proto.extender

import com.google.protobuf.compiler.PluginProtos
import com.foo.proto.CodeGenerator

class ExtensionCodeGens : CodeGenerator {

  private val extensionGenerators = listOf<CodeGenerator>(
      LocalDateExtensionFunctionGenerator()
  )

  override fun generateFiles(request: PluginProtos.CodeGeneratorRequest): List<PluginProtos.CodeGeneratorResponse.File> =
      extensionGenerators.flatMap { it.generateFiles(request) }
}
