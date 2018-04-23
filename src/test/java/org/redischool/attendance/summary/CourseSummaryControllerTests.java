package org.redischool.attendance.summary;

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
public class CourseSummaryControllerTests {

    private MockMvc mvc;

    @Mock
    CourseSummaryRepository courseSummaryRepository;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(new CourseSummaryController(courseSummaryRepository)).build();
    }

    @Test
    public void testRendersCourses() throws Exception {
        given(courseSummaryRepository.getCourses())
                .willReturn(Arrays.asList(new CourseSummary(0, "class1"), new CourseSummary(0, "class2")));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("courseList"))
                .andExpect(model().attribute("courses", Arrays.asList(new CourseSummary(0, "class1"), new CourseSummary(0, "class2"))));
    }
}
