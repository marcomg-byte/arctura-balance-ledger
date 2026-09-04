package com.arctura.payment_bridge.interfaces.rest.exception.request;

/**
 * Raised when a JSON request body omits one or more required properties.
 *
 * <p>Missing property names are included in the detail field as a
 * comma-separated list to keep the error response easy to parse.</p>
 */
public class MissingRequestBodyPropsException extends RequestException {
    private static final String CODE = "MISSING_REQUEST_BODY_PROPS";

    /**
     * Creates the exception for missing request body properties.
     *
     * @param missingProps names of the missing properties
     */
    public MissingRequestBodyPropsException(String... missingProps) {
      super(CODE, buildMessage(missingProps), buildDetail(missingProps));
    }

    /**
     * Builds a human-readable validation message for the missing properties.
     *
     * @param missingProps names of the missing properties
     * @return message suitable for the API error response
     */
    private static String buildMessage(String... missingProps) {
        if (missingProps == null || missingProps.length == 0) {
            return "Request body is required but was not provided";
        }
        if (missingProps.length == 1) {
            return "Required request body property '" + missingProps[0] + "' is missing";
        }
        return "Required request body properties " + String.join(", ", missingProps) + " are missing";
    }

    /**
     * Builds the machine-readable detail value for the missing properties.
     *
     * @param missingProps names of the missing properties
     * @return comma-separated property names, or an empty string when none are
     *         supplied
     */
    private static String buildDetail(String... missingProps) {
        return missingProps == null ? "" : String.join(",", missingProps);
    }
}
