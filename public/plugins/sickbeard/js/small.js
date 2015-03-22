function sickbeard(moduleId) {
	this.moduleId = moduleId;
	this.shows = [];
	this.animation = "slideInRight";
	this.interval;
	this.timeout;
	this.currentIndex = 0;
	this.documentReady = function() {

		var parent = this;
		$('#sb-' + this.moduleId + '-previous').click(function() {
			parent.showPreviousShow();
			clearInterval(parent.interval);
			clearTimeout(parent.timeout);
			parent.timeout = setTimeout(function(){parent.playSlideShow()}, 10000);
		});
		$('#sb-' + this.moduleId + '-next').click(function() {
			parent.showNextShow();
			clearInterval(parent.interval);
			clearTimeout(parent.timeout);
			parent.timeout = setTimeout(function(){parent.playSlideShow()}, 10000);
		});
		
		this.playSlideShow();
	}
	
	this.playSlideShow = function(){
		var parent = this;
		this.interval =  setInterval(function(){
			parent.showNextShow();
		}, 7000);
	}

	this.onPageChange = function(page) {
		// this.refreshShows(this.shows);
	}

	this.onMessage = function(method, message) {
		if (method == 'refresh') {
			this.processData(message);
		}
	}

	this.processData = function(message) {
		if (this.compareShows(message, this.shows)) {
			return;
		} else {
			this.shows = message;
			this.currentIndex = 0;
			var body = $("#sb-shows-" + this.moduleId);
			for(i = 0; i < this.shows.length; i++){
				body.append(this.showToHtml(this.shows[i], i));
			}

			this.showShow();
		}

	}

	this.showShow = function() {
		//preloading images
		
		
		var links = $('#sb-' + this.moduleId + '-next, #sb-' + this.moduleId + '-previous');
		links.attr('disabled', true);


		var oldShow = $("#sb-shows-" + this.moduleId+' .sb-show.active');
		
		if (this.currentIndex >= this.shows.length) {
			this.currentIndex = 0;
		}

		if (this.currentIndex < 0) {
			this.currentIndex = this.shows.length - 1;
		}
		
		var newShow = $("#sb-shows-" + this.moduleId+' .sb-show[data-show='+this.currentIndex+']');
		var newAnimation = this.animation;
		newShow.addClass(newAnimation + ' active');
		
		var oldAnimation;
		if(this.animation == 'slideInLeft'){
			oldAnimation = 'slideOutRight';
		}else{
			oldAnimation = 'slideOutLeft';
		}

		oldShow.addClass(oldAnimation);

		
		setTimeout(function() {
			oldShow.removeClass(oldAnimation);
			oldShow.removeClass('active');
			newShow.removeClass(newAnimation);
			links.removeAttr('disabled');
		}, 600);
	}

	this.compareShows = function(a, b) {
		if (a === b)
			return true;
		if (a == null || b == null)
			return false;
		if (a.length != b.length)
			return false;

		// If you don't care about the order of the elements inside
		// the array, you should sort both arrays here.

		for (var i = 0; i < a.length; ++i) {
			if (a[i].showId !== b[i].showId)
				return false;
		}
		return true;
	}

	this.showToHtml = function(show, index) {
		var html = [];
		html.push('<div class="sb-show animated" data-show="',index,'" style="background-image:url(', show.poster, ')">');
		html.push('<div class="sb-show-info">');
		html.push('<p>');
		html.push('<span class="sb-show-date">', show.nextShowingReadable, '</span>');
		html.push('<span class="sb-show-title">', show.name, '</span>');
		html.push('</p>');
		html.push('</div>');
		html.push('</div>');

		return html.join('');
	}

	this.showPreviousShow = function(event) {
		this.currentIndex = this.currentIndex - 1;
		this.animation = "slideInLeft";
		this.showShow();
	}

	this.showNextShow = function(event) {
		this.currentIndex = this.currentIndex + 1;
		this.animation = "slideInRight"
		this.showShow();
	}
}