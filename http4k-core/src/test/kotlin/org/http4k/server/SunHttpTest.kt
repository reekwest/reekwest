package org.http4k.server

import org.http4k.client.ApacheClient

class SunHttpTest : ServerContract(::SunHttp, ApacheClient())

class SunHttpStopTest : ServerStopContract(
    { stopMode -> SunHttp(0, stopMode) },
    ApacheClient(),
    {
        enableImmediateStop()
        enableGracefulStop()
    }
)
