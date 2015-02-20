var ws;

var CURRENT_PAGE = 1;
var NOTIFICATION_SHOW_CLASS = "fadeInRight";
var NOTIFICATION_HIDE_CLASS = "fadeOutRight";

$(document).ready(function() {
	if (typeof (Storage) !== "undefined") {
		var page = localStorage.getItem("page");
		if (page != undefined) {
			CURRENT_PAGE = page;
		}
	}

	if ($('.page-container[data=' + CURRENT_PAGE + ']').length == 0) {
		CURRENT_PAGE = 1;
	}

	ws = new WebSocket(WS_ADDRESS);
	try {
		ws.onmessage = onMessage;

		ws.onopen = function(e) {
			var wsMsg = new WebsocketMessage();
			wsMsg.message = CURRENT_PAGE;
			wsMsg.method = "start";

			ws.send(JSON.stringify(wsMsg));

			$(".spinner-black").addClass('animated fadeOut');
			if (BIG_SCREEN == 1) {
				$('.module').addClass('animated fadeIn');
			}
			//			
			setTimeout(function() {
				$(".spinner-black").remove();
			}, 1500);

			for (i = 0; i < modules.length; i++) {
				if (modules[i] != null && modules[i].onConnect != undefined) {
					modules[i].onConnect();
				}
			}
		}

		ws.onerror = function(error) {
			console.error('There was an un-identified Web Socket error');
		};

		ws.onclose = function() {
			$("#global-overlay p").html('Connection to server lost.<br /><a href="' + window.location + '" class="btn btn-warning" > Refresh page </a>');
			$("#global-overlay").show();
			$("#global-overlay").addClass('bounceDown');

		}
	} catch (e) {
		console.error('Sorry, the web socket at "%s" is un-available error', WS_ADDRESS);
		console.log(e);
	}

	$(".internal-link").click(function(event) {
		window.location = $(this).attr('href');
		return false
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
	case 'reload':
		location.reload();
		break;
	case 'remote404':
		$('#' + json.id + '-overlay').html('This remote module is not available at the moment.');
		$('#' + json.id + '-overlay').show();
		break;
	default:
		$('#' + json.id + '-overlay').hide();
		break;

	}

	modules[json.id].onMessage(json.method, json.message, json.extra);
}

function showSuccessMessage(message) {
	showNotification(message, 'success');
}

function showErrorMessage(message) {
	showNotification(message, 'error');	
}

function showNotification(message, type){
var box = $("#message-box");
	
	var id = Date.now();
	var html = '<div id="'+id+'" class="notification '+type+' animated '+NOTIFICATION_SHOW_CLASS+'">'+message+'</div>';
	box.append(html);
	setInterval(function() {
		
		$('#'+id).removeClass(NOTIFICATION_SHOW_CLASS);
		$('#'+id).addClass(NOTIFICATION_HIDE_CLASS);
		setInterval(function() {
			$('#'+id).remove();
		}, 1000);
	}, 4000);
}

function sendMessage(moduleId, method, message) {
	var wsMsg = new WebsocketMessage();
	wsMsg.message = message;
	wsMsg.id = moduleId;
	wsMsg.method = method;
	var json = JSON.stringify(wsMsg);
	ws.send(json);
	console.log(json);
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
	var units = si ? [ 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB' ] : [ 'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB' ];
	var u = -1;
	do {
		bytes /= thresh;
		++u;
	} while (bytes >= thresh);
	return bytes.toFixed(1) + ' ' + units[u];
}
