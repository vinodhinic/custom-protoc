package com.foo.proto.extender

import com.foo.proto.CodeGenerator
import com.google.protobuf.compiler.PluginProtos

class ExtensionCodeGens : CodeGenerator {

    private val extensionGenerators = listOf<CodeGenerator>(
            LocalDateExtensionFunctionGenerator()
    )

    override fun generateFiles(request: PluginProtos.CodeGeneratorRequest): List<PluginProtos.CodeGeneratorResponse.File> =
            extensionGenerators.flatMap { it.generateFiles(request) }
}
