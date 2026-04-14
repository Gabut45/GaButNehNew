package com.ujicoba.gabut.api;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.ujicoba.gabut.BuildConfig;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SantaCoderStringRequest extends StringRequest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String SECRET_PREFIX = "Bearer ";

    private final JSONObject params;

    public SantaCoderStringRequest(int method, String url, JSONObject params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.params = params;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION_HEADER, SECRET_PREFIX + BuildConfig.SECRET_KEY);
        return headers;
    }

    @Override
    public byte[] getBody() {
        return params.toString().getBytes();
    }
}

