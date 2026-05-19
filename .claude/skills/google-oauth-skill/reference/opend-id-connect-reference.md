# Google OpenID Connect API Reference

This page describes Google's implementation as an OpenID Connect provider and
provides the technical reference for Google's OIDC endpoints. The [OpenID
Connect (OIDC) Core 1.0](https://openid.net/specs/openid-connect-core-1_0-final.html) specification defines the protocol for
authenticating users and obtaining identity information.

This reference is not intended to provide step-by-step instructions on how to
implement OIDC; for implementation details, see the [OpenID Connect](https://developers.google.com/identity/openid-connect/openid-connect) guide.

## Discovery Document

The [Discovery Document](https://developers.google.com/identity/openid-connect/reference#discovery-document) contains metadata about Google's
OpenID Connect configuration as defined in the [OpenID Connect Discovery
1.0](https://openid.net/specs/openid-connect-discovery-1_0.html) specification.

**URL:** `https://accounts.google.com/.well-known/openid-configuration`

**Supported request method:** `GET`

### Response body

The response fields are returned in a JSON object in the body of the HTTP
response to the requester's `GET` request to
`https://accounts.google.com/.well-known/openid-configuration`.

| Field | Type | Description |
|---|---|---|
| `issuer` | `string` | The Issuer Identifier. Case-sensitive URL using the `https` scheme. The modern value is `https://accounts.google.com`; however, `accounts.google.com` is also returned for legacy implementations. |
| `authorization_endpoint` | `string` | The URL of the Authorization Endpoint. |
| `device_authorization_endpoint` | `string` | The URL of the Device Authorization Endpoint. |
| `token_endpoint` | `string` | The URL of the Token Endpoint. |
| `userinfo_endpoint` | `string` | The URL of the UserInfo Endpoint. |
| `revocation_endpoint` | `string` | The URL of the Revocation Endpoint. |
| `jwks_uri` | `string` | The URL of the JSON Web Key Set (JWKS) document. |
| `response_types_supported` | `array` | A list of supported `response_type` values. |
| `response_modes_supported` | `array` | A list of supported `response_mode` values. |
| `authorization_response_iss_parameter_supported` | `boolean` | Boolean indicating support for [RFC 9207](https://datatracker.ietf.org/doc/html/rfc9207). |
| `subject_types_supported` | `array` | A list of supported subject identifier types. |
| `id_token_signing_alg_values_supported` | `array` | A list of supported algorithms for signing the ID Token. |
| `scopes_supported` | `array` | A list of supported [scope values](https://developers.google.com/identity/protocols/oauth2/scopes). |
| `claims_supported` | `array` | A list of supported claims. |
| `token_endpoint_auth_methods_supported` | `array` | A list of supported authentication methods for the Token Endpoint. |
| `code_challenge_methods_supported` | `array` | A list of supported code challenge methods for PKCE. |
| `grant_types_supported` | `array` | A list of supported OAuth 2.0 Grant Types. |

## ID Token (Claims)

The `id_token` value returned in responses is a signed JSON Web Token (JWT) that
must be verified using the keying material obtained from the `jwks_uri` found in
the Discovery Document. The following table describes the contents of the
decoded ID Token payload.

> [!NOTE]
> **Note:** While the [Discovery Document](https://developers.google.com/identity/openid-connect/reference#discovery-document) lists the standard claims supported by the OpenID Connect specification, this reference provides the canonical list of all claims returned in Google ID Tokens.

| Claim | Type | Description |
|---|---|---|
| `iss` | `string` | **Required.** The Issuer Identifier for the Issuer of the response. Typically `https://accounts.google.com`; however, `accounts.google.com` is also returned for legacy implementations. |
| `sub` | `string` | **Required.** An identifier for the user, unique among all Google Accounts and never reused. A Google Account can have multiple email addresses at different points in time, but the `sub` value is never changed. Use `sub` within your application as the unique-identifier key for the user. Maximum length of 255 case-sensitive ASCII characters. |
| `azp` | `string` | The Client Identifier of the authorized presenter, obtained from the [Google Cloud console](https://console.developers.google.com/auth/clients). This claim is only needed when the party requesting the ID Token is not the same as the audience of the ID Token. |
| `aud` | `string` | **Required.** The audience for which the ID Token is intended. This is your application's Client Identifier, obtained from the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `iat` | `integer` | **Required.** The time the ID Token was issued. Represented in Unix epoch time (integer seconds). |
| `exp` | `integer` | **Required.** Expiration time on or after which the ID Token must not be accepted. Represented in Unix epoch time (integer seconds). |
| `nonce` | `string` | The value of the `nonce` supplied by your app in the authentication request. You should protect against replay attacks by presenting this value only once. |
| `auth_time` | `integer` | The time user authentication took place, a JSON number representing the number of seconds that have elapsed since the Unix epoch (January 1, 1970, 00:00:00 UTC). Provided when the `auth_time` claim is included in the authentication request `claims` parameter. |
| `at_hash` | `string` | Access token hash. Provides validation that the Access Token is tied to the identity token. If the ID Token is issued with an `access_token` value in the server flow, this claim is always included. |
| `email` | `string` | The user's email address. Provided only if you included the `email` scope in your request. The value of this claim may not be unique to this account and could change over time, therefore you shouldn't use this value as the primary identifier to link to your user record. You also can't rely on the domain of the `email` claim to identify users of Google Workspace or Cloud organizations; use the `hd` claim instead. **Warning:** Don't use email address as an identifier because a Google Account can have multiple email addresses at different points in time. Always use the `sub` field as the identifier for the user. |
| `email_verified` | `boolean` | True if the user's email address has been verified; otherwise false. |
| `name` | `string` | The user's full name, in a displayable form. Might be provided when the request scope included the string `profile` or the ID Token is returned from a token refresh. |
| `picture` | `string` | The URL of the user's profile picture. Might be provided when the request scope included the string `profile` or the ID token is returned from a token refresh. |
| `given_name` | `string` | The user's given name(s) or first name(s). Might be provided when a `name` claim is present. |
| `family_name` | `string` | The user's surname(s) or last name(s). Might be provided when a `name` claim is present. |
| `hd` | `string` | The domain associated with the Google Workspace or Cloud organization of the user. Provided only if the user belongs to a Google Cloud organization. You must check this claim when restricting access to a resource to only members of certain domains. The absence of this claim indicates that the account does not belong to a Google hosted domain. |

## Authorization Endpoint

The Authorization Endpoint is used to authenticate the user and obtain an
Authorization Code or tokens.

**URL:** `https://accounts.google.com/o/oauth2/v2/auth`

**Supported request methods:** `GET`, `POST`

### Request parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `client_id` | `string` | Required | The Client Identifier string that you obtain from the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `nonce` | `string` | Optional | A random value generated by your app that enables replay protection. Required only if requesting an ID Token (when `response_type` includes `id_token`). |
| `response_type` | `string` | Required | Determines the flow to use. If the value is `code`, launches an Authorization Code Flow, requiring a `POST` to the Token Endpoint to obtain the tokens. If the value is `token`, `id_token`, `token id_token` or `id_token token`, launches an Implicit flow, requiring the use of JavaScript at the Redirection URI to retrieve tokens from the [URI fragment](https://datatracker.ietf.org/doc/html/rfc3986#section-3). Using `token` in any form is strongly discouraged as it exposes Access Tokens in the URL; this value is forbidden in OAuth 2.1. |
| `response_mode` | `string` | Optional | Specifies how the Authorization Response is delivered. If the `response_type` is `code`, the default is `query`. For other response types, the default is `fragment`. Supported values: `query`, `fragment`, `form_post`. |
| `redirect_uri` | `string` | Required | Determines where the response is sent. The value of this parameter must exactly match one of the authorized redirect values that you set in the [Google Cloud console](https://console.developers.google.com/auth/clients) (including the HTTP or HTTPS scheme, case, and trailing '/', if any). Redirect URIs and authorized JavaScript origins must follow the validation rules outlined in the [OAuth 2.0 URI validation](https://developers.google.com/identity/protocols/oauth2/web-server#uri-validation) documentation. |
| `scope` | `string` | Required | A space-delimited, unordered list of scopes. The list must include the `openid` value and then include the `profile` value, the `email` value, or both. You can also include non-OIDC scopes. If the `profile` scope value is present, the ID Token might (but is not guaranteed to) include the user's default `profile` claims. If the `email` scope value is present, the ID Token includes `email` and `email_verified` claims. For more information, see [OAuth 2.0 Scopes](https://developers.google.com/identity/protocols/oauth2/scopes). |
| `state` | `string` | Recommended | An opaque string that is round-tripped in the protocol; that is to say, it is returned as a URI parameter in the Authorization Code flow, and in the URI fragment in the Implicit flow. The `state` can be useful for correlating requests and responses. Because your `redirect_uri` can be guessed, using a `state` value can increase your assurance that an incoming connection is the result of an authentication request initiated by your app. This provides protection against attacks such as cross-site request forgery. |
| `access_type` | `string` | Optional | The allowed values are `offline` and `online`. If your application needs to refresh Access Tokens when the user is not present at the browser, then use `offline`. This value is required to get a Refresh Token returned. |
| `hd` | `string` | Optional | Streamline the login process for accounts owned by a Google Cloud organization. By including the Google Cloud organization domain (for example, `mycollege.edu`), you can indicate that the account selection UI should be optimized for accounts at that domain. To optimize for Google Cloud organization accounts generally instead of just one Google Cloud organization domain, set a value of an asterisk (`*`): `hd=*`. |
| `login_hint` | `string` | Optional | When your app knows which user it is trying to authenticate, it can provide this parameter as a hint to the authentication server. Passing this hint suppresses the account chooser and either pre-fills the email box on the sign-in form, or selects the proper session, which can help you avoid problems that occur if your app logs in the wrong user account. The value can be either an email address or the `sub` string, which is equivalent to the user's Google ID. |
| `prompt` | `string` | Optional | A space-delimited list of string values that specifies whether the Authorization Server prompts the user for reauthentication and consent. Possible values: `none` (no UI), `consent` (prompt for consent), `select_account` (prompt to select account). |
| `hl` | `string` | Optional | A [BCP 47](https://tools.ietf.org/html/bcp47) language tag used to specify the display language for the sign-in, account chooser, and consent screens. Use of this parameter is discouraged, as browser settings and Google Account preferences are typically better indicators of user language preference. |
| `include_granted_scopes` | `boolean` | Optional | If this parameter is provided with the value `true`, and the authorization request is granted, the authorization will include any previous authorizations granted to this user/application combination for other scopes; see [Incremental authorization](https://developers.google.com/identity/protocols/oauth2/web-server#incrementalAuth). |
| `claims` | `object` | Optional | The [claims parameter](https://openid.net/specs/openid-connect-core-1_0-final.html#ClaimsParameter) is used to specify one or more optional fields to include in the UserInfo Response or ID Token. To request the `auth_time` claim, use `claims={\"id_token\":{\"auth_time\":{\"essential\":true}}}`. |

### Response parameters

The Authorization Response is delivered to the Client's Redirection URI
(`redirect_uri`) using an HTTP `GET` redirect. The response parameters are
appended to the Redirection URI in the query string or URL fragment, depending
on the `response_type` and `response_mode`.

| Parameter | Type | Description |
|---|---|---|
| `iss` | `string` | The issuer identifier. Per [RFC 9207](https://datatracker.ietf.org/doc/html/rfc9207), this parameter is always returned and set to `https://accounts.google.com`. |
| `code` | `string` | The Authorization Code that can be exchanged for an Access Token and ID Token. |
| `state` | `string` | The same value as the `state` parameter from the request. |
| `id_token` | `string` | A JSON Web Token (JWT) that contains identity information about the user. |
| `access_token` | `string` | The Access Token that can be sent to a Google API. |
| `token_type` | `string` | The type of token returned. Always `Bearer`. |
| `expires_in` | `integer` | The lifetime of the access token in seconds, relative to the time the token was issued. |
| `scope` | `string` | The scopes of access granted by the `code` or `access_token` expressed as a list of space-delimited, case-sensitive strings. |
| `error` | `string` | An error code if the request failed. |
| `error_description` | `string` | A description of the error if the request failed. |

### Error Responses

For the Authorization Endpoint, errors are either returned to the Client's
`redirect_uri` as parameters in the query string or URL fragment, or displayed
to the user as a Google-hosted error page.

#### Redirected Errors

The following error codes may be returned to your `redirect_uri`:

| Error | Description |
|---|---|
| `access_denied` | The user or the Authorization Server denied the request. |
| `invalid_request` | The request is missing a required parameter, includes an invalid parameter value, includes a parameter more than once, or is otherwise malformed. |
| `unauthorized_client` | The client is not authorized to request an Authorization Code using this method. |
| `unsupported_response_type` | The Authorization Server does not support obtaining an Authorization Code using this method. |
| `invalid_scope` | The requested scope is invalid, unknown, or malformed. |

#### User-facing Errors

In some cases, such as when the `client_id` or `redirect_uri` is invalid, the
Authorization Server cannot safely redirect the user back to your application.
In these instances, an error page is displayed directly to the user.

| Error | Description |
|---|---|
| `admin_policy_enforced` | The Google Account is unable to authorize one or more scopes requested due to the policies of their Google Workspace administrator. See the [Google Workspace Admin help article](https://support.google.com/a/answer/7281227) for more information about how an administrator may restrict access until access is explicitly granted to your OAuth Client Identifier. |
| `disallowed_useragent` | The authorization endpoint is displayed inside an embedded user-agent disallowed by Google's [OAuth 2.0 Policies](https://developers.google.com/identity/protocols/oauth2/policies#browsers). |
| `org_internal` | The OAuth Client Identifier in the request is part of a project limiting access to Google Accounts in a specific [Google Cloud Organization](https://cloud.google.com/resource-manager/docs/cloud-platform-resource-hierarchy#organizations). |
| `deleted_client` | The OAuth client being used to make the request has been deleted. Deletion can happen manually or automatically in the case of [unused clients](https://support.google.com/cloud/answer/15549257#unused-client-deletion). |
| `invalid_grant` | The `code_challenge` parameter is invalid or missing when using PKCE. When refreshing an Access Token or using incremental authorization, the token may have expired, been invalidated, or the user account may have been deleted or disabled. |
| `redirect_uri_mismatch` | The `redirect_uri` passed in the request does not match an authorized Redirection URI for the Client Identifier. Review authorized Redirection URIs in the [Google Cloud console](https://console.developers.google.com/auth/clients). This error may also occur if the request uses the deprecated OAuth out-of-band (OOB) flow. |
| `invalid_client` | The origin from which the request was made is not authorized for this client, the client configuration is incorrect, or the OAuth Client Secret is incorrect. |
| `origin_mismatch` | The scheme, domain, and/or port of the JavaScript originating the authorization request does not match an authorized JavaScript origin URI registered for the OAuth Client Identifier. Review authorized JavaScript origins in the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `invalid_request` | There was something wrong with the request. This could be due to a malformed request, missing required parameters, or the use of an authorization method that Google doesn't support. |

## Token Endpoint

The Token Endpoint is used to exchange an Authorization Code for an Access Token
and an ID Token.

**URL:** `https://oauth2.googleapis.com/token`

**Supported request method:** `POST`

### Request parameters (Authorization Code Grant)

| Parameter | Type | Required | Description |
|---|---|---|---|
| `code` | `string` | Required | The Authorization Code received from the authorization endpoint. |
| `client_id` | `string` | Required | The Client Identifier for your application, obtained from the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `client_secret` | `string` | Required | The Client Secret for your application, obtained from the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `redirect_uri` | `string` | Required | The Redirection URI used in the initial authorization request. Redirect URIs and authorized JavaScript origins must follow the validation rules outlined in the [OAuth 2.0 URI validation](https://developers.google.com/identity/protocols/oauth2/web-server#uri-validation) documentation. |
| `grant_type` | `string` | Required | Must be set to `authorization_code`. |

### Request parameters (Device Flow)

| Parameter | Type | Required | Description |
|---|---|---|---|
| `client_id` | `string` | Required | The Client Identifier for your application, obtained from the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `client_secret` | `string` | Required | The Client Secret for your application, obtained from the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `device_code` | `string` | Required | The `device_code` returned by the device authorization endpoint. |
| `grant_type` | `string` | Required | Must be set to `urn:ietf:params:oauth:grant-type:device_code`. |

### Request parameters (Refreshing an Access Token)

| Parameter | Type | Required | Description |
|---|---|---|---|
| `client_id` | `string` | Required | The Client Identifier for your application, obtained from the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `client_secret` | `string` | Required | The Client Secret for your application, obtained from the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `grant_type` | `string` | Required | Must be set to `refresh_token` as defined in the [OpenID Connect Refresh Tokens](https://openid.net/specs/openid-connect-core-1_0-final.html#RefreshTokens) specification. |
| `refresh_token` | `string` | Required | The Refresh Token returned from the Authorization Code exchange. |
| `scope` | `string` | Optional | A space-delimited list of scopes requested for the new Access Token. The requested scopes must be a subset of the scopes granted in the original authorization request. |

### Response body

The response fields are returned in a JSON object in the body of the HTTP
response to the requester's `POST` request to
`https://oauth2.googleapis.com/token`.

| Field | Type | Description |
|---|---|---|
| `access_token` | `string` | The Access Token that can be sent to a Google API. |
| `expires_in` | `integer` | The lifetime of the Access Token in seconds, relative to the time the token was issued. |
| `id_token` | `string` | A JSON Web Token (JWT) that contains identity information about the user. This token is returned during the initial Authorization Code exchange and can also be returned during a Refresh Token request if the `openid` scope was granted. |
| `scope` | `string` | The scopes of access granted by the `access_token` expressed as a list of space-delimited, case-sensitive strings. |
| `token_type` | `string` | The type of token returned. Always `Bearer`. |
| `refresh_token` | `string` | (Optional) A token that can be used to obtain new Access Tokens. This field is only returned in the initial exchange of an Authorization Code if `access_type=offline` was requested. |
| `refresh_token_expires_in` | `integer` | (Optional) The remaining lifetime of the Refresh Token in seconds. This value is only set when the user grants [time-based access](https://developers.google.com/identity/protocols/oauth2/web-server#time-based-access). |

### Error responses

If the request fails, the Authorization Server returns a JSON object with the
following fields:

| Field | Type | Description |
|---|---|---|
| `error` | `string` | An error code. |
| `error_description` | `string` | A description of the error if the request failed. |

The following table describes the potential error codes and their associated
HTTP status codes:

| Error | HTTP Status Code | Description |
|---|---|---|
| `invalid_request` | `400` | The request is missing a required parameter, includes an invalid parameter value, or is otherwise malformed. |
| `invalid_client` | `401` | The client authentication failed. For example, the `client_id` or `client_secret` is invalid, or the client type is incorrect. |
| `invalid_grant` | `400` | The provided Authorization Code, Refresh Token, or device code is invalid, expired, revoked, or does not match the Redirection URI used in the authorization request. |
| `unauthorized_client` | `400` | The authenticated client is not authorized to use this authorization Grant Type. |
| `unsupported_grant_type` | `400` | The authorization Grant Type is not supported by the Authorization Server. |
| `invalid_scope` | `400` | The requested scope is invalid, unknown, or malformed. |
| `authorization_pending` | `428` | (Device Flow) The user has not yet completed the authorization flow. |
| `slow_down` | `429` | (Device Flow) The device is sending polling requests too frequently. |
| `access_denied` | `403` | (Device Flow) The user refused to grant access to the device. |
| `expired_token` | `400` | (Device Flow) The `device_code` has expired. |
| `admin_policy_enforced` | `400` | The user is unable to authorize the requested scopes due to policies enforced by their Google Workspace administrator. |
| `org_internal` | `403` | The Client Identifier is part of a project that limits access to a specific Google Cloud Organization. |

## Device Authorization Endpoint

The Device Authorization Endpoint is used in the [OAuth 2.0 Device Flow](https://developers.google.com/identity/protocols/oauth2/limited-input-device)
to obtain a User Code and verification URL for devices with limited input
capabilities.

**URL:** `https://oauth2.googleapis.com/device/code`

**Supported request method:** `POST`

### Request parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `client_id` | `string` | Required | The Client Identifier for your application, obtained from the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `scope` | `string` | Required | A space-delimited list of scopes that identify the resources that your application could access on the user's behalf. |

### Response body

The response is a JSON object that contains the following fields:

| Field | Type | Description |
|---|---|---|
| `device_code` | `string` | A value that Google uniquely assigns to identify the device that runs the app requesting authorization. |
| `user_code` | `string` | A case-sensitive value that identifies to Google the scopes that the application is requesting access to. Your user interface will instruct the user to enter this value on a separate device with richer input capabilities. |
| `verification_url` | `string` | A URL that the user must navigate to, on a separate device, to enter the `user_code` and grant or deny access to your application. |
| `expires_in` | `integer` | The length of time, in seconds, that the `device_code` and `user_code` are valid. |
| `interval` | `integer` | The length of time, in seconds, that your device should wait between polling requests. |

## Revocation Endpoint

The Revocation Endpoint allows your application to revoke an Access Token or a
Refresh Token.

**URL:** `https://oauth2.googleapis.com/revoke`

**Supported request method:** `POST`

### Request parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `token` | `string` | Required | The Access Token or Refresh Token that you want to revoke. |

### Response body

If the revocation is successful, the response is an empty HTTP `200 OK`. If the
revocation fails, an error response is returned in a JSON object.

| Field | Type | Description |
|---|---|---|
| `error` | `string` | An error code if the request failed. |
| `error_description` | `string` | A description of the error if the request failed. |

The following table describes the potential error codes:

| Error | Description |
|---|---|
| `invalid_token` | The token provided in the request is already expired or has already been revoked. |
| `invalid_request` | The request is missing a required parameter, includes an invalid parameter value, or is otherwise malformed. |

## UserInfo Endpoint

The UserInfo Endpoint returns profile information about the authenticated user.

**URL:** `https://openidconnect.googleapis.com/v1/userinfo`

**Supported request methods:** `GET`, `POST`

### Request headers

| Header | Description |
|---|---|
| `Authorization` | **Required.** Must be set to `Bearer: access_token`. |

### Response body

The response fields are returned in a JSON object in the body of the HTTP
response to the requester's `GET` or `POST` request to
`https://openidconnect.googleapis.com/v1/userinfo`.

| Field | Type | Description |
|---|---|---|
| `sub` | `string` | **Required.** An identifier for the user, unique among all Google Accounts and never reused. Case-sensitive string not exceeding 255 characters. |
| `name` | `string` | The user's full name, in a displayable form. |
| `given_name` | `string` | The user's given name(s) or first name(s). |
| `family_name` | `string` | The user's surname(s) or last name(s). |
| `picture` | `string` | The URL of the user's profile picture. |
| `email` | `string` | The user's email address. |
| `email_verified` | `boolean` | Whether the user's email address has been verified. |
| `hd` | `string` | The hosted domain associated with the user's Google Workspace or Cloud organization. |

## Tokeninfo Endpoint

The `tokeninfo` endpoint is a Google-specific implementation used to validate an
ID token **for debugging purposes**.

**URL:** `https://oauth2.googleapis.com/tokeninfo`

**Supported request methods:** `GET`, `POST`

### Request parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `id_token` | `string` | Required | The ID Token to validate. |

### Response body

The response fields are returned in a JSON object in the body of the HTTP
response to the requester's `GET` or `POST` request to
`https://oauth2.googleapis.com/tokeninfo`.

| Field | Type | Description |
|---|---|---|
| `iss` | `string` | The issuer identifier. Always `https://accounts.google.com`. |
| `sub` | `string` | An identifier for the user, unique among all Google Accounts and never reused. |
| `aud` | `string` | The audience for which the ID Token is intended. This is your application's Client Identifier, obtained from the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `iat` | `integer` | Time at which the JWT was issued. Represented as the number of seconds from 1970-01-01T0:0:0Z as measured in UTC. |
| `exp` | `integer` | Expiration time on or after which the ID Token must not be accepted for processing. Represented as the number of seconds from 1970-01-01T0:0:0Z as measured in UTC. |
| `email` | `string` | The user's email address. |
| `email_verified` | `string` | Whether the user's email address has been verified. Value is a string `"true"` or `"false"`. |
| `access_type` | `string` | The access type requested in the original authorization request. |
| `azp` | `string` | The Client Identifier of the authorized presenter, obtained from the [Google Cloud console](https://console.developers.google.com/auth/clients). |
| `scope` | `string` | The space-delimited list of [scopes](https://developers.google.com/identity/protocols/oauth2/scopes) granted to the token. |
| `alg` | `string` | The algorithm used to sign the ID Token. |
| `kid` | `string` | The key ID used to sign the ID Token. |
| `typ` | `string` | The type of token. Always `JWT`. |