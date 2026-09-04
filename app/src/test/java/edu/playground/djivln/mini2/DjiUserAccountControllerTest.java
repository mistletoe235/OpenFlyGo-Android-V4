package edu.playground.djivln.mini2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DjiUserAccountControllerTest {
    @Test public void authorizedAndNotAuthorizedAreBothLoggedIn() {
        assertTrue(DjiAccountStatePolicy.isLoggedIn("AUTHORIZED"));
        assertTrue(DjiAccountStatePolicy.isLoggedIn("NOT_AUTHORIZED"));
        assertFalse(DjiAccountStatePolicy.isLoggedIn("NOT_LOGGED_IN"));
        assertFalse(DjiAccountStatePolicy.isLoggedIn("TOKEN_OUT_OF_DATE"));
        assertFalse(DjiAccountStatePolicy.isLoggedIn("UNKNOWN"));
    }

    @Test public void onlyLoggedOutAndExpiredStatesOfferLogin() {
        assertTrue(DjiAccountStatePolicy.shouldOfferLogin("NOT_LOGGED_IN"));
        assertTrue(DjiAccountStatePolicy.shouldOfferLogin("TOKEN_OUT_OF_DATE"));
        assertFalse(DjiAccountStatePolicy.shouldOfferLogin("AUTHORIZED"));
        assertFalse(DjiAccountStatePolicy.shouldOfferLogin("NOT_AUTHORIZED"));
        assertFalse(DjiAccountStatePolicy.shouldOfferLogin("UNKNOWN"));
    }
}
