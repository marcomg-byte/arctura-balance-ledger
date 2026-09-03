package com.arctura.payment_bridge.interfaces.rest.exception.request;

public class MissingRequestBodyPropsException extends RequestException {
    private static final String CODE = "MISSING_REQUEST_BODY_PROPS";

    public MissingRequestBodyPropsException(String... missingProps) {
      super(CODE, buildMessage(missingProps), buildDetail(missingProps));
    }

    private static String buildMessage(String... missingProps) {
        if (missingProps == null || missingProps.length == 0) {
            return "Request body is required but was not provided";
        }
        if (missingProps.length == 1) {
            return "Required request body property '" + missingProps[0] + "' is missing";
        }
        return "Required request body properties " + String.join(", ", missingProps) + " are missing";
    }

    private static String buildDetail(String... missingProps) {
        return missingProps == null ? "" : String.join(",", missingProps);
    }
}
