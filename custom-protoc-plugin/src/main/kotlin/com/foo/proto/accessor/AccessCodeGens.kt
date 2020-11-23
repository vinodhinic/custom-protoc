package com.foo.proto.accessor

import com.google.protobuf.compiler.PluginProtos
import com.foo.proto.CodeGenerator

class AccessCodeGens : CodeGenerator {

  private val accessorGenerators = listOf<CodeGenerator>(
      LocalDateAccessorCodeGenerator(),
      InstantAccessorCodeGenerator(),
      BigDecimalAccessorCodeGenerator()
  )

  override fun generateFiles(request: PluginProtos.CodeGeneratorRequest): List<PluginProtos.CodeGeneratorResponse.File> =
      accessorGenerators.flatMap { it.generateFiles(request) }
}
