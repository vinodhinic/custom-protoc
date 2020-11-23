@file:JvmName("CustomCodeGen")

package com.foo.proto


import com.foo.proto.accessor.AccessCodeGens
import com.foo.proto.codegen.FooOptions
import com.foo.proto.extender.ExtensionCodeGens
import com.foo.proto.implementor.ImplementsKafkaEventCodeGenerator
import com.google.common.io.ByteStreams
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos
import java.io.IOException
import kotlin.system.exitProcess

fun main() {
    try {
        val generatorRequestBytes: ByteArray = ByteStreams.toByteArray(System.`in`)
        val extensionRegistry: ExtensionRegistry = ExtensionRegistry.newInstance()
        extensionRegistry.add(FooOptions.javaAccessor)
        extensionRegistry.add(FooOptions.kafkaEvent)
        extensionRegistry.add(FooOptions.fooInterface)

        val request: PluginProtos.CodeGeneratorRequest = PluginProtos.CodeGeneratorRequest.parseFrom(
                generatorRequestBytes, extensionRegistry)
        val generators: List<CodeGenerator> = listOf(
                AccessCodeGens(),
                ImplementsKafkaEventCodeGenerator(),
                ExtensionCodeGens()
        )

        val files: List<PluginProtos.CodeGeneratorResponse.File> = generators
                .map { it.generateFiles(request) }
                .flatten()
                .toList()
        val response: PluginProtos.CodeGeneratorResponse = PluginProtos.CodeGeneratorResponse.newBuilder()
                .addAllFile(files).build()
        response.writeTo(System.out)
    } catch (ex: Throwable) {
        try {
            PluginProtos.CodeGeneratorResponse
                    .newBuilder()
                    .setError(ex.message)
                    .build()
                    .writeTo(System.out)
        } catch (ex2: IOException) {
            abort(ex2)
        }
        abort(ex)
    }
}

fun abort(ex: Throwable) {
    ex.printStackTrace(System.err)
    exitProcess(1)
}
