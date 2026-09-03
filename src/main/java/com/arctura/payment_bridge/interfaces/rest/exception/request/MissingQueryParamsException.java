package com.arctura.payment_bridge.interfaces.rest.exception.request;

public class MissingQueryParamsException extends RequestException {
    private static final String CODE = "MISSING_QUERY_PARAMS";

    public MissingQueryParamsException(String... params) {
        super(CODE, buildMessage(params), buildDetail(params));
    }

    private static String buildMessage(String... params) {
        if (params == null || params.length == 0) {
            return "Required query parameter(s) are missing";
        }
        if (params.length == 1) {
            return "Required query parameter '" + params[0] + "' is missing";
        }
        return "Required query parameters " + String.join(", ", params) + " are missing";
    }

    private static String buildDetail(String... params) {
        return params == null ? "" : String.join(",", params);
    }
}
