package org.redischool.attendance.details;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;

import static java.util.Arrays.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class CourseDetailsControllerTests {

    private MockMvc mvc;

    @Mock
    private CourseDetailsRepository courseDetailsRepository;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(new CourseDetailsController(courseDetailsRepository)).build();
    }

    @Test
    public void testRenderCourseDetails() throws Exception {
        CourseDetails returnedCourseDetails = new CourseDetails(
                "class2",
                asList("Student-name", "Student-other-name"),
                asList("4/24", "4/27"));

        given(courseDetailsRepository.getCourseDetails("class2")).willReturn(returnedCourseDetails);

        mvc.perform(get("/courses/?name=" + "class2"))
                .andExpect(status().isOk())
                .andExpect(view().name("courseDetail"))
                .andExpect(model().attribute("courseDetails", returnedCourseDetails));
    }

    @Test
    public void testRedirectToSuccessPageOnSuccessfulSave() throws Exception {
        String courseName = "courseName";

        mvc.perform(post("/courses?name=" + courseName)
                .param("attendances[student1]", "P")
                .param("attendances[student2]", "L")
                .param("date", "4/20")
                .param("courseName", courseName))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/thanks"));

        HashMap<String, String> courseAttendance = new HashMap<String, String>() {{
            put("student1", "P");
            put("student2", "L");
        }};

        verify(courseDetailsRepository).updateCourseData(courseName, "4/20", courseAttendance);
    }

    @Test
    public void testRedirectToErrorWhenNoDate() throws Exception {
        doThrow(new IllegalArgumentException(""))
                .when(courseDetailsRepository).updateCourseData(anyString(), anyString(), anyMap());

        String courseName = "courseName";

        mvc.perform(post("/courses?name=" + courseName)
                .param("attendances[student1]", "P")
                .param("attendances[student2]", "L")
                .param("date", "WRONG DATE")
                .param("courseName", courseName))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses?name=" + courseName + "&error=Date required"));
    }
}
