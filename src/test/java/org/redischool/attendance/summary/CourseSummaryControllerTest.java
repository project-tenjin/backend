package org.redischool.attendance.summary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redischool.attendance.details.CourseAccessValidator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CourseSummaryControllerTest {

    private MockMvc mvc;

    @Mock
    CourseSummaryRepository courseSummaryRepository;

    @Mock
    CourseAccessValidator courseAccessValidator;

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new CourseSummaryController(courseSummaryRepository, courseAccessValidator)).build();
    }

    @Test
    public void testRendersOnlyAssignedCourses() throws Exception {
        given(courseAccessValidator.hasPermissions(any(), eq("class1"))).willReturn(true);
        given(courseAccessValidator.hasPermissions(any(), eq("class2"))).willReturn(true);

        given(courseAccessValidator.hasPermissions(any(), eq("unassigned"))).willReturn(false);

        given(courseSummaryRepository.getCourses())
                .willReturn(Arrays.asList(
                        new CourseSummary(0, "class1"),
                        new CourseSummary(1, "unassigned"),
                        new CourseSummary(2, "class2")));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("courseList"))
                .andExpect(model().attribute("courses", Arrays.asList(
                        new CourseSummary(0, "class1"),
                        new CourseSummary(2, "class2"))));
    }
}
