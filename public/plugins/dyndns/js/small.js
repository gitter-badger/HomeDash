function dyndns(moduleId){
	this.moduleId = moduleId;
	
	
	this.documentReady = function(){
		alert('yo');
	};

	this.onMessage = function (method, message){
		if(method == 'refresh'){
			this.processData(message);
		}
	};
	
	this.processData = function (json){
		
	};
}