package com.arctura.payment_bridge.interfaces.rest.exception.request;

/**
 * Raised when one or more required query parameters are absent from a request.
 *
 * <p>Parameter names are included in the detail field as a comma-separated list
 * to keep the API error response machine-readable.</p>
 */
public class MissingQueryParamsException extends RequestException {
    private static final String CODE = "MISSING_QUERY_PARAMS";

    /**
     * Creates the exception for missing query parameters.
     *
     * @param params names of the missing query parameters
     */
    public MissingQueryParamsException(String... params) {
        super(CODE, buildMessage(params), buildDetail(params));
    }

    /**
     * Builds a human-readable validation message for the missing parameters.
     *
     * @param params names of the missing query parameters
     * @return message suitable for the API error response
     */
    private static String buildMessage(String... params) {
        if (params == null || params.length == 0) {
            return "Required query parameter(s) are missing";
        }
        if (params.length == 1) {
            return "Required query parameter '" + params[0] + "' is missing";
        }
        return "Required query parameters " + String.join(", ", params) + " are missing";
    }

    /**
     * Builds the machine-readable detail value for the missing parameters.
     *
     * @param params names of the missing query parameters
     * @return comma-separated parameter names, or an empty string when none are
     *         supplied
     */
    private static String buildDetail(String... params) {
        return params == null ? "" : String.join(",", params);
    }
}
