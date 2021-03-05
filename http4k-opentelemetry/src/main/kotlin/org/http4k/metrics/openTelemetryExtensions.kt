package org.http4k.metrics

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.metrics.GlobalMetricsProvider
import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.trace.Tracer

/**
 * OpenTracing works using a set of named Singletons. We use the the constant name here to
 * make it simple to get the instances of the required objects.
 */
object Http4kOpenTelemetry {
    const val INSTRUMENTATION_NAME = "http4k"

    val tracer: Tracer get() = default.tracerProvider.get(INSTRUMENTATION_NAME)

    val meter: Meter get() = GlobalMetricsProvider.getMeter(INSTRUMENTATION_NAME)

    val default: OpenTelemetry = GlobalOpenTelemetry.get()
}
