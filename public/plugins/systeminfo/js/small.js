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

		

		this.cpuHistory = obj.cpuInfo;
		this.ramHistory = obj.ramInfo;

		if(this.ramHistory.length > 0 && this.cpuHistory.length > 0){
			var cpu = this.cpuHistory[this.cpuHistory.length -1].cpuUsage;
			var ram = this.ramHistory[this.ramHistory.length -1].percentageUsed;
			var cpuText = $('#cpu-txt-'+this.moduleId);
			var ramText = $('#ram-txt-'+this.moduleId);
			
			cpuText.countTo({from: parseInt(cpuText.html()), to: cpu});
			ramText.html(ram);
			
			//$('#cpu-txt-'+this.moduleId).html(this.cpuHistory[this.cpuHistory.length -1].cpuUsage);
			//$('#ram-txt-'+this.moduleId).html(this.ramHistory[this.ramHistory.length -1].percentageUsed);
		
			//$('#sys-info-svg-'+this.moduleId).html(this.generateSVG());
			$('#sys-info-'+this.moduleId+'-cpu-path').attr('d',this.cpuArrayToSVGGraph(this.cpuHistory));
			$('#sys-info-'+this.moduleId+'-ram-path').attr('d',this.ramArrayToSVGGraph(this.ramHistory));
		}
		var parent = this;

		/*var i = 0;
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
		*/
		
	};
	
	this.generateSVG = function(){
		var html = [];
		
		html.push('<svg class="sys-info-graph" preserveAspectRatio="none" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 100 100">');
	    html.push('<g class="surfaces">');
	    
	    var cpuSvg = this.cpuArrayToSVGGraph(this.cpuHistory);
	    html.push('<path class="cpu-svg" d="');
	    html.push(cpuSvg);
	    html.push('"></path>');
	        
	    var ramSvg = this.ramArrayToSVGGraph(this.ramHistory);
	    html.push('<path class="ram-svg" d="');
	    html.push(ramSvg);
	    html.push('"></path>');
	    
	    html.push('</g>');
	    html.push('</svg>');
	    
	    return html.join('');
		
	}
	
	
	
	this.cpuArrayToSVGGraph = function(array){
		var html = [];
		html.push('M0,100');
		var lastIndex = 0;
		var step = 100/array.length;
    	html.push(' L0,',100-array[0].cpuUsage);
		 $.each(array, function(index, cpuInfo){
			 
		    	html.push(' L',(index+1)*step,',',100-cpuInfo.cpuUsage);
		    	lastIndex = index*step;
		    });
		 html.push(' L',100,',100 Z');
		 return html.join('');
	}
	
	this.ramArrayToSVGGraph = function(array){
		var html = [];
		html.push('M0,100');
		var lastIndex = 0;
		var step = 100/array.length;
    	html.push(' L0,',100-array[0].percentageUsed);
		 $.each(array, function(index, ramInfo){
			 
		    	html.push(' L',(index+1)*step,',',100-ramInfo.percentageUsed);
		    	lastIndex = index*step;
		    });
		 html.push(' L',100,',100 Z');
		 return html.join('');
	}

	/*this.getDiskSpaceSVG = function(percentage){
		
		var html = [];
		html.push('<svg class="sys-info-hdd" preserveAspectRatio="none" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 100 100">');
	    html.push('<g class="surfaces">');
	    html.push('<rect class="sys-info-hdd-rect-full" x="0" y="0" width="100" height="100" />');
	    html.push('<rect class="sys-info-hdd-rect" x="0" y="0" width="',percentage,'" height="100" />');
	    html.push('</g>');
	    html.push('</svg>');
	   	    
	    return html.join('');
	}*/
	
	/*this.getDiskSpaceHtml = function(path, diskSpace) {
		var html = [];
		var totalSpace = diskSpace.total;
		var usedSpace = diskSpace.used;
		var percentage = Math.ceil((usedSpace / totalSpace) * 100);

		
		
		html.push('<div class="hdd-info">');
		html.push('<span>', diskSpace.pretty,'</span>');
		html.push('<p>', path, '</p>');
		html.push(this.getDiskSpaceSVG(percentage));

		html.push('</div>');
		
		
		return html.join("");
	}*/
}