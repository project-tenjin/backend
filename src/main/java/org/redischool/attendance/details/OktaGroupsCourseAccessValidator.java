package org.redischool.attendance.details;

import org.codehaus.jackson.map.ObjectMapper;
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
    @Override
    public void validatePermissions(Authentication authentication, String courseName) throws IOException, UserAccessDeniedException {
        ArrayList groups = (ArrayList) parseToken(authentication).get("groups");
        if (!groups.contains(courseName)) throw new UserAccessDeniedException("You do not have permissions");
    }

    private Map parseToken(Authentication authentication) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map authDetails = objectMapper.convertValue(authentication.getDetails(), Map.class);
        Jwt jwt = JwtHelper.decode((String) authDetails.get("tokenValue"));
        return objectMapper.readValue(jwt.getClaims(), Map.class);
    }
}
