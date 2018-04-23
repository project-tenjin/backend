package org.redischool.attendance.details;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
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

    @Mock
    private CourseHelper courseHelper;

    @Before
    public void setUp() throws Exception {
        CourseDetailsController controller = new CourseDetailsController(courseDetailsRepository, courseHelper);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testRenderCourseDetails() throws Exception {
        String closestCourseDate = "2017-04-27T00:00Z";
        CourseDetails returnedCourseDetails = new CourseDetails(
                "class2",
                asList("Student-name", "Student-other-name"),
                asList("4/24", "4/27"),
                // 2017, 4, 24 | 2017, 4, 27
                asList(new Date(1492992000000L), new Date(1493251200000L)));
        List datesMap = Collections.singletonList(new HashMap() {{
            put("formatted", returnedCourseDetails.getFormattedDates());
            put("java", returnedCourseDetails.getJavaDates());
        }});

        given(courseDetailsRepository.getCourseDetails("class2")).willReturn(returnedCourseDetails);
        given(courseHelper.closestCourseDate(any(), eq(returnedCourseDetails))).willReturn(closestCourseDate);
        given(courseHelper.getFormattedDatesMap(returnedCourseDetails)).willReturn(datesMap);

        mvc.perform(get("/courses/?name=" + "class2"))
                .andExpect(status().isOk())
                .andExpect(view().name("courseDetail"))
                .andExpect(model().attribute("courseDetails", returnedCourseDetails))
                .andExpect(model().attribute("datesMap", datesMap))
                .andExpect(model().attribute("closestCourseDate", closestCourseDate));
    }

    @Test
    public void testErrorWhenCourseNotFound() throws Exception {
        given(courseDetailsRepository.getCourseDetails("doesnotexist")).willThrow(
                new RuntimeException(
                        new GoogleJsonResponseException(
                                new HttpResponseException.Builder(400, "Bad Request", new HttpHeaders()),
                                new GoogleJsonError()
                        )
                )
        );

        mvc.perform(get("/courses/?name=" + "doesnotexist"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAttendanceForCourseAndDate() throws Exception {
        String courseName = "courseName";
        String date = "4/20";

        HashMap<String, String> courseAttendance = new HashMap<String, String>() {{
            put("student1", "P");
            put("student2", "L");
        }};

        given(courseDetailsRepository.getAttendance(courseName, date)).willReturn(courseAttendance);

        mvc.perform(get("/attendance")   //?courseName=" + courseName + "&date=" + date)
                .param("date", date)
                .param("courseName", courseName))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.courseName").value(courseName))
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.attendances.student1").value("P"))
                .andExpect(jsonPath("$.attendances.student2").value("L"));
    }

    @Test
    public void testRedirectToSuccessPageOnSuccessfulSave() throws Exception {
        String courseName = "courseName";

        mvc.perform(post("/attendance")
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

        verify(courseDetailsRepository).updateAttendance(courseName, "4/20", courseAttendance);
    }

    @Test
    public void testRedirectToErrorWhenNoDate() throws Exception {
        doThrow(new IllegalArgumentException(""))
                .when(courseDetailsRepository).updateAttendance(anyString(), anyString(), anyMap());

        String courseName = "courseName";

        mvc.perform(post("/attendance?name=" + courseName)
                .param("attendances[student1]", "P")
                .param("attendances[student2]", "L")
                .param("date", "WRONG DATE")
                .param("courseName", courseName))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses?name=" + courseName + "&error=Date required"));
    }
}
