function portmapper(moduleId){
	this.moduleId = moduleId;
	this.onMessage = function(command, message){
		if(command == 'getRouter'){
			this.getRouter(message);
		}else if(command = 'refresh' || command == 'getMappings'){
			this.getMappings(message);
		}
	}
	
	this.documentReady = function(){
		var parent = this;
		
		$('#mapper'+this.moduleId+'-overlay').show();
		$('#mapper'+this.moduleId+'-add').click(function(){
			$("#mapper"+parent.moduleId+"-modal").modal('show');
		});
		
		$('#mapper'+this.moduleId+'-form').submit(function(){
			parent.addPort();
			return false;
		});
		
		$('#mapper'+this.moduleId+'-savePort').click(function(){
			$('#mapper'+parent.moduleId+'-form').submit();
		});
		
		$(document).on('click', '.mapper'+this.moduleId+'-remove-port', function(){
			parent.removePort($(this).attr('data'));
		});
	}
	
	this.onConnect = function(){
		//alert(this.moduleId);
		sendMessage(this.moduleId, 'getRouter', '');
	}
	
	this.getRouter = function(router){
		$('#mapper'+this.moduleId+'-routerName').html(router.name);
		$('#mapper'+this.moduleId+'-ip').html(router.externalIp);
		$('#mapper'+this.moduleId+'-overlay').hide();
		sendMessage(this.moduleId, 'getMappings','');
	}
	
	this.getMappings = function(mappings){
		var table = $("#mapper"+this.moduleId+'-ports tbody');
		table.html('');
		var parent = this;
		$.each(mappings, function(index, mapping){
			table.append(parent.port2Html(mapping));
		});
	}
	
	this.port2Html = function(mapping){
		var html = [];
		html.push('<tr>');
		html.push('<td>',mapping.name,'</td>');
		html.push('<td>',mapping.protocol,'</td>');
		html.push('<td>',mapping.externalPort,'</td>');
		html.push('<td>',mapping.internalPort,'</td>');
		html.push('<td>',mapping.internalIp,'</td>');
		html.push('<td><button class="btn btn-danger btn-xs mapper',this.moduleId,'-remove-port" data="',mapping.externalPort,'|',mapping.protocol,'"><span class="glyphicon glyphicon-remove"></span></button></td>');
		html.push('</tr>');
		
		return html.join('');
	}
	
	this.addPort = function(){
		var name = $('#mapper'+this.moduleId+'-form input[name="name"]').val();
		var protocol = $('#mapper'+this.moduleId+'-form select[name="protocol"]').val();
		var externalPort = $('#mapper'+this.moduleId+'-form input[name="externalPort"]').val();
		var internalPort = $('#mapper'+this.moduleId+'-form input[name="internalPort"]').val();
		var internalIp = $('#mapper'+this.moduleId+'-form input[name="client"]').val();
		
		var force =  $('#mapper'+this.moduleId+'-form input[name="force"]:checked').length > 0;
		
		var data = [];
		data.push(name,'|',protocol,'|', externalPort,'|', internalPort,'|',internalIp);
				
		if(force){
			sendMessage(this.moduleId, 'addPortForce', data.join(''));
		}else{
			sendMessage(this.moduleId, 'addPort', data.join(''));
		}
		
		$("#mapper"+this.moduleId+"-modal").modal('hide');

	}
	
	this.removePort = function(data){
		if(confirm('Delete this port ?')){
			sendMessage(this.moduleId, 'removePort', data);
		}
	}
	
	
}