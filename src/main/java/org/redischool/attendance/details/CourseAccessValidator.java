package org.redischool.attendance.details;

import org.springframework.security.core.Authentication;

public interface CourseAccessValidator {
    void validatePermissions(Authentication authentication, String courseName) throws UserAccessDeniedException;

    boolean hasPermissions(Authentication authentication, String courseName);
}
