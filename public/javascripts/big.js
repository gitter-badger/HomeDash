<div class="torrent">
		<p><strong>name</strong></p>
		<div class="progress small-progress-bar">
		 	<div class="progress-bar" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 60%;">
		    	<span class="sr-only">60% Complete</span>
		  	</div>
		</div>
		<p>Ul:30kbs | Dl:10kbs</p>
	</div>
	
	
	
function torrentToHtml@{module.id}(torrent){
	var html = [];
	
	var percent = Math.ceil(torrent.percentDone *100);
	
	html.push('<div class="torrent" id="torrent-',torrent.id,'">');
	html.push('<p><strong>',torrent.name,'</strong></p>');
	html.push('<div class="progress small-progress-bar">');
	html.push('<div class="progress-bar" role="progressbar" aria-valuenow="',,'" aria-valuemin="0" aria-valuemax="100" style="width: ',torrent.percentDone,'%;">');
	html.push('<span class="sr-only">',torrent.percentDone,'% Complete</span>');
	html.push('</div>');
	html.push('</div>');
	html.push('<p>DL: ',humanFileSize(torrent.downloadSpeed, true),' | Ul: ',humanFileSize(torrent.uploadSpeed, true),'</p>');
}