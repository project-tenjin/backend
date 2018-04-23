package org.redischool.attendance.details;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;


@Controller
public class CourseDetailsController {

    private CourseDetailsRepository courseDetailsRepository;
    private CourseHelper courseHelper;
    private CourseAccessValidator courseAccessValidator;

    @Autowired
    public CourseDetailsController(
            CourseDetailsRepository courseDetailsRepository,
            CourseHelper courseHelper,
            CourseAccessValidator courseAccessValidator) {
        this.courseDetailsRepository = courseDetailsRepository;
        this.courseHelper = courseHelper;
        this.courseAccessValidator = courseAccessValidator;
    }

    @GetMapping("/courses")
    String showCourse(@PathParam("name") String name,
                      @PathParam("error") String error,
                      Model model,
                      Authentication authentication) throws IOException, UserAccessDeniedException {
        courseAccessValidator.validatePermissions(authentication, name);

        model.addAttribute("error", error);

        CourseDetails courseDetails = courseDetailsRepository.getCourseDetails(name);

        model.addAttribute("datesMap", courseHelper.getFormattedDatesMap(courseDetails));
        model.addAttribute("courseDetails", courseDetails);
        model.addAttribute("closestCourseDate", courseHelper.closestCourseDate(new Date(), courseDetails));

        return "courseDetail";
    }

    @GetMapping("/attendance")
    @ResponseBody
    public CourseAttendance getAttendance(
            @RequestParam String courseName,
            @RequestParam String date,
            Authentication authentication) throws IOException, UserAccessDeniedException {
        courseAccessValidator.validatePermissions(authentication, courseName);

        CourseAttendance courseAttendance = new CourseAttendance();

        courseAttendance.setCourseName(courseName);
        courseAttendance.setDate(date);
        courseAttendance.setAttendances(courseDetailsRepository.getAttendance(courseName, date));

        return courseAttendance;
    }

    @PostMapping("/attendance")
    public String postCourseAttendance(
            @ModelAttribute CourseAttendance courseAttendance,
            Authentication authentication
    ) throws IOException, UserAccessDeniedException {
        final String courseName = courseAttendance.getCourseName();
        courseAccessValidator.validatePermissions(authentication, courseName);
        try {
            courseDetailsRepository.updateAttendance(courseName,
                    courseAttendance.getDate(),
                    courseAttendance.getAttendances());
            return "redirect:/thanks";

        } catch (IllegalArgumentException e) {
            return "redirect:/courses?name=" + courseName + "&error=Date required";
        }
    }

    @ExceptionHandler({NoSuchElementException.class, GoogleJsonResponseException.class})
    ResponseEntity<String> handleNoSuchElementException(Exception ex) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({UserAccessDeniedException.class})
    String handleUserAccessDeniedException(Exception ex) {
        return "403";
    }

}


