function sickbeard(moduleId) {
	this.moduleId = moduleId;
	this.shows = [];
	this.documentReady = function() {
		
		var parent = this;
		$('#sb-'+this.moduleId+'-previous').click(function(){parent.showPreviousShow()});
		$('#sb-'+this.moduleId+'-next').click(function(){parent.showNextShow()});
	}

	this.onPageChange = function(page){
		//this.refreshShows(this.shows);
	}
	
	this.onMessage = function(method, message) {
		if (method == 'refresh') {
			this.processData(message);
		}
	}

	this.processData = function(message) {
		if(this.compareShows(message,this.shows)){
			return;
		}else{
			this.shows = message;
			this.currentIndex = 0;
			this.showShow();
		}

	}
	
	
	
	this.showShow = function(){
		var body = $("#sb-shows-" + this.moduleId);

		if(this.currentIndex >= this.shows.length){
			this.currentIndex = 0;
		}
		
		if(this.currentIndex < 0){
			this.currentIndex =  this.shows.length -1;
		}
		body.html(this.showToHtml(this.shows[this.currentIndex]));	
	}

	this.compareShows = function(a, b){
		if (a === b) return true;
		  if (a == null || b == null) return false;
		  if (a.length != b.length) return false;

		  // If you don't care about the order of the elements inside
		  // the array, you should sort both arrays here.

		  for (var i = 0; i < a.length; ++i) {
		    if (a[i].showId !== b[i].showId) return false;
		  }
		  return true;
	}
	
	this.showToHtml = function(show) {
		var html = [];
		html.push('<div class="sb-show animated fadeIn" style="background-image:url(',show.poster, ')">');
		html.push('<div class="sb-show-info">');
		html.push('<p>');
		html.push('<span class="sb-show-date">', show.nextShowingReadable,'</span>');
		html.push('<span class="sb-show-title">', show.name, '</span>');
		html.push('</p>');
		html.push('</div>');
		html.push('</div>');

		return html.join('');
	}
	
	this.showPreviousShow = function(event){
		this.currentIndex = this.currentIndex + 1;
		this.showShow();
	}
	
	this.showNextShow = function(event){
		this.currentIndex = this.currentIndex - 1;
		this.showShow();
	}
}