function systeminfo(moduleId) {
	this.moduleId = moduleId;
	this.cpuHistory = [];
	this.ramHistory = [];
	this.documentReady = function() {
	}

	this.onMessage = function(method, obj) {

		if (method == 'refresh') {
			this.processData(obj);
		}

	}

	this.processData = function(obj) {

		var html = [];

		var ramPercent = Math.ceil((obj.usedRam / obj.maxRam) * 100);

		var cpu = Math.ceil(obj.cpuUsage);

		/*$("#ramText" + this.moduleId).html(
				humanFileSize(obj.usedRam, true) + " / "
						+ humanFileSize(obj.maxRam, true));

		var ramProgress = $("#ram" + this.moduleId);
		ramProgress.css("width", ramPercent + "%");
		ramProgress
				.removeClass("progress-bar-danger progress-bar-warning progress-bar-info");

		if (ramPercent > 90) {
			ramProgress.addClass("progress-bar-danger");
		} else if (ramPercent > 70) {
			ramProgress.addClass("progress-bar-warning");
		} else if (ramPercent > 50) {
			ramProgress.addClass("progress-bar-info");
		}

		var cpuProgress = $("#cpu" + this.moduleId);
		cpuProgress.css("width", cpu + "%");

		cpuProgress.removeClass("progress-bar-danger progress-bar-warning progress-bar-info progress-bar-success");
		
		

		if (cpu > 90) {
			cpuProgress.addClass("progress-bar-danger");
		} else if (cpu > 70) {
			cpuProgress.addClass("progress-bar-warning");
		} else if (cpu > 50) {
			cpuProgress.addClass("progress-bar-info");
		} else if (cpu < 25) {
			cpuProgress.addClass("progress-bar-success");
		}*/
		
		$('#cpu-txt-'+this.moduleId).html(cpu);
		$('#ram-txt-'+this.moduleId).html(ramPercent);

		this.cpuHistory.push(cpu);
		this.ramHistory.push(ramPercent);
		
		if(this.cpuHistory.length > 100){
			this.cpuHistory.shift();
		}
		
		if(this.ramHistory.length > 100){
			this.ramHistory.shift();
		}
		
		var parent = this;

		var i = 0;
		$.each(obj.diskSpace, function(path, value) {
			html.push(parent.getDiskSpaceHtml(path, value));
			i++;
		});

		if (i == 0) {
			$('#diskUsage' + this.moduleId).hide();
		} else {
			$('#diskUsage' + this.moduleId).show();
		}
		
		$("#systeminfo" + this.moduleId + "-harddisk").html(html.join(""));
		
		$('#sys-info-svg-'+this.moduleId).html(this.generateSVG());
	};
	
	this.generateSVG = function(){
		var html = [];
		
		html.push('<svg class="sys-info-graph" preserveAspectRatio="none" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 100 100">');
	    html.push('<g class="surfaces">');
	    
	    var cpuSvg = this.arrayToSVGGraph(this.cpuHistory);
	    html.push('<path class="cpu-svg" d="');
	    html.push(cpuSvg);
	    html.push('"></path>');
	        
	    var ramSvg = this.arrayToSVGGraph(this.ramHistory);
	    html.push('<path class="ram-svg" d="');
	    html.push(ramSvg);
	    html.push('"></path>');
	    
	    html.push('</g>');
	    html.push('</svg>');
	    return html.join('');
		
	}
	
	this.arrayToSVGGraph = function(array){
		var html = [];
		html.push('M0,100');
		var lastIndex = 0;
		var step = 100/array.length;
    	html.push(' L0,',100-array[0]);
		 $.each(array, function(index, percent){
			 
		    	html.push(' L',(index+1)*step,',',100-percent);
		    	lastIndex = index*step;
		    });
		 html.push(' L',100,',100 Z');
		 return html.join('');
	}

	this.getDiskSpaceSVG = function(percentage){
		
		var html = [];
		html.push('<svg class="sys-info-hdd" preserveAspectRatio="none" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 100 100">');
	    html.push('<g class="surfaces">');
	    html.push('<rect class="sys-info-hdd-rect-full" x="0" y="0" width="100" height="100" />');
	    html.push('<rect class="sys-info-hdd-rect" x="0" y="0" width="',percentage,'" height="100" />');
	    html.push('</g>');
	    html.push('</svg>');
	    
	    console.log(html.join(''));
	    
	    return html.join('');
	}
	
	this.getDiskSpaceHtml = function(path, diskSpace) {
		var html = [];
		var totalSpace = diskSpace[0].split(" ")[0];
		var usedSpace = diskSpace[2].split(" ")[0];
		var freeSpace = diskSpace[1].split(" ")[0];
		var percentage = Math.ceil((usedSpace / totalSpace) * 100);

		/*var progressColor = "progress-bar-success";

		if (percentage > 90) {
			progressColor = "progress-bar-danger";
		} else if (percentage > 70) {
			progressColor = "progress-bar-warning";
		} else if (percentage > 50) {
			progressColor = "progress-bar-info";
		}*/
		
		html.push('<div class="hdd-info">');
		html.push('<span>', usedSpace, '/', diskSpace[0],'</span>');
		html.push('<p>', path, '</p>');
		html.push(this.getDiskSpaceSVG(percentage));

		html.push('</div>');
		
		/*html.push('<div class="progress">');
		html.push('<div class="progress-bar ', progressColor,
				'" role="progressbar" aria-valuenow="', usedSpace,
				'" aria-valuemin="0" aria-valuemax="', totalSpace,
				'" style="width: ', percentage, '%">');
		html.push('</div></div>');
		 */
		return html.join("");
	}
}