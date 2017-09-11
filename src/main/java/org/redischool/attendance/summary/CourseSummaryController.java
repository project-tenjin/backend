package org.redischool.attendance.summary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CourseSummaryController {

	private CourseSummaryRepository courseSummaryRepository;

	@Autowired
	public CourseSummaryController(CourseSummaryRepository courseSummaryRepository) {
		this.courseSummaryRepository = courseSummaryRepository;
	}

	@GetMapping("/")
	public String listCourses(Model model) {
		model.addAttribute("courses", courseSummaryRepository.getCourses());
		return "courseList";
	}
}
