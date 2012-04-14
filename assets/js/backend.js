(function() {
	function Backend() {
		var that = this;
		function facebookInit(callback) {
			// prepare doument for facebook sdk
			$('<div></div>').attr("id", "fb-root").appendTo($("body"));
			window.fbAsyncInit = function() {
			    FB.init({
			      appId      : '352831231424321', // App ID
			      channelUrl : '//dl.dropbox.com/u/1629873/whenwhere/channel.html', // Channel File
			      status     : true, // check login status
			      cookie     : true, // enable cookies to allow the server to access the session
			      xfbml      : true  // parse XFBML
			    });
			    if(callback) {
					callback();
				}
		  	};

			// Load the SDK Asynchronously
			(function(d){
				var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
				if (d.getElementById(id)) {return;}
				js = d.createElement('script'); js.id = id; js.async = true;
				js.src = "//connect.facebook.net/en_US/all.js";
				ref.parentNode.insertBefore(js, ref);
			}(document));		
		}
		function facebookSSO() {
			if (!window.FB) {
				facebookInit(facebookSSO);
			} else {
				FB.login(function(response) {
					if (response.authResponse) {										
						whenwhere.facebook_login_callback();
					} else {
						console.log('User cancelled login or did not fully authorize.');
					}
				});
			}
		}
		this.facebookSSO = facebookSSO;

		$(function() {
			function onPageChange(page_id) {
                var current_page = $("#"+page_id);
                switch (page_id) {
                case "main":
                	$("ul", current_page)
                		.empty()
                		.append('<li><a href="my_event.htm">My Event</a></li>')
                		.listview('refresh');
                    break;      
                }
            }

            $(document).bind("pagechange", function (event, data) {
                onPageChange(data.toPage.attr("id"));
            });

            // trigger onPageChange if the page was loaded directly
            onPageChange($.mobile.activePage.attr("id"));
        });
	};

	Backend.prototype = {
		"queryGraph":function(endpoint, param){
			console.log("Getting \"" + endpoint + "\"");		
			if(!param) {
				param = {};
			} else if(typeof(param) == "string") {				
				param = JSON.parse(param);
			}
			FB.api('/'+endpoint, param, function(response) {		
				whenwhere.facebook_callback(response); // send as json
			});			
		},		
		"showToast":function(msg){
			//alert(msg);		
			console.log("showToast:"+msg);
		},
		"isInsideWebView":function() {
			return false;
		},
		"showContact": function(email) {
			window.location = "mailto:"+email;
		},
		"showLoading":function() {
			$.mobile.showPageLoadingMsg();
		},
		"hideLoading":function() {
			$.mobile.hidePageLoadingMsg();
		},
	}

	if(!window.backend) {
		window.backend = new Backend();
	}
})();
