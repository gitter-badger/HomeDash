function harddisk(moduleId) {
	this.moduleId = moduleId;

	this.documentReady = function() {
	}

	this.onMessage = function(method, obj) {

		if (method == 'refresh') {
			this.processData(obj);
		}

	}

	this.processData = function(diskSpace) {
		$("#hdd" + this.moduleId + "-path").html(diskSpace.path);
		$("#hdd" + this.moduleId + "-data").html(diskSpace.pretty);
		
		var totalSpace = diskSpace.total;
		var usedSpace = diskSpace.used;
		var percentage = Math.ceil((usedSpace / totalSpace) * 100);
		
		$("#hdd" + this.moduleId + "-harddisk").html(this.getDiskSpaceSVG(percentage));
	};
	

	

	this.getDiskSpaceSVG = function(percentage){
		
		var html = [];
		html.push('<svg class="hdd-svg" preserveAspectRatio="none" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 100 100">');
	    html.push('<g class="surfaces">');
	    html.push('<rect class="hdd-rect-full" x="0" y="0" width="100" height="100" />');
	    html.push('<rect class="hdd-rect" x="0" y="0" width="',percentage,'" height="100" />');
	    html.push('</g>');
	    html.push('</svg>');
	   	    
	    return html.join('');
	}
	
	
}