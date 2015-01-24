function transmission(moduleId) {
	this.moduleId = moduleId;
	this.torrents = []
	this.selectedTorrent = null;
	
	this.documentReady = function() {
		var parent = this;
		$("#addTorrent" + this.moduleId).click(function(e) {
			parent.addTorrent();
		});

		$("#altSpeed" + this.moduleId).click(function(e) {
			parent.setAltSpeed();
		});
		
		$(document).on('click', '.torrent', function(e){
			$(".modal").appendTo('body').modal('show');
			parent.selectedTorrent = $(this).attr('data');
		});
		
		$("#removeTorrent").click( function(e){
			parent.removeTorrent(parent.selectedTorrent);
			$(".modal").modal('hide');
		});
		
		$("#pauseTorrent").click( function(e){
			parent.pauseTorrent(parent.selectedTorrent);
			$(".modal").modal('hide');
		});

	}

	this.addTorrent = function() {
		var url = prompt("Margnet link");
		sendMessage(this.moduleId, 'addTorrent', url);
	}

	this.onMessage = function(method, message) {
		if (method == 'refresh') {
			this.processData(message);
		}
	}

	this.setAltSpeed = function() {
		var button = $("#altSpeed" + this.moduleId);
		var setAltSpeed = true;
		if (button.hasClass("btn-primary")) {
			setAltSpeed = false;
			button.removeClass("btn-primary");
		} else {
			setAltSpeed = true;
			button.addClass("btn-primary");
		}

		sendMessage(this.moduleId, 'altSpeed', setAltSpeed);
	}

	this.processData = function(json) {
		if (!json) {
			$("#transmission-" + this.moduleId + "-overlay").show();
		} else {

			$("#torrentcount" + this.moduleId).html(
					json.status.obj.map.torrentCount);
			$("#dlSpeed" + this.moduleId).html(
					humanFileSize(json.status.obj.map.downloadSpeed, true));
			$("#ulSpeed" + this.moduleId).html(
					humanFileSize(json.status.obj.map.uploadSpeed, true));

			if (json.alternateSpeeds) {
				$("#altSpeed" + this.moduleId).addClass("btn-primary");
			} else {
				$("#altSpeed" + this.moduleId).removeClass("btn-primary");
			}

			$("#torrents" + this.moduleId).html('<hr />');

			var parent = this;
			$.each(json.torrents, function(index, value) {
				parent.torrents[value.id] = value;
				$("#torrents" + parent.moduleId).append(
						parent.torrentToHtml(value, json.rpcVersion));
			});

			$("#transmission-" + this.moduleId + "-overlay").hide();
		}

	}

	this.torrentToHtml = function(torrent, rpcVersion) {
		var html = [];

		var percent = Math.ceil(torrent.percentDone * 100);
		console.log(torrent.name + ":" + percent);

		html.push('<div  id="torrent-', torrent.id, '">');
		html.push('<p style="overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">');
		html.push('<button  data="',torrent.id,'" class="torrent btn btn-primary btn-xs" style="float: right"><span class="glyphicon glyphicon-pencil"></span></button>');
		html.push(this.getStatusIcon(torrent.status, rpcVersion), ' ');
		html.push('<strong>', torrent.name, '</strong>');
		html.push('</p>');
		html.push('<div class="progress small-progress-bar">');
		html.push('<div class="progress-bar" role="progressbar" aria-valuenow="',
				percent,
				'" aria-valuemin="0" aria-valuemax="100" style="width: ',
				percent, '%;">');
		//html.push('<span class="sr-only">', percent, '% Complete</span>');
		html.push('</div>');
		html.push('</div>');
		html.push('<p>');
		html.push('DL: ', humanFileSize(torrent.downloadSpeed, true),
				'/s | Ul: ', humanFileSize(torrent.uploadSpeed, true), '/s');
		html.push('<span style="float:right">');
		html.push(humanFileSize(torrent.downloaded, true), '/', humanFileSize(
				torrent.totalSize, true));
		html.push('</span>');
		html.push('</p>');
		html.push('<hr />');

		return html.join("");
	}

	this.getStatusIcon = function(value, rpcVersion) {
		if (rpcVersion < 14) {
			switch (value) {
			case 16:
				return '<span class="glyphicon glyphicon-ok"></span>';
			case 8:
				return '<span class="glyphicon glyphicon-coud-upload"></span>';
			case 4:
				return '<span class="glyphicon glyphicon-cloud-download"></span>';
			case 2:
				return '<span class="glyphicon glyphicon-refresh"></span>';
			case 4:
				return '<span class="glyphicon glyphicon-time"></span>';
			default:
				return '';
			}
		} else {
			switch (value) {
			case 6:
				return '<span class="glyphicon glyphicon-cloud-upload"></span>';
			case 5:
				return '<span class="glyphicon glyphicon-time"></span>';
			case 4:
				return '<span class="glyphicon glyphicon-cloud-download"></span>';
			case 3:
				return '<span class="glyphicon glyphicon-time"></span>';
			case 2:
				return '<span class="glyphicon glyphicon-refresh"></span>';
			case 1:
				return '<span class="glyphicon glyphicon-time"></span>';
			case 0:
				return '<span class="glyphicon glyphicon-pause"></span>';
			default:
				return '';
			}
		}

	}
	
	this.removeTorrent = function(id){
		sendMessage(this.moduleId, 'removeTorrent', id);
	}
	
	this.pauseTorrent = function(id){
		sendMessage(this.moduleId, 'pauseTorrent', id);
	}

}