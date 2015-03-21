function yamahaamp(moduleId){
	this.moduleId = moduleId;
	
	
	this.documentReady = function(){
		var parent = this;
		$("#amp"+this.moduleId+"-power").click(function(){
			sendMessage(parent.moduleId, 'ampCommand', 'On/Standby');
		});
		
		$(document).on('click', "#amp"+this.moduleId+"-power2", function(){
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
		$('#amp'+this.moduleId+'-name').html(json.name);
		if(json != undefined && json.on != undefined && json.on){
			$("#amp"+this.moduleId+"-power").addClass("btn-success");
			
			$("#amp"+this.moduleId+"-volume").html(json.volume);
		
			$(".input-button").removeClass("active");
			$('.input-button[data="'+json.input+'"]').addClass("active");
			$("#"+this.moduleId+"-overlay").hide();
		}else{
			$(".amp"+this.moduleId+"-power").removeClass("btn-success");
			var html = [];
			html.push('<p>The amplifier is off, turn it on</p><button id="amp'+this.moduleId+'-power2" class="btn yamp-power"><i class="fa fa-power-off"></i></button>');
			$("#"+this.moduleId+"-overlay").html(html);
			$("#"+this.moduleId+"-overlay").show();
		}
		
		
	}
}