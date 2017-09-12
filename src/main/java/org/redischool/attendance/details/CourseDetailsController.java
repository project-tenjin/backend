package org.redischool.attendance.details;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.NoSuchElementException;


@Controller
public class CourseDetailsController {

    private CourseDetailsRepository courseDetailsRepository;

    @Autowired
    public CourseDetailsController(CourseDetailsRepository courseDetailsRepository) {
        this.courseDetailsRepository = courseDetailsRepository;
    }

    @GetMapping("/courses")
    String showCourse(@PathParam("name") String name,
                      @PathParam("error") String error,
                      Model model) {

        model.addAttribute("error", error);

        CourseDetails courseDetails = courseDetailsRepository.getCourseDetails(name);

        model.addAttribute("courseDetails", courseDetails);

        return "courseDetail";
    }

    @GetMapping("/attendance")
    @ResponseBody
    public CourseAttendance getAttendance(
            @RequestParam String courseName,
            @RequestParam String date) {

        CourseAttendance courseAttendance = new CourseAttendance();

        courseAttendance.setCourseName(courseName);
        courseAttendance.setDate(date);
        courseAttendance.setAttendances(courseDetailsRepository.getAttendance(courseName, date));

        return courseAttendance;
    }

    @PostMapping("/attendance")
    public String postCourseAttendance(@ModelAttribute CourseAttendance courseAttendance) {
        final String courseName = courseAttendance.getCourseName();
        try {
            courseDetailsRepository.updateAttendance(courseName,
                    courseAttendance.getDate(),
                    courseAttendance.getAttendances());
            return "redirect:/thanks";

        } catch (IllegalArgumentException e) {
            return "redirect:/courses?name=" + courseName + "&error=Date required";
        }
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<String> handleNoSuchElementException() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}


