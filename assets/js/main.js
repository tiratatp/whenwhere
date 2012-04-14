(function() {
    if(window.whenwhere) {
        return;
    }
    function Whenwhere() {        
        // private
        var geocoder;

        function isAlphanumeric(value) {
            if (value.match(/^[0-9a-zA-Z]+$/)) {
                return true;
            } else {
                return false;
            }
        }

        function showLoading() {
            backend.showLoading();
        }

        function hideLoading() {
            backend.hideLoading();
        }

        function dump(arr, level) {
            var dumped_text = "";
            if (!level) {
                level = 0
            }
            var level_padding = "";
            for (var j = 0; j < level + 1; j++) {
                level_padding += "    "
            }
            if (typeof arr == "object") {
                for (var item in arr) {
                    var value = arr[item];
                    if (typeof value == "object") {
                        dumped_text += level_padding + "'" + item + "' ...\n";
                        dumped_text += dump(value, level + 1)
                    } else {
                        dumped_text += level_padding + "'" + item + "' => \"" + value + '"\n'
                    }
                }
            } else {
                dumped_text = "===>" + arr + "<===(" + typeof arr + ")"
            }
            return dumped_text
        }

        function getGravatar(email) {
            var url = 'http://www.gravatar.com/avatar/' + hex_md5(email) + '.jpg?s=80';
            return url;
        }

        function reverseGeocoding(lat, lng, callback) {
            try {
                if(!geocoder) {
                    geocoder = new google.maps.Geocoder();
                }
                var latlng = new google.maps.LatLng(lat, lng);
                geocoder.geocode({'latLng': latlng}, function(results, status) {
                    if (status == google.maps.GeocoderStatus.OK) {
                        var result = results[1];
                        if (result) {
                            callback(result.formatted_address);
                        }
                    } else {
                        console.log("Geocoder failed due to: " + status);
                    }
                });
            } catch (e) {
                // in case google does not finihs loading
            }            
        }

        function getMyEvent(email) {
            function padding(str) {        
                if((""+str).length<2) {
                    str = "0"+str;
                }
                return str;
            }
            var now = new Date(),
                date_string = now.getFullYear()+"-"+padding(now.getMonth()+1)+"-"+padding(now.getDate());
            callDB('_design/event/_view/by_creator?key=["'+email+'", "'+date_string+'"]', function (data) {
                if (data.error) {
                    console.log(data.error);
                    backend.showToast("Error:"+data.error);
                    // handle invalid event id
                    return;
                }
                var events = data.rows,
                    event_length = events.length,
                    event_container = $("#event_container").empty();
                if (typeof event_length == "undefined") {
                    events = [events];
                    event_length = 1;
                }
                if(event_length == 0) {
                    backend.showToast("No event yet!");
                    event_container.append('<li><a data-ajax="false" href="where.htm"><h3>No event yet!</h3><p>Create one?</p></a></li>');
                } else {
                    for (var i = 0; i < event_length; i++) {
                        event_container.prepend(createEventListItem(events[i].value));
                    }   
                }        
                event_container.listview('refresh');
                hideLoading();
            });
        }

        function createPersonListItem(person) {
            var ret = $('<li><a class="person" data-email="'+person.email+'" href="#"><img alt="' + person.name + '" src="' + getGravatar(person.email) + '" /><h3>' + person.name + '<br/>' + person.email + '</h3><p>Within ' + person.radius + ' km. of <span class="position">' + person.position + '</span><br/>around ' +person.when+'</p></a></li>');
            reverseGeocoding(person.latitude, person.longitude, function(address) {
                $(".position", ret).html(address);
            });
            return ret;
        }

        function createEventListItem(event) {
            var position = event.position,
                ret = $('<li><a href="event_info.htm" data-event-id="'+event._id+'" class="event"><h3>' + event.action + '</h3><p>Within '+event.radius+' km. of <span class="position">' + event.position + '</span><br/>around ' +event.when+'</p></a></li>');
            reverseGeocoding(event.latitude, event.longitude, function(address) {
                $(".position", ret).html(address);
            });
            return ret;
        }

        function createPlaceListItem(place) {            
            var location = place.location,
                latitude = location.latitude,
                longitude = location.longitude,
                ret = $('<li><a href="place_info.htm" data-place-id="'+place.id+'" class="place"><img alt="' + place.name + '" src="https://graph.facebook.com/' + place.id + '/picture?type=square" /><h3>' + place.name + '</h3><p>'+place.category+'<br/><span class="position">'+latitude+','+ longitude+'</span></p></a></li>');
            reverseGeocoding(latitude, longitude, function(address) {
                $(".position", ret).html(address);
            });
            return ret;
        }

        function callDB(endpoint, onSuccess) {
            var url,
                isInsideWebView = backend.isInsideWebView();    
            if(isInsideWebView) {
                url = 'https://nuttyknot.cloudant.com/whenwhere/' + endpoint;
            } else {
                url = 'https://thedlyeationfiethaskedga:iuE0TjhIS682nccpdaORPJDv@nuttyknot.cloudant.com/whenwhere/' + endpoint;
            }   
            //console.log("callDB:"+url);
            $.ajax({
                url: url,
                success: onSuccess,
                // since http basic auth header doesn't work in jsonp
                beforeSend: function (req) {
                    if(isInsideWebView) {
                        req.setRequestHeader('Authorization', 'Basic aW5raW5naW11bGRzdGlmZmljYXRhaWxsOjdud2FqRGZwVUw3UU1mRklKeUFIZUJvTg==');
                    }            
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log("ERROR:" + jqXHR + textStatus + errorThrown);
                },
                dataType: (isInsideWebView?"json":"jsonp")
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
                    people_container = $("#people_container").empty(),
                    sum_lat = 0,
                    sum_lng = 0;                 
                if (typeof people_length == "undefined") {
                    people = [people];
                    people_length = 1;
                }
                for (var i = 0; i < people_length; i++) {
                    var person = people[i].value;
                    console.log(dump(person));

                    sum_lat += person.latitude;
                    sum_lng += person.longitude;
                    
                    people_container.prepend(createPersonListItem(person));
                }
                localStorage.mean_pos = sum_lat/people_length +","+sum_lng/people_length;
                console.log("mean_pos:"+localStorage.mean_pos);
                people_container.listview('refresh');
                hideLoading();
            });
        }

        function loadNearbyPlace(mean_pos) {
            var param = {
                    'type':'place',
                    //'q':'coffee',
                    'center': mean_pos,
                    'distance': '10000',
                }, 
                place_query = localStorage.place_query;
            if(place_query) {
                param.q = place_query;
            }
            if(backend.isInsideWebView()) {
                param = JSON.stringify(param);
            }
            console.log(dump(param));
            showLoading();
            backend.queryGraph("search", param);            
        }

        function loadNearbyPlaceCallback(data) {
            console.log(dump(data));
            var places = data.data,
                places_length = places.length,
                places_container = $("#places_container").empty();
            if(places_length == 0) {
                backend.showToast("No place found!");
                places_container.append('<li><h3>No place found!</h3></li>');
            } else {
                for(var i=0;i<places_length;i++) {
                    places_container.append(createPlaceListItem(places[i]));
                }    
            }            
            places_container.listview('refresh');
            hideLoading();
        }

        // public
        this.facebook_callback = function(data_str) {
            var data;
            console.log("facebook_callback:"+data_str);
            if(typeof(data_str) != "object") {
                data = JSON.parse(data_str); // if not object, parse it again
            } else {
                data = data_str;
            }    
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
            case "decide_where":
                loadNearbyPlaceCallback(data);
                break;
            }
        }

        $(function () {
            // clear old state
            delete localStorage.isJoin;
            //delete localStorage.isOwner;
            //delete localStorage.event_id;
            delete localStorage.mean_pos;
            delete localStorage.place_query;

            $(".event").live("click", function() {
                showLoading();
                localStorage.event_id = $(this).data("event-id");       
                localStorage.isOwner = true;    
            });

            $(".person").live("click", function() {
                backend.showContact($(this).data("email"));
                return false;
            });

            $("body").live("scroll", function() {
                if (this.offsetHeight + this.scrollTop >= this.scrollHeight) {
                    alert("down!");
                }
            });

            function onPageChange(page_id) {
                console.log("onPageChange():" + page_id);
                var current_page = $("#"+page_id);
                switch (page_id) {
                case "search":
                    delete localStorage.event_id;
                    delete localStorage.isOwner;
                    $("form", current_page).submit(function () {
                        var event_field = $("#event_number"),
                            event_id = event_field.val();
                        if (event_id == "" || !isAlphanumeric(event_id)) {
                            event_field.focus();
                            return false;
                        }
                        localStorage.event_id = event_id;                
                    });
                    $("#qr_code", current_page).click(function () {
                        backend.scanQR();
                    });
                    break;
                case "event_info":
                    var id = localStorage.event_id,
                        isOwner = localStorage.isOwner;      
                    if(!id) {                        
                        backend.showToast("No event_id!");
                        $.mobile.changePage("main.htm");
                        return;
                    }
                    $(".join button", current_page).click(function () {
                        localStorage.isJoin = true;
                        backend.createWhere(id);
                    });
                    $(".owner a", current_page)
                        .attr("href","decide_where.htm#"+id)
                        .click(function() {
                            localStorage.event_id = id;
                            localStorage.isOwner = isOwner;
                        });
                          
                    getEventInfo(id);
                    if(isOwner) {
                        $(".owner", current_page).show();
                        $(".join", current_page).hide();
                    }
                    break;
                case "when":
                    var slider_start = $(".slider_start", current_page),
                        slider_end = $(".slider_end", current_page),
                        now = Math.ceil(new Date().getTime()/1800000)*1800000,
                        start_time = new Date(now + 1800000),
                        end_time = new Date(now + 5400000),
                        diff = end_time - start_time,
                        free_time = $("#free_time", current_page),
                        name = $("#name", current_page).val(localStorage.name),
                        email = $("#email", current_page).val(localStorage.email),
                        min = 1*slider_start.val(),
                        max = 1*slider_end.val();

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
                        min = 1*slider_start.val();
                        start_time = new Date(now + min * 60000);
                        render();

                        if(min > max) {
                            slider_end.val(min).slider('refresh');  
                        }
                    });

                    slider_end.change(function () {
                        max = 1*slider_end.val();
                        end_time = new Date(now + max * 60000);
                        render();

                        if(min > max) {
                            slider_start.val(max).slider('refresh');  
                        }
                    });

                    $("#connect_facebook", current_page).click(function () {
                        showLoading();
                        backend.facebookSSO();
                    });

                    $("form", current_page).submit(function () {                                
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
                        if((1*slider_start.val())>=(1*slider_end.val())) {
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
                        $('button[type="submit"]', this).attr("disabled","disabled");
                        backend.submit(name_val, email_val, action_val, free_time.val(), event_id);
                        return false;
                    });
                    break;
                case "result":
                    var id = localStorage.event_id;
                    if(!id) {                        
                        backend.showToast("No event_id!");
                        $.mobile.changePage("main.htm");
                        return;
                    }
                    var url = "http://chart.apis.google.com/chart?chs=200x200&cht=qr&chl=" + id,
                        qr = $("<img/>").attr({
                        "alt": "qr code",
                        "id": "qr_code",
                        "src": url,
                    });
                    $("#img_container", current_page).append(qr);
                    $(".share", current_page).click(function () {
                        share("Let's " + localStorage.action + "! " + url);
                        return false;
                    });
                    hideLoading();
                    break;
                case "my_event":
                    delete localStorage.event_id;
                    delete localStorage.isOwner;

                    showLoading();
                    backend.facebookSSO();
                    break;
                case "decide_where":
                    var mean_pos = localStorage.mean_pos;
                    if(!mean_pos) {
                        $.mobile.changePage("main.htm");
                        return;
                    }
                    loadNearbyPlace(mean_pos);
                    break;
                case "search_where":
                    var place_query = $('#place_query', current_page);
                    place_query.val(localStorage.place_query);
                    $("form", current_page).submit(function() {
                        localStorage.place_query = place_query.val();
                        $('.ui-dialog').dialog('close');
                        return false;
                    });
                    break;

                }
            }

            $(document).bind("pagechange", function (event, data) {
                onPageChange(data.toPage.attr("id"));
            });

            // trigger onPageChange if the page was loaded directly
            onPageChange($.mobile.activePage.attr("id"));
        });    
    }

    Whenwhere.prototype = {
        "createWhere":function() {
            backend.createWhere();
        },
        "callback":function (event_id) {
            switch ($.mobile.activePage.attr("id")) {
            case "search":
                console.log("QR:" + event_id);
                $("#event_number").val(event_id);
                break;
            case "when":
                if (localStorage.isJoin) {
                    console.log("Join!" + localStorage.event_id);
                    delete localStorage.isJoin;
                } else {
                    localStorage.event_id = event_id;
                    console.log("Done!" + event_id);
                }
                $.mobile.changePage("result.htm");
                break;
            }
        },
        "facebook_login_callback": function () {
            backend.queryGraph("me"); // in "when"
        },
        "share":function(url) {
            backend.shareLink(url);
            return false;
        }
    }
    if(!window.whenwhere) {
        window.whenwhere = new Whenwhere();
    }    
})();
