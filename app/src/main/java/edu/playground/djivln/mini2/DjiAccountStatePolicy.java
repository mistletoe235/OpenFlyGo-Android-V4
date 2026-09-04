package edu.playground.djivln.mini2;

final class DjiAccountStatePolicy {
    private DjiAccountStatePolicy() {}

    static boolean isLoggedIn(String stateName) {
        return "AUTHORIZED".equals(stateName) || "NOT_AUTHORIZED".equals(stateName);
    }

    static boolean shouldOfferLogin(String stateName) {
        return "NOT_LOGGED_IN".equals(stateName) || "TOKEN_OUT_OF_DATE".equals(stateName);
    }
}
