function googlepubliccalendar(moduleId){
	this.moduleId = moduleId;
	
	this.documentReady = function(){
		var parent = this;
		$(document).on('click', '.gcal'+this.moduleId+'-event',function(event){
			var tr = $(this);
			var link = '<a href="'+tr.attr+'" target="_blank">Link to the event</a>'
			
			$('#gcal'+parent.moduleId+'-eventName').html(tr.attr('data-title'));
			$('#gcal'+parent.moduleId+'-eventDate').html(tr.attr('data-date'));
			$('#gcal'+parent.moduleId+'-eventDescription').html(parent.url2link(parent.nl2br(tr.attr('data-description')))+'<br /><br />'+link);
			
			$('#gcal'+parent.moduleId+'-modal').modal('show');
		});
	}
	
	this.onMessage = function (method, message){
		if(method == 'refresh'){
			this.processData(message);
		}else if(method == 'error'){
			$("#cp"+this.moduleId+"-modal").modal('hide');
		}
	}
	
	this.processData = function(message){
		$('#gcal'+this.moduleId+'-title').html(message.title);
		var parent = this;
		
		var table = $('#gcal'+this.moduleId+'-events tbody');
		
		table.html('');
		
		$.each(message.events, function(index, event){
			var html = [];
			
			html.push('<tr data-date="',event.startTime,'" data-title="',event.summary,'" data-description="',event.description,'" data-href="', event.link,'" class="gcal-event gcal',parent.moduleId, '-event">');
			html.push('<td>', event.startTime, '</td><td>', event.summary, '</td>');
			html.push('</tr>');
			
			
			table.append(html.join(''));
		});
	}
	
	this.nl2br = function (str, is_xhtml) {
	    var breakTag = (is_xhtml || typeof is_xhtml === 'undefined') ? '<br />' : '<br>';
	    return (str + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1' + breakTag + '$2');
	}
	
	this.url2link = function(text)
    {
	      var exp = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
	      return text.replace(exp,'<a href="$1" target="_blank">$1</a>'); 
	    }
	
	
		
}