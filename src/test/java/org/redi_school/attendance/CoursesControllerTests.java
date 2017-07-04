package org.redi_school.attendance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class CoursesControllerTests {

    private MockMvc mvc;

    @Mock
    CoursesRepository coursesRepository;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(new CoursesController(coursesRepository)).build();
    }

    @Test
    public void testRendersCourses() throws Exception {
        given(coursesRepository.getCourses()).willReturn(Arrays.asList(new CourseSummary(0, "class1"), new CourseSummary(0, "class2")));
        this.mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("courseList"))
                .andExpect(model().attribute("courses", Arrays.asList(new CourseSummary(0, "class1"), new CourseSummary(0, "class2"))));
    }

    @Test
    public void testRenderCourseDetails() throws Exception {
        given(coursesRepository.getCourses()).willReturn(Arrays.asList(new CourseSummary(0, "class1"), new CourseSummary(1, "class2")));
        given(coursesRepository.getCourseDetails("class2")).willReturn(new CourseDetails("class2", Arrays.asList("Student-name", "Student-other-name")));

        this.mvc.perform(get("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("courseDetail"))
                .andExpect(model().attribute("courseDetails", new CourseDetails("class2", Arrays.asList(
                        "Student-name",
                        "Student-other-name")))
                );
    }

    @Test
    public void testRenderCourseDetailsForNonExistingCourse() throws Exception {
        given(coursesRepository.getCourses()).willReturn(Arrays.asList(new CourseSummary(0, "class1"), new CourseSummary(1, "class2")));
        this.mvc.perform(get("/courses/2"))
                .andExpect(status().isNotFound());
    }
}
