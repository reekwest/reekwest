package org.http4k.routing

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.LensFailure

interface Module {
    infix fun then(that: Module): Module {
        val thisBinding = toRouter()
        val thatBinding = that.toRouter()

        return object : Module {
            override fun toRouter(): Router = object : Router {
                override fun match(request: Request): HttpHandler? = thisBinding.match(request) ?: thatBinding.match(request)
            }
        }
    }

    fun toHttpHandler(): HttpHandler = toRouter().let { router ->
        { req ->
            try {
                router.match(req)?.invoke(req) ?: Response(Status.NOT_FOUND)
            } catch (e: LensFailure) {
                Response(Status.BAD_REQUEST)
            }
        }
    }

    fun toRouter(): Router
}