package org.redi_school.attendance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        CourseDetails returnedCourseDetails = new CourseDetails("class2",
                Arrays.asList("Student-name", "Student-other-name"),
                Arrays.asList("4/24", "4/27"),
                new HashMap<String, Map<String, String>>() {{
                    put("4/24", new HashMap<String, String>() {{ put("student-name", "P"); }});
                    put("4/27", new HashMap<String, String>() {{ put("student-name", "U"); }});
                }}
        );
        given(coursesRepository.getCourses()).willReturn(Arrays.asList(new CourseSummary(0, "class1"), new CourseSummary(1, "class2")));
        given(coursesRepository.getCourseDetails("class2")).willReturn(returnedCourseDetails);

        this.mvc.perform(get("/courses/?name=" + "class2"))
                .andExpect(status().isOk())
                .andExpect(view().name("courseDetail"))
                .andExpect(model().attribute("courseDetails", returnedCourseDetails));
    }

    @Test
    public void testRenderCourseDetailsWithDate() throws Exception {
        String expectedClass = "class2";
        String expectedDate = "4/27";

        CourseDetails returnedCourseDetails = new CourseDetails("class2",
                Arrays.asList("Student-name", "Student-other-name"),
                Arrays.asList(expectedDate, expectedDate),
                new HashMap<String, Map<String, String>>() {{
                    put("4/27", new HashMap<String, String>() {{ put("student-name", "U"); }});
                }}
        );
        given(coursesRepository.getCourses()).willReturn(Arrays.asList(new CourseSummary(0, "class1"), new CourseSummary(1, expectedClass)));
        given(coursesRepository.getCourseDetails(expectedClass)).willReturn(returnedCourseDetails);

        this.mvc.perform(get("/courses/?name=" + expectedClass + "&date=" + expectedDate))
                .andExpect(status().isOk())
                .andExpect(view().name("courseDetail"))
                .andExpect(model().attribute("courseDetails", returnedCourseDetails));
    }

    @Test
    public void testRenderCourseDetailsWithDateForNonExistingDate() throws Exception {
        assertThat(1).isEqualTo(2);
    }

    @Test
    public void testRenderCourseDetailsForNonExistingCourse() throws Exception {
        given(coursesRepository.getCourses()).willReturn(Arrays.asList(new CourseSummary(0, "class1"), new CourseSummary(1, "class2")));
        this.mvc.perform(get("/courses?name=not_a_course"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRedirectToSuccessPageOnSuccessfulSave() throws Exception {
        String courseName = "courseName";

        this.mvc.perform(post("/courses?name=" + courseName)
                .param("attendances[bar]", "bar")
                .param("attendances[foo]", "foo")
                .param("date", "4/20")
                .param("courseName", courseName))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/thanks"));

        HashMap<String, String> courseAttendance = new HashMap<String, String>() {{
            put("bar", "bar");
            put("foo", "foo");
        }};

        verify(coursesRepository).updateCourseData(courseName, "4/20", courseAttendance);
    }
}
