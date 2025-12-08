package io.mosip.resident.util;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.when;

/**
 * Utility class for setting up authentication details in tests.
 *
 * @author Kamesh Shekhar Prasad
 */

public class AuthenticationTestUtil {

    private static String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJubEpTaUExM2tPUWhZQ0JxMEVKSkRlWnFTOGsybDB3MExUbmQ1WFBCZ20wIn0." +
            "eyJleHAiOjE2NzIxMjU0NjEsImlhdCI6MTY3MjAzOTA2MSwianRpIjoiODc5YTdmYTItZWZhYy00YTQwLTkxODQtNzZiM2FhMWJiODg0IiwiaXNzIjoiaHR0c" +
            "HM6Ly9pYW0uZGV2Lm1vc2lwLm5ldC9hdXRoL3JlYWxtcy9tb3NpcCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJiNTc3NjkzYi0xOWI1LTRlYTktYWEzNy1kMT" +
            "EzMjdkOGRkNzkiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJtb3NpcC1yZXNpZGVudC1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiNWNmZWIzNTgtNGY1Ni00NjM" +
            "0LTg3NmQtNGFjNzk1OTYyYWRkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9u" +
            "IiwiZGVmYXVsdC1yb2xlcy1tb3NpcCJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYW" +
            "Njb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoid2FsbGV0X2JpbmRpbmcgYXV0aC5oaXN0b3J5LnJlYWRvbmx5IG1pY3JvcHJvZmlsZS1q" +
            "d3QgaWRlbnRpdHkucmVhZG9ubHkgaWRhX3Rva2VuIG9mZmxpbmVfYWNjZXNzIGFkZHJlc3MgdXBkYXRlX29pZGNfY2xpZW50IGNyZWRlbnRpYWwubWFuYWdlIH" +
            "ZpZC5tYW5hZ2UgZ2V0X2NlcnRpZmljYXRlIGFkZF9vaWRjX2NsaWVudCB2aWQucmVhZG9ubHkgaWRlbnRpdHkudXBkYXRlIG5vdGlmaWNhdGlvbnMubWFuYWdl" +
            "IGVtYWlsIHVwbG9hZF9jZXJ0aWZpY2F0ZSBhdXRoLnJlYWRvbmx5IGF1dGgubWV0aG9kLm1hbmFnZSBub3RpZmljYXRpb25zLnJlYWRvbmx5IGluZGl2aWR1YWxf" +
            "aWQgYXV0aC5oaXN0b3J5Lm1hbmFnZSB0ZXN0IHByb2ZpbGUgY2FyZC5tYW5hZ2Ugc2VuZF9iaW5kaW5nX290cCIsInNpZCI6IjVjZmViMzU4LTRmNTYtNDYzNC0" +
            "4NzZkLTRhYzc5NTk2MmFkZCIsInVwbiI6ImthbWVzaCIsImFkZHJlc3MiOnt9LCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJLYW1lc2ggU2hla2hh" +
            "ciIsImdyb3VwcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy1tb3NpcCJdLCJwcmVmZXJyZWRfdXNlcm5hb" +
            "WUiOiJrYW1lc2giLCJnaXZlbl9uYW1lIjoiS2FtZXNoIiwiZmFtaWx5X25hbWUiOiJTaGVraGFyIiwicGljdHVyZSI6ImlWQk9SdzBLR2dvQUFBQU5TVWhFVW" +
            "dBQUFBb0FBQUFLQ0FJQUFBQUNVRmpxQUFBQUFYTlNSMElBcnM0YzZRQUFBQVJuUVUxQkFBQ3hqd3Y4WVFVQUFBQUpjRWhaY3dBQUZpVUFBQllsQVVsU0pQQUF" +
            "BQUJDU1VSQlZDaFRiWXRCRWdBZ0NBTDcvNmVOaEJ5MDlxRGk2Z3BqWFpTeFVVOG8vanJmcERtY21ZMVFBT1doZ1Rzd3Y2c1NtOHpWaFVMbGdzdCsrOFQ1MUlq" +
            "WU5VSGRJKzRYWkhvQUFBQUFTVVZPUks1Q1lJST0iLCJlbWFpbCI6ImthbWVzaHNyMTMzOEBnbWFpbC5jb20ifQ.YLddWNd7ldiMvPhDK0HhXaKjEmeOE0T6wS" +
            "CjfN3mlwxDxHm2DzMHnwbKR5orEm1NRyCnUfGGm5IMVTdDnXz1iUAsU7zeKA2XOdH3zQgMUu-vqJpgRWRG-XJHakSyblfAFIVAILRi7rwJQjL7X1lhm1ZAqUX" +
            "Soh6kZBoOeYd_29RQQzFQNzpn_Ahk4GxQu_TLyvoWeNXpfx94om7TqrZYghtTg5_svku2P0NuFxzbWysPMjaHrEff0idKY94sKJ6eNpLXRXbJCPkAHtfVY0U3" +
            "YDQqWUpYjE3hQCZz0u_L8sieJIN3mYtjd12rfOrjEKu2fFGu5UbJRVqkmOw0egVGHw";;
    
    public static void getAuthUserDetailsFromAuthentication() {
        Authentication authentication= Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        MosipUserDto mosipUserDto = new MosipUserDto();
        mosipUserDto.setToken(token);
        // test the case where the principal is an AuthUserDetails object
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
    }
}
