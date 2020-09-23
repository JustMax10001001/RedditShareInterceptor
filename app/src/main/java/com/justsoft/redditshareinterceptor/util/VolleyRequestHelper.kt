package com.justsoft.redditshareinterceptor.util

import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

class VolleyRequestHelper(
    private val requestQueue: RequestQueue
) : RequestHelper {

    override fun readHttpTextResponse(
        requestUrl: String,
        params: MutableMap<String, String>
    ): String {
        val requestFuture = RequestFuture.newFuture<String>()
        requestQueue.add(
            object : StringRequest(Method.GET, requestUrl, requestFuture, requestFuture) {

                override fun getParams(): MutableMap<String, String> = params
            }.setRetryPolicy(
                DefaultRetryPolicy(
                    10000,
                    2,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            )
        )
        return requestFuture.get()
    }

    override fun readHttpJsonResponse(
        requestUrl: String,
        params: MutableMap<String, String>
    ): JSONObject {
        val requestFuture = RequestFuture.newFuture<JSONObject>()
        requestQueue.add(
            object : JsonObjectRequest(Method.GET, requestUrl, null, requestFuture, requestFuture) {

                override fun getParams(): MutableMap<String, String> = params
            }.setRetryPolicy(
                DefaultRetryPolicy(
                    10000,
                    2,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            )
        )
        return requestFuture.get()
    }
}