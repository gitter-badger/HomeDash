var ws;

$(document).ready(function() {
	ws = new WebSocket(WS_ADDRESS);
	try {
		ws.onmessage = onMessage;

		ws.onopen = function(e) {
			var wsMsg = new WebsocketMessage();
			wsMsg.message = "start";
			ws.send(JSON.stringify(wsMsg));
			
			if(showLoadingOverlay){
				$("#global-overlay").addClass('bounceUp');
				$(".module").addClass('fadeInLeft');
			}
		}

		ws.onerror = function(error) {
			console.error('There was an un-identified Web Socket error');
		};

		ws.onclose = function() {
			$("#global-overlay p").html('Connection to server lost.<br /><a href="'+window.location+'" class="btn btn-warning" > Refresh page </a>');
			$(".spinner").hide();
			$("#global-overlay").show();
			$("#global-overlay").removeClass('bounceUp');
			$("#global-overlay").addClass('bounceDown');
			

		}
	} catch (e) {
		console.error('Sorry, the web socket at "%s" is un-available error', WS_ADDRESS);
		console.log(e);
	}

	$("#showSettings").click(function() {
		$(".module").removeClass('fadeInLeft');
		$(".settings").slideToggle("fast");
	});
	
	$(".internal-link").click(function(event){
		location.href = $(this).attr('href');
		return false;
	});
});

function onMessage(event) {
	var json = JSON.parse(event.data);

	console.log(event.data);

	switch (json.method) {
	case 'success':
		showSuccessMessage(json.message);
		break;
	case 'error':
		showErrorMessage(json.message);
		break;
	}
	
	modules[json.id].onMessage(json.method, json.message);
}

function showSuccessMessage(message) {
	var box = $("#message-box");
	box.html(message);
	box.removeClass("error");
	box.addClass('success');
	box.slideDown();

	setInterval(function() {
		box.slideUp()
	}, 3000);
}

function showErrorMessage(message) {
	var box = $("#message-box");
	box.html(message);
	box.removeClass("success");
	box.addClass('error');
	box.slideDown();

	setInterval(function() {
		box.slideUp()
	}, 3000);
}

function sendMessage(moduleId, method, message) {
	var wsMsg = new WebsocketMessage();
	wsMsg.message = message;
	wsMsg.id = moduleId;
	wsMsg.method = method;
	ws.send(JSON.stringify(wsMsg));
}

function WebsocketMessage() {
	this.id = -1;
	this.method = "";
	this.message = "";
}

function humanFileSize(bytes, si) {
	var thresh = si ? 1000 : 1024;
	if (bytes < thresh)
		return bytes + ' B';
	var units = si ? [ 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB' ] : [
			'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB' ];
	var u = -1;
	do {
		bytes /= thresh;
		++u;
	} while (bytes >= thresh);
	return bytes.toFixed(1) + ' ' + units[u];
}