package org.redischool.attendance.details;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OktaGroupsCourseAccessValidatorTest {
    @Mock
    private Authentication authentication;

    private OktaGroupsCourseAccessValidator courseAccessValidator;

    @BeforeEach
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

    @Test
    public void validatePermissions_throws_whenUserDoesNotHavePermissions() throws UserAccessDeniedException {
        Map<Object, Object> authDetails = new HashMap<>();
        authDetails.put("tokenValue", tokenWithGroup("Chasing Unisus"));
        when(authentication.getDetails()).thenReturn(authDetails);

        assertThatThrownBy(() -> courseAccessValidator.validatePermissions(authentication, "Chasing Unicorns"))
                .isInstanceOf(UserAccessDeniedException.class);
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
