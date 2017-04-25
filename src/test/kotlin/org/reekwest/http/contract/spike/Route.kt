package org.reekwest.http.contract.spike

import org.reekwest.http.contract.*
import org.reekwest.http.core.*

data class RouteResponse(val status: Status, val description: String?, val example: String?)

data class Route private constructor(private val name: String,
                                     private val description: String?,
                                     val body: BodyLens<*>?,
                                     private val produces: Set<ContentType> = emptySet(),
                                     private val consumes: Set<ContentType> = emptySet(),
                                     val requestParams: Iterable<Lens<Request, *>> = emptyList(),
                                     private val responses: Iterable<RouteResponse> = emptyList()) : Iterable<Lens<Request, *>> {
    constructor(name: String, description: String? = null) : this(name, description, null)

    override fun iterator(): Iterator<Lens<Request, *>> = requestParams.plus(body?.let { listOf(it)} ?: emptyList()).iterator()

    fun header(new: HeaderLens<*>) = copy(requestParams = requestParams.plus(new))
    fun query(new: QueryLens<*>) = copy(requestParams = requestParams.plus(new))
    fun body(new: Lens<HttpMessage, *>) = copy(body = new)
    fun returning(new: Pair<Status, String>, description: String? = null) = copy(responses = responses.plus(RouteResponse(new.first, new.second, description)))
    fun producing(vararg new: ContentType) = copy(produces = produces.plus(new))
    fun consuming(vararg new: ContentType) = copy(consumes = consumes.plus(new))

    infix operator fun div(next: String): PathBinder0 = PathBinder0(this, { Root / next })
    infix operator fun <T> div(next: Lens<String, T>) = PathBinder0(this, { Root }) / next

}

abstract class ServerRoute(val pathBuilder: PathBinder, val method: Method, vararg val pathParams: Lens<String, *>) {

    fun matches(actualMethod: Method, basePath: PathBuilder, actualPath: PathBuilder): Boolean? = actualMethod == method && actualPath == pathBuilder.pathFn(basePath)

    abstract fun match(filter: Filter, basePath: PathBuilder): (Method, PathBuilder) -> HttpHandler?

    fun describeFor(basePath: PathBuilder): String = (pathBuilder.pathFn(basePath).toString()) + pathParams.map { it.toString() }.joinToString { "/" }
}

internal class ExtractedParts(private val mapping: Map<PathLens<*>, *>) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(lens: PathLens<T>): T = mapping[lens] as T
}

class RouteBinder<in T> internal constructor(private val pathBuilder: PathBinder,
                                             private val method: Method,
                                             private val invoker: (T, ExtractedParts) -> HttpHandler,
                                             private vararg val pathLenses: PathLens<*>) {
    infix fun bind(fn: T): ServerRoute = object : ServerRoute(pathBuilder, method) {
        override fun match(filter: Filter, basePath: PathBuilder) =
            {
                actualMethod: Method, actualPath: PathBuilder ->
                matches(actualMethod, basePath, actualPath)?.let {
                    from(actualPath)?.let { filter(invoker(fn, it)) }
                }
            }
    }

    private fun from(path: PathBuilder) = try {
        if (path.toList().size == pathLenses.size) {
            ExtractedParts(mapOf(*pathLenses
                .mapIndexed { index, lens -> lens to path(index, lens) }.
                toTypedArray()))
        } else {
            null
        }
    } catch (e: ContractBreach) {
        null
    }
}