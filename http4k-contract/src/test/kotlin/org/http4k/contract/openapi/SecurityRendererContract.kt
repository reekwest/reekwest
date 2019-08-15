package org.http4k.contract.openapi

import argo.jdom.JsonNode
import org.http4k.contract.security.Security
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Argo
import org.http4k.testing.Approver
import org.http4k.testing.JsonApprovalTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(JsonApprovalTest::class)
interface SecurityRendererContract {

    val security: Security
    val renderer: SecurityRenderer

    @Test
    fun ref(approver: Approver) {
        approver.assertSecurityRenders(renderer.ref(security))
    }

    @Test
    fun full(approver: Approver) {
        approver.assertSecurityRenders(renderer.full(security))
    }
}

private fun Approver.assertSecurityRenders(function: Render<JsonNode>?) {
    assertApproved(Response(Status.OK).body(
        Argo {
            pretty(function?.invoke(this)!!)
        }
    ))
}