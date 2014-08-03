function sickbeard(moduleId){
	this.moduleId = moduleId;
	
	this.documentReady = function(){}
	
	this.onMessage = function (method, message){
		if(method == 'refresh'){
			this.processData(message);
		}
	}
	
	this.processData = function (message){
		var body = $("#sb-comingShows"+this.moduleId+" tbody");
		body.html("");
		
		$.each(message, function(index, value){
			var html = [];
			
			html.push('<tr><td>');
			html.push(value.nextShowingReadable);
			html.push('</td><td>');
			html.push(value.name);
			html.push('</td></tr>');
			
			body.append(html.join(''));
		});
	}
}