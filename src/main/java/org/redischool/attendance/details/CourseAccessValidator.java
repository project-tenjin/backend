package org.redischool.attendance.details;

import org.springframework.security.core.Authentication;

import java.io.IOException;

public interface CourseAccessValidator {
    void validatePermissions(Authentication authentication, String courseName) throws UserAccessDeniedException, IOException;
}
