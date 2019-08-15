package org.http4k.contract.security

import org.http4k.core.Filter
import org.http4k.core.Uri
import org.http4k.security.OAuthProvider

sealed class OAuthSecurity(override val filter: Filter,
                           val name: String,
                           val scopes: List<OAuthScope>,
                           val refreshUrl: Uri?,
                           val extraFields: Map<String, String> = emptyMap()) : Security {
    companion object
}

class AuthCodeOAuthSecurity(val authorizationUrl: Uri,
                            val tokenUrl: Uri,
                            scopes: List<OAuthScope> = emptyList(),
                            filter: Filter,
                            name: String = "oauthSecurityAuthCode",
                            refreshUrl: Uri? = null,
                            extraFields: Map<String, String> = emptyMap()) :
    OAuthSecurity(filter, name, scopes, refreshUrl, extraFields) {

    companion object {
        operator fun invoke(oAuthProvider: OAuthProvider,
                            customScopes: List<OAuthScope>? = null) = AuthCodeOAuthSecurity(
            oAuthProvider.providerConfig.authUri,
            oAuthProvider.providerConfig.tokenUri,
            customScopes ?: oAuthProvider.scopes.map { OAuthScope(it, "") },
            oAuthProvider.authFilter
        )
    }
}

class ImplicitOAuthSecurity(val authorizationUrl: Uri,
                            filter: Filter,
                            scopes: List<OAuthScope> = emptyList(),
                            refreshUrl: Uri? = null,
                            name: String = "oauthSecurityImplicit",
                            extraFields: Map<String, String> = emptyMap()) :
    OAuthSecurity(filter, name, scopes, refreshUrl, extraFields)

data class OAuthScope(val name: String, val description: String = name)