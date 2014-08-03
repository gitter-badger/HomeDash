function yamahaamp(moduleId){
	this.moduleId = moduleId;
	
	
	this.documentReady = function(){
		var parent = this;
		$("#amp"+this.moduleId+"-power").click(function(){
			sendMessage(parent.moduleId, 'ampCommand', 'On/Standby');
		});
		
		$("#amp"+this.moduleId+"-power2").click(function(){
			sendMessage(parent.moduleId, 'ampCommand', 'On/Standby');
		});
		
		$("#amp"+this.moduleId+"-volumeDown").click(function(){
			sendMessage(parent.moduleId, 'ampCommand', 'volumeDown');
			var volume = parseFloat($("#amp"+parent.moduleId+"-volume").html());
			volume -= 0.5;
			//$("#amp@module.id-volume").html(volume);
		});
		
		
		$("#amp"+parent.moduleId+"-volumeUp").click(function(){
			sendMessage(parent.moduleId, 'ampCommand', 'volumeUp');
			var volume = parseFloat($("#amp"+parent.moduleId+"-volume").html());
			volume = volume + 0.5;
			//$("#amp@module.id-volume").html(volume);
		});
		
		$(".input-button").click(function(){
			sendMessage(parent.moduleId, 'ampCommand', $(this).attr("data"));
			$(".input-button").removeClass("active");
			$(this).addClass("active");
		});
	
	}

	this.onMessage = function (method, message){
		if(method == 'refresh'){
			this.processData(message);
		}
	}
	
	this.processData = function (json){
		if(json != undefined && json.on != undefined && json.on){
			$("#amp"+this.moduleId+"-power").addClass("btn-success");
			
			$("#amp"+this.moduleId+"-volume").html(json.volume);
		
			$(".input-button").removeClass("active");
			$('.input-button[data="'+json.input+'"]').addClass("active");
			$("#amp"+this.moduleId+"-overlay").hide();
		}else{
			$(".amp"+this.moduleId+"-power").removeClass("btn-success");
			$("#amp"+this.moduleId+"-overlay").show();
		}
		
		
	}
}