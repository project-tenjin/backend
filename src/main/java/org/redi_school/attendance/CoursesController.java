package org.redi_school.attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.NoSuchElementException;

@Controller
public class CoursesController {

    private CoursesRepository coursesRepository;

    @Autowired
    public CoursesController(CoursesRepository coursesRepository) {
        this.coursesRepository = coursesRepository;
    }

    @GetMapping("/")
    String listCourses(Model model) {
        model.addAttribute("courses", coursesRepository.getCourses());
        return "courseList";
    }

    @GetMapping("/courses/{id}")
    String showCourse(@PathVariable int id, Model model) {
        Sheet currentCourse = coursesRepository.getCourses().stream()
                .filter((Sheet course) -> course.getId() == id)
                .findFirst().get();
        model.addAttribute("course", currentCourse);
        return "courseDetail";
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<String> handleNoSuchElementException() {
        return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
    }
}
