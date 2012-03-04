function createWhere() {
    backend.createWhere();
}

function callback(event_id) {
    switch ($.mobile.activePage.attr("id")) {
    case "search":
        console.log("QR:" + event_id);
        $("#event_number").val(event_id);
        break;
    case "when":
        hideLoading();
        if (localStorage.isJoin) {
            console.log("Join!" + localStorage.event_id);
            delete localStorage.isJoin;
        } else {
            localStorage.event_id = event_id;
            console.log("Done!" + event_id);
        }
        backend.loadPage("result.htm");
        break;
    }
}

function facebook_login_callback() {
    backend.queryGraph("me"); // in "when"
}

function facebook_callback(data_str) {
    var data = JSON.parse(data_str);
    switch ($.mobile.activePage.attr("id")) {
    case "when":
        // call "me" from "when"
        $("#when #name").val(data.name);
        $("#when #email").val(data.email);
        hideLoading();
        break;
    case "my_event":
        getMyEvent(data.email);
        break;
    }
}

function createPersonListItem(person) {
	console.log(dump(person));
    var ret = '<li><a class="person" data-email="'+person.email+'" href="#"><img alt="' + person.name + '" src="' + getGravatar(person.email) + '" /><h3>' + person.name + '<br/>' + person.email + '</h3><p>Within ' + person.radius + ' km. of ' + person.position + '<br/>around ' +person.when+'</p></a></li>';
    return ret;
}

function createEventListItem(event) {
    var ret = '<li><a href="event_info.htm" data-event-id="'+event._id+'" class="event"><h3>' + event.action + '</h3><p>Within '+event.radius+' km. of ' + event.position + '<br/>around ' +event.when+'</p></a></li>';
    return ret;
}

function share(url) {
    backend.shareLink(url);
    return false;
}

function callDB(endpoint, onSuccess) {
	var url = 'https://nuttyknot.cloudant.com/whenwhere/' + endpoint;
	console.log("callDB:"+url);
    $.ajax({
        url: url,
        success: onSuccess,
        beforeSend: function (req) {
            req.setRequestHeader('Authorization', 'Basic aW5raW5naW11bGRzdGlmZmljYXRhaWxsOjdud2FqRGZwVUw3UU1mRklKeUFIZUJvTg==');
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log("ERROR:" + jqXHR + textStatus + errorThrown);
        },
        dataType: "json"
    });
}

function getMyEvent(email) {	
    callDB('_design/event/_view/by_creator?key="' + email + '"', function (data) {
        if (data.error) {
            console.log(data.error);
            backend.showToast("Error:"+data.error);
            // handle invalid event id
            return;
        }
        var events = data.rows,
            event_length = events.length,
            event_container = $("#event_container");
        if (typeof event_length == "undefined") {
            events = [events];
            event_length = 1;
        }
        if(event_length == 0) {
        	backend.showToast("No event yet!");
        	event_container.append('<li><a class="create_event" href="#"><h3>No event yet!</h3><p>Create one?</p></a></li>');
        } else {
        	for (var i = 0; i < event_length; i++) {
	            event_container.prepend(createEventListItem(events[i].value));
	        }	
        }        
        event_container.listview('refresh');
        hideLoading();
    });
}

function getEventInfo(event_id) {
    if (!isAlphanumeric(event_id)) {
        return;
    }
    callDB('_design/rsvp/_view/by_event_id?key="' + event_id + '"', function (data) {
        if (data.error) {
            console.log(data.error);
            backend.showToast("Error:"+data.error);
            // handle invalid event id
            return;
        }
        var people = data.rows,
            people_length = people.length,
            people_container = $("#people_container");
        if (typeof people_length == "undefined") {
            people = [people];
            people_length = 1;
        }
        for (var i = 0; i < people_length; i++) {
            people_container.prepend(createPersonListItem(people[i].value));
        }
        people_container.listview('refresh');
        hideLoading();
    });
}

$(function () {
	// clear old state
	delete localStorage.isJoin;
	delete localStorage.isOwner;
	delete localStorage.event_id;

	$(".event").live("click", function() {
		localStorage.event_id = $(this).data("event-id");		
		localStorage.isOwner = true;	
	});

	$(".create_event").live("click", function() {
		backend.createWhere();
		return false;
	});

	$(".person").live("click", function() {
		backend.showContact($(this).data("email"));
		return false;
	});

    function onPageChange(page_id) {
        console.log("onPageChange():" + page_id);
        switch (page_id) {
        case "search":
            $("#search form").submit(function () {
                var event_field = $("#event_number"),
                    event_id = event_field.val();
                if (event_id == "" || !isAlphanumeric(event_id)) {
                    event_field.focus();
                    return false;
                }
                localStorage.event_id = event_id;                
            });
            $("#qr_code").click(function () {
                backend.scanQR();
            });
            break;
        case "event_info":
            $("#join").click(function () {
                localStorage.isJoin = true;
                backend.createWhere(id);
            });

            var id = localStorage.event_id;
            getEventInfo(id);
            break;
        case "when":
            var slider_start = $(".slider_start").val(90).slider('refresh'),
                slider_end = $(".slider_end").val(210).slider('refresh'),
                now = new Date().getTime(),
                start_time = new Date(now + 1800000),
                end_time = new Date(now + 5400000),
                diff = end_time - start_time,
                free_time = $("#free_time"),
                name = $("#name").val(localStorage.name),
                email = $("#email").val(localStorage.email);

            function getTime(date_obj) {
                var min = date_obj.getMinutes();
                if (min / 10 < 1) {
                    min = "0" + min;
                }
                return date_obj.getHours() + ":" + min;
            }

            function render() {
                var hour = diff / 3600000;
                free_time.val(getTime(start_time) + " - " + getTime(end_time) + " (About " + hour + " " + (hour > 1 ? "hours" : "hour") + ")");
                diff = end_time - start_time;
            }

            render();

            slider_start.change(function () {
                start_time = new Date(now + slider_start.val() * 60000);
                render();
            });

            slider_end.change(function () {
                end_time = new Date(now + slider_end.val() * 60000);
                render();
            });

            $("#connect_facebook").click(function () {
                showLoading();
                backend.facebookSSO();
            });

            $("#when form").submit(function () {            	            	
                var name_val = name.val(),
                    event_id = localStorage.event_id,
                    email_val = email.val(),
                    action = $("#action"),
                    action_val = action.val();
                if (name_val == "") {
                	backend.showToast("Please enter name");
                    name.focus();                    
                    return false;
                } 
                if (email_val == "") {
                	backend.showToast("Please enter email");
                    email.focus();                    
                    return false;
                } 
                if (action_val == "") {
                	backend.showToast("Please enter action");
                    action.focus();                    
                    return false;
                } 
                if(slider_start.val()>=slider_end.val()) {
            		backend.showToast("End time must be later than Start time");
            		return false;
            	}
                showLoading();
                if (!localStorage.isJoin) {
                    event_id = "";
                }
                localStorage.name = name_val;
                localStorage.email = email_val
                localStorage.action = action_val;
                backend.submit(name_val, email_val, action_val, free_time.val(), event_id);
                return false;
            });
            break;
        case "result":
            var url = "http://chart.apis.google.com/chart?chs=200x200&cht=qr&chl=" + localStorage.event_id,
            	qr = $("<img/>").attr({
                "alt": "qr code",
                "id": "qr_code",
                "src": url,
            });
            $("#img_container").append(qr);
            $(".share").click(function () {
                share("Let's " + localStorage.action + "! " + url);
                return false;
            });
            hideLoading();
            break;
        case "my_event":
            showLoading();
            backend.facebookSSO();
            break;
        }
    }

    $(document).bind("pagechange", function (event, data) {
        onPageChange(data.toPage.attr("id"));
    });

    // trigger onPageChange if the page was loaded directly
    onPageChange($.mobile.activePage.attr("id"));
});