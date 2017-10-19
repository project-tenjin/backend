function setUpFixtures() {
  setFixtures('<form name="course_attendance" action="" class="course_attendance">'
            + ' <input type="hidden" name="courseName" value="Intermediate Java" />'
            + ' <div class="error"></div>'
            + ' <select id="date" name="date" onchange="javascript:refreshAttendance()">'
            + '  <option selected="selected" disabled="disabled">Please select a date</option>'
            + '  <option data-date="2017-09-17T22:00Z">18.Sep, (Mon)</option>'
            + '  <option data-date="2017-09-21T22:00Z">22.Sep, (Fri)</option>'
            + '  <option data-date="2017-09-24T22:00Z">25.Sep, (Mon)</option>'
            + ' </select>'
            + ' <table>'
            + '  <tr>'
            + '   <td>User 1</td>'
            + '   <td><input type="radio" value="P" name="attendances[User 1]" /></td>'
            + '   <td><input type="radio" value="L" name="attendances[User 1]" /></td>'
            + '   <td><input type="radio" value="U" name="attendances[User 1]" /></td>'
            + '  </tr>'
            + '  <tr>'
            + '   <td>User 2</td>'
            + '   <td><input type="radio" value="P" name="attendances[User 2]" /></td>'
            + '   <td><input type="radio" value="L" name="attendances[User 2]" /></td>'
            + '   <td><input type="radio" value="U" name="attendances[User 2]" /></td>'
            + '  </tr>'
            + '  <tr>'
            + '   <td>User 3</td>'
            + '   <td><input type="radio" value="P" name="attendances[User 3]" /></td>'
            + '   <td><input type="radio" value="L" name="attendances[User 3]" /></td>'
            + '   <td><input type="radio" value="U" name="attendances[User 3]" /></td>'
            + '  </tr>'
            + '  <tr>'
            + '   <td>User 4</td>'
            + '   <td><input type="radio" value="P" name="attendances[User 4]" /></td>'
            + '   <td><input type="radio" value="L" name="attendances[User 4]" /></td>'
            + '   <td><input type="radio" value="U" name="attendances[User 4]" /></td>'
            + '  </tr>'
            + ' </table>'
            + ' <div id="overlay"></div>'
            + '</form>')
}

var ajaxResponse

beforeEach(function() {
    ajaxResponse = $.Deferred()
    courseName = ''
    setUpFixtures()

    spyOn($, 'get').and.callFake(function(e) {
        return ajaxResponse.promise()
    })
})

describe("refreshAttendance", function() {
    it("shows the overlay while loading", function() {
        refreshAttendance()
        expect($('#overlay')).toBeVisible()
    })

    describe("when successful", function() {
        beforeEach(function() {
            ajaxResponse.resolve({'text':'this a a fake response'})
        })

        it("updates attendances", function() {
            spyOn(window, 'updateAttendances')

            refreshAttendance()

            expect(window.updateAttendances).toHaveBeenCalled()
        })

        it("hides loading indicator", function() {
            spyOn($.fn, 'fadeOut')
            refreshAttendance()
            expect($.fn.fadeOut).toHaveBeenCalled()
        })
    })

    describe("when failing", function() {
        beforeEach(function() {
            ajaxResponse.reject({'text':'this a a fake response'})
        })

        it("hides loading indicator", function() {
            spyOn($.fn, 'fadeOut')
            refreshAttendance()
            expect($.fn.fadeOut).toHaveBeenCalled()
        })
    })
})

describe("updateAttendances", function() {
    attendances = JSON.parse('{"attendances":{"User 1":"P","User 2":"L","User 3":"U","User 4":"E","User 5":""},"date":"22.Sep, (Fri)","courseName":"Intermediate Java"}')

    it("checks the right attendance values", function() {
        updateAttendances(attendances)

        expect($("input[name='attendances[User 1]'][value='P']")).toBeChecked()
        expect($("input[name='attendances[User 1]'][value='L']")).not.toBeChecked()
        expect($("input[name='attendances[User 1]'][value='U']")).not.toBeChecked()

        expect($("input[name='attendances[User 2]'][value='P']")).not.toBeChecked()
        expect($("input[name='attendances[User 2]'][value='L']")).toBeChecked()
        expect($("input[name='attendances[User 2]'][value='U']")).not.toBeChecked()

        expect($("input[name='attendances[User 3]'][value='P']")).not.toBeChecked()
        expect($("input[name='attendances[User 3]'][value='L']")).not.toBeChecked()
        expect($("input[name='attendances[User 3]'][value='U']")).toBeChecked()

        expect($("input[name='attendances[User 4]'][value='P']")).not.toBeChecked()
        expect($("input[name='attendances[User 4]'][value='L']")).not.toBeChecked()
        expect($("input[name='attendances[User 4]'][value='U']")).toBeChecked()

        expect($("input[name='attendances[User 5]'][value='P']")).not.toBeChecked()
        expect($("input[name='attendances[User 5]'][value='L']")).not.toBeChecked()
        expect($("input[name='attendances[User 5]'][value='U']")).not.toBeChecked()
    })
})

describe("selectInitialAttendance", function() {
    beforeEach(function() {
        spyOn(window, 'refreshAttendance')
        selectInitialAttendance('2017-09-21T22:00Z')
    })

    it("selects a date", function() {
        expect($('.course_attendance option:nth-child(3)')).toBeSelected()
    })

    it("refreshes attendances", function() {
        expect(window.refreshAttendance).toHaveBeenCalled()
    })
})
