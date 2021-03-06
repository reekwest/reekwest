package org.http4k.serverless

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.RequestContext
import org.http4k.core.Store
import org.http4k.core.then

/**
 * Http4k app loader - instantiate the application from the environment config and RequestContexts
 */
fun interface AppLoaderWithContexts : (Map<String, String>, Store<RequestContext>) -> HttpHandler

fun Filter.then(appLoader: AppLoaderWithContexts) = AppLoaderWithContexts { p1, contexts -> then(appLoader(p1, contexts)) }
