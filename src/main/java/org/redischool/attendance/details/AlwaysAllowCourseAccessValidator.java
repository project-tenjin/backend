package org.redischool.attendance.details;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
class AlwaysAllowCourseAccessValidator implements CourseAccessValidator {
    @Override
    public void validatePermissions(Authentication authentication, String courseName) {
        // allow access to all courses
    }

    @Override
    public boolean hasPermissions(Authentication authentication, String courseName) {
        return true;
    }
}