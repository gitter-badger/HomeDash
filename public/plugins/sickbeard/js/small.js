function sickbeard(moduleId) {
	this.moduleId = moduleId;
	this.shows = [];
	this.documentReady = function() {
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
			this.refreshShows(this.shows);
		}

	}
	
	this.refreshShows = function(shows){
		var body = $("#sb-shows-" + this.moduleId);
		body.remove();
		var container = $('#sb-slide-container-'+this.moduleId);
		container.html('<div id="sb-shows-' + this.moduleId+'" class="sb-shows-container"></div>'); 
		
		body = $("#sb-shows-" + this.moduleId + "");
		body.html("");

		var parent = this;

		$.each(shows, function(index, value) {
			var html = [];

			// html.push('<tr><td>');
			// html.push(value.nextShowingReadable);
			// html.push('</td><td>');
			// html.push(value.name);
			// html.push('</td></tr>');

			html.push(parent.showToHtml(value));

			body.append(html.join(''));
		});

		// starting slideshow
		body.slidesjs({
			height : 500,
			navigation : {
				active : true,
				effect : "slide"
			// [string] Can be either "slide" or "fade".
			},
			pagination : {
				active : false,
				effect : "slide"
			},
			callback : {
				loaded : function(number) {
					$('.sickbeard .slidesjs-next').html('<i class="fa fa-chevron-right"></i>');
					$('.sickbeard .slidesjs-previous').html('<i class="fa fa-chevron-left"></i>');
				}
			},
			play: {
			      active: false,
			        // [boolean] Generate the play and stop buttons.
			        // You cannot use your own buttons. Sorry.
			      effect: "slide",
			        // [string] Can be either "slide" or "fade".
			      interval: 5000,
			        // [number] Time spent on each slide in milliseconds.
			      auto: true,
			        // [boolean] Start playing the slideshow on load.
			      swap: true,
			        // [boolean] show/hide stop and play buttons
			      pauseOnHover: true,
			        // [boolean] pause a playing slideshow on hover
			      restartDelay: 2500
			        // [number] restart delay on inactive slideshow
			    }
		});
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
		html.push('<div class="sb-show" style="background-image:url(',
				show.poster, ')">');
		html.push('<div class="sb-show-info">');
		html.push('<p>');
		html.push('<span class="sb-show-date">', show.nextShowingReadable,'</span>');
		html.push('<span class="sb-show-title">', show.name, '</span>');
		html.push('</p>');
		html.push('</div>');
		html.push('</div>');

		return html.join('');
	}
}