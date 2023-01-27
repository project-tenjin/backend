package org.redischool.attendance.details;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Component
@Profile("!test")
public class OktaGroupsCourseAccessValidator implements CourseAccessValidator {

    private final ObjectMapper objectMapper;

    public OktaGroupsCourseAccessValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void validatePermissions(Authentication authentication, String courseName) throws UserAccessDeniedException {
        if (!hasPermissions(authentication, courseName)) {
            throw new UserAccessDeniedException("You do not have permissions");
        }
    }

    @Override
    public boolean hasPermissions(Authentication authentication, String courseName) {
        ArrayList groups = (ArrayList) parseToken(authentication).get("groups");
        return groups.contains(courseName);
    }

    private Map parseToken(Authentication authentication) {
        Map authDetails = objectMapper.convertValue(authentication.getDetails(), Map.class);
        Jwt jwt = JwtHelper.decode((String) authDetails.get("tokenValue"));

        try {
            return objectMapper.readValue(jwt.getClaims(), Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
