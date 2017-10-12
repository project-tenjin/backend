function refreshAttendance() {
    var dateSelected = $("#date option:selected").text();

    $.get('/attendance', { courseName: courseName, date: dateSelected }, function(data) {
        for (var student in data.attendances) {
            if (data.attendances.hasOwnProperty(student)) {
                var attendanceValue = data.attendances[student];

                //TODO: Quick fix for this edge case
                //In the future, 'E' should have a note associated in the frontend
                if(attendanceValue == "E") {
                    attendanceValue = "U";
                }

                if(attendanceValue == "") {
                    $("input[name='attendances["+student+"]']").prop("checked",false);
                } else {
                    $("input[name='attendances["+student+"]'][value='"+attendanceValue+"']").prop("checked",true);
                }

            }
        }
    });
}
