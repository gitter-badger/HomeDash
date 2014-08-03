function transmission(moduleId){
	this.moduleId = moduleId;
	
	
	this.documentReady = function(){
		var parent = this;
		$("#addTorrent"+this.moduleId).click(function(){
			parent.addTorrent();
		});
		$("#altSpeed"+this.moduleId).click(function(){
			parent.setAltSpeed();
		});
	}

	this.addTorrent = function (event){
		var url = prompt("Margnet link");
		sendMessage(this.moduleId, 'addTorrent', url);
	}

	this.onMessage = function (method, message){
		if(method == 'refresh'){
			this.processData(message);
		}
	}
	
	this.setAltSpeed = function (){
		
		var button = $("#altSpeed"+this.moduleId);
		var setAltSpeed = true;
		if(button.hasClass("btn-primary")){
			setAltSpeed = false;
			button.removeClass("btn-primary");
		}else{
			setAltSpeed = true;
			button.addClass("btn-primary");
		}
		
		sendMessage(this.moduleId, 'altSpeed', setAltSpeed);
	}
	
	this.processData = function (json){
		if(!json){
			$("#transmission-"+this.moduleId+"-overlay").show();
		}else{
			$("#torrentcount"+this.moduleId).html(json.status.obj.map.torrentCount);
			$("#dlSpeed"+this.moduleId).html(this.humanFileSize(json.status.obj.map.downloadSpeed, true));
			$("#ulSpeed"+this.moduleId).html(this.humanFileSize(json.status.obj.map.uploadSpeed, true));
			
			if(json.alternateSpeeds){
				$("#altSpeed"+this.moduleId).addClass("btn-primary");
			}else{
				$("#altSpeed"+this.moduleId).removeClass("btn-primary");
			}
			
			$("#transmission-"+this.moduleId+"-overlay").hide();
		}
	}
	
	this.humanFileSize = function(bytes, si) {
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
	
	
}