package org.redischool.attendance.details;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;

import java.io.IOException;
import java.util.HashMap;

import static org.mockito.Mockito.when;

public class OktaGroupsCourseAccessValidatorTest {
    Authentication authentication;
    OktaGroupsCourseAccessValidator courseAccessValidator;

    @Before
    public void setUp() throws Exception {
        authentication = Mockito.mock(Authentication.class);
        courseAccessValidator = new OktaGroupsCourseAccessValidator();
    }

    @Test
    public void validatePermissions_doesNotThrowWhenUserHasPermissions() throws IOException, UserAccessDeniedException {
        HashMap<Object, Object> authDetails = new HashMap<>();
        authDetails.put("tokenValue", tokenWithGroup("Chasing Unicorns"));
        when(authentication.getDetails()).thenReturn(authDetails);

        courseAccessValidator.validatePermissions(authentication, "Chasing Unicorns");
    }

    @Test(expected = UserAccessDeniedException.class)
    public void validatePermissions_throwWhenUserDoesNotHavePermissions() throws IOException, UserAccessDeniedException {
        HashMap<Object, Object> authDetails = new HashMap<>();
        authDetails.put("tokenValue", tokenWithGroup("Chasing Unisus"));
        when(authentication.getDetails()).thenReturn(authDetails);

        courseAccessValidator.validatePermissions(authentication, "Chasing Unicorns");
    }

    private String tokenWithGroup(String groupName) {
        return JwtHelper.encode("{\"groups\":[\"" + groupName + "\"]}", new MacSigner("FOO")).getEncoded();
    }
}