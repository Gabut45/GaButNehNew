package com.ujicoba.gabut.api;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ujicoba.gabut.BuildConfig;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SantaCoderJsonObjRequest extends JsonObjectRequest {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String SECRET_PREFIX = "Bearer ";


    public SantaCoderJsonObjRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, "application/json");
        headers.put(AUTHORIZATION_HEADER, SECRET_PREFIX + BuildConfig.SECRET_KEY);
        return headers;
    }
}
