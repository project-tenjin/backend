package org.redischool.attendance.details;

import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CourseHelper {
    public String closestCourseDate(Date targetDate, CourseDetails courseDetails) {
        if (courseDetails.getJavaDates().isEmpty()) {
            return null;
        }

        Date closestDate = null;
        Date firstCourseDate = courseDetails.getJavaDates().get(0);
        if (targetDate.before(firstCourseDate)) {
            closestDate = firstCourseDate;
        }

        for (Date courseDate : courseDetails.getJavaDates()) {
            if (courseDate.before(targetDate) || courseDate.equals(targetDate)) {
                closestDate = courseDate;
            }
        }
        return supportingDateFormat(closestDate);
    }

    public List<Object> getFormattedDatesMap(CourseDetails courseDetails) {
        List<Object> datesMap = new ArrayList<>();
        for (int i = 0; i < courseDetails.getJavaDates().size(); i++) {
            Map dateMap = new HashMap<String, List<String>>();

            // We want to keep allowing adjusting the user displayed date format by changing
            // the format in the backing Google SpreadSheet. However that means we can not
            // reliably do any date comparisons (e.g. for <select> field pre-selections) based
            // on those user formatted dates. Hence we need to maintain both here, the user
            // formatted date to display and a 'supporting' date representation to be used
            // wherever we need them programmatically.
            dateMap.put("display", courseDetails.getFormattedDates().get(i));
            dateMap.put("supporting", supportingDateFormat(courseDetails.getJavaDates().get(i)));
            datesMap.add(dateMap);
        }
        return datesMap;
    }

    private String supportingDateFormat(Date date) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(date.toInstant());
    }
}
