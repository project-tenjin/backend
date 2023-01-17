package org.redischool.attendance.details;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OktaGroupsCourseAccessValidatorTest {
    @Mock
    private Authentication authentication;

    private OktaGroupsCourseAccessValidator courseAccessValidator;

    @Before
    public void setUp() {
        courseAccessValidator = new OktaGroupsCourseAccessValidator(new ObjectMapper());
    }

    @Test
    public void validatePermissions_doesNotThrow_whenUserHasPermissions() throws UserAccessDeniedException {
        HashMap<Object, Object> authDetails = new HashMap<>();
        authDetails.put("tokenValue", tokenWithGroup("Chasing Unicorns"));
        when(authentication.getDetails()).thenReturn(authDetails);

        courseAccessValidator.validatePermissions(authentication, "Chasing Unicorns");
    }

    @Test(expected = UserAccessDeniedException.class)
    public void validatePermissions_throws_whenUserDoesNotHavePermissions() throws UserAccessDeniedException {
        HashMap<Object, Object> authDetails = new HashMap<>();
        authDetails.put("tokenValue", tokenWithGroup("Chasing Unisus"));
        when(authentication.getDetails()).thenReturn(authDetails);

        courseAccessValidator.validatePermissions(authentication, "Chasing Unicorns");
    }

    @Test
    public void hasPermissions_returnsTrue_whenUserHasPermissions() {
        HashMap<Object, Object> authDetails = new HashMap<>();
        authDetails.put("tokenValue", tokenWithGroup("Chasing Unicorns"));
        when(authentication.getDetails()).thenReturn(authDetails);

        assertThat(courseAccessValidator.hasPermissions(authentication, "Chasing Unicorns")).isTrue();
    }

    @Test
    public void hasPermissions_returnsFalse_whenUserDoesNotHavePermissions() {
        HashMap<Object, Object> authDetails = new HashMap<>();
        authDetails.put("tokenValue", tokenWithGroup("Chasing Unisus"));
        when(authentication.getDetails()).thenReturn(authDetails);

        assertThat(courseAccessValidator.hasPermissions(authentication, "Chasing Unicorns")).isFalse();
    }

    private String tokenWithGroup(String groupName) {
        return JwtHelper.encode("{\"groups\":[\"" + groupName + "\"]}", new MacSigner("FOO")).getEncoded();
    }
}
