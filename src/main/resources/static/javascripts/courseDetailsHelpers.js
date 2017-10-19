function refreshAttendance() {
    var dateSelected = $("#date option:selected").text()
    showOverlay()

    $.get('/attendance', { courseName: courseName, date: dateSelected }, function() {})
        .done(updateAttendances)
        .always(hideOverlay)
}

function updateAttendances(data) {
    for (var student in data.attendances) {
        if (data.attendances.hasOwnProperty(student)) {
            var attendanceValue = data.attendances[student]

            // TODO: Quick fix for this edge case
            // In the future, 'E' should have a note associated in the frontend
            if(attendanceValue == "E") {
                attendanceValue = "U"
            }

            if(attendanceValue == "") {
                $("input[name='attendances[" + student + "]']").prop("checked", false)
            } else {
                $("input[name='attendances[" + student + "]'][value='" + attendanceValue + "']")
                    .prop("checked", true)
            }
        }
    }
}

function showOverlay() {
    var table = $(".course_attendance table")
    var tablePosition = table.position()
    var top = tablePosition.top + parseInt(table.css('marginTop'))
    var left = tablePosition.left
    var width = table.width()
    var height = table.height()

    $("#overlay").css({
        position: 'absolute',
        top: top,
        left: left,
        width: width,
        height: height
    })
    $("#overlay").fadeIn()
}

function hideOverlay() {
    $("#overlay").fadeOut()
}

function selectInitialAttendance(selectDate) {
    var option = $('.course_attendance option[data-date="' + selectDate + '"]')
    option.prop('selected', true)
    refreshAttendance()
}
