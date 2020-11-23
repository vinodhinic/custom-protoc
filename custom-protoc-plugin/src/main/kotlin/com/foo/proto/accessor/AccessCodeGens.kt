package com.foo.proto.accessor

import com.foo.proto.CodeGenerator
import com.google.protobuf.compiler.PluginProtos

class AccessCodeGens : CodeGenerator {

    private val accessorGenerators = listOf<CodeGenerator>(
            LocalDateAccessorCodeGenerator(),
            InstantAccessorCodeGenerator(),
            BigDecimalAccessorCodeGenerator()
    )

    override fun generateFiles(request: PluginProtos.CodeGeneratorRequest): List<PluginProtos.CodeGeneratorResponse.File> =
            accessorGenerators.flatMap { it.generateFiles(request) }
}
