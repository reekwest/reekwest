package org.http4k.openapi.server

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeSpec
import org.http4k.openapi.ApiGenerator
import org.http4k.openapi.GenerationOptions
import org.http4k.openapi.OpenApi3Spec

object ServerApiGenerator : ApiGenerator {
    override fun invoke(spec: OpenApi3Spec, options: GenerationOptions) = with(spec) {

        val endpoints = buildEndpoints()

        val server = buildServer(endpoints)

        endpoints.map { it.asFileSpec(options.packageName("server.endpoints")) } +
            endpoints
                .fold(FileSpec.builder(options.packageName("server"), server.name!!)) { acc, next ->
                    acc.addImport(options.packageName("server.endpoints"), next.name)
                }
                .addType(server)
                .addFunction(server.buildMain())
                .build()
    }
}

private fun TypeSpec.buildMain() = FunSpec.builder("main")
    .addStatement("%N().%M(SunHttp(8000)).start()", this, MemberName("org.http4k.server", "asServer"))
    .build()

private fun FunSpec.asFileSpec(packageName: String) = FileSpec.builder(packageName, name).addFunction(this).indent("\t").build()
