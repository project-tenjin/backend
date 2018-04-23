package org.redischool.attendance.details;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("test")
class AlwaysAllowCourseAccessValidator implements CourseAccessValidator {
    public void validatePermissions(Authentication authentication, String courseName) throws IOException, UserAccessDeniedException {
        // allow access to all courses
    }
}