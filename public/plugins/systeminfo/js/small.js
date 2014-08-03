function systeminfo(moduleId){
	this.moduleId = moduleId;
	this.documentReady = function(){}

	this.onMessage = function(method, obj){

		if(method == 'refresh'){
			this.processData(obj);
		}
		
	}
	
	this.processData = function(obj){

		var html = [];
		
		var ramPercent = Math.ceil((obj.usedRam / obj.maxRam) * 100);
		
		var cpu = Math.ceil(obj.cpuUsage*100);
				
		
		
		$("#ramText"+this.moduleId).html(humanFileSize(obj.usedRam, true) +" / "+humanFileSize(obj.maxRam, true));
		
		
		var ramProgress = $("#ram"+this.moduleId);
		ramProgress.css("width", ramPercent+"%");
		ramProgress.removeClass("progress-bar-danger progress-bar-warning progress-bar-info");
		
		if(ramPercent > 90){
			ramProgress.addClass("progress-bar-danger");
		}else if(ramPercent > 70){
			ramProgress.addClass("progress-bar-warning");
		}else if(ramPercent > 50){
			ramProgress.addClass("progress-bar-info");
		}
		
		var cpuProgress = $("#cpu"+this.moduleId);
		cpuProgress.css("width", cpu+"%");

		cpuProgress.removeClass("progress-bar-danger progress-bar-warning progress-bar-info progress-bar-success");
		
		if(cpu > 90){
			cpuProgress.addClass("progress-bar-danger");
		}else if(cpu > 70){
			cpuProgress.addClass("progress-bar-warning");
		}else if(cpu > 50){
			cpuProgress.addClass("progress-bar-info");
		}else if(cpu < 25){
			cpuProgress.addClass("progress-bar-success");
		}
		
		var parent = this;
		$.each(obj.diskSpace, function(path, value){
			html.push(parent.getDiskSpaceHtml(path, value));
		});
		
		$("#systeminfo"+this.moduleId+"-harddisk").html(html.join(""));
	}
	
	this.getDiskSpaceHtml = function (path, diskSpace){
		var html = [];
		var totalSpace = diskSpace[0].split(" ")[0];
		var usedSpace = diskSpace[2].split(" ")[0];
		var freeSpace = diskSpace[1].split(" ")[0];
		var percentage = Math.ceil((usedSpace/totalSpace) * 100);
		
		var progressColor = "progress-bar-success";
		
		if(percentage > 90){
			progressColor = "progress-bar-danger";
		}else if(percentage > 70){
			progressColor = "progress-bar-warning";
		}else if(percentage > 50){
			progressColor = "progress-bar-info";
		}
		
		html.push('<div class="progress">');
		html.push('<div class="progress-bar ', progressColor, '" role="progressbar" aria-valuenow="', usedSpace, '" aria-valuemin="0" aria-valuemax="', totalSpace, '" style="width: ',percentage, '%">');
		html.push('"',path,'": ',usedSpace,' / ',diskSpace[0]);
		html.push('</div></div>');
				
		return html.join("");
	}
}