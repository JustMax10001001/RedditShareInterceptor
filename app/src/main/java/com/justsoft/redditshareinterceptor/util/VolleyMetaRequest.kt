package com.justsoft.redditshareinterceptor.util

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


open class VolleyMetaRequest : JsonObjectRequest {
    constructor(
        method: Int,
        url: String?,
        jsonRequest: JSONObject?,
        listener: Response.Listener<JSONObject?>?,
        errorListener: Response.ErrorListener?
    ) : super(method, url, jsonRequest, listener, errorListener) {
    }

    constructor(
        url: String?,
        jsonRequest: JSONObject?,
        listener: Response.Listener<JSONObject?>?,
        errorListener: Response.ErrorListener?
    ) : super(url, jsonRequest, listener, errorListener) {
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
        return try {
            val jsonString = String(
                response.data,
                Charset.forName(HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET))
            )

            val jsonResponse = JSONObject(jsonString)
            jsonResponse.put("headers", JSONObject(response.headers.toMap()))
            Response.success(
                jsonResponse,
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        } catch (je: JSONException) {
            Response.error(ParseError(je))
        }
    }


}