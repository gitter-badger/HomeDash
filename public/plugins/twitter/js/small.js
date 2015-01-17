function twitter(moduleId){
	this.moduleId = moduleId;
	this.tweets;
	this.currentIndex;
	this.documentReady = function(){

		var parent = this;
		$('#twitter-'+this.moduleId+'-previous').click(function(){parent.showPreviousTweet()});
		$('#twitter-'+this.moduleId+'-next').click(function(){parent.showNextTweet()});
	}

	this.onMessage = function (method, message){
		if(method == 'refresh'){
			this.processData(message);
		}
	}
	
	this.processData = function(tweets){
		this.tweets = tweets;
		this.currentIndex = tweets.length-1;
		this.showTweet();
	}
	
	this.showTweet = function(){
		
		if(this.currentIndex >= this.tweets.length){
			this.currentIndex = 0;
		}
		
		if(this.currentIndex < 0){
			this.currentIndex =  this.tweets.length -1;
		}
		$('#twitter-'+this.moduleId).html(this.tweetToHtml(this.tweets[this.currentIndex]));
	}
	
	this.tweetToHtml = function(tweet){
		var html = [];
		html.push('<div class="animated fadeIn" id="twitter-',this.moduleId,'-tweet">');
		html.push('<p class="twitter-username">');
		html.push('<img src="',tweet.userPicture,'"/>',tweet.username, '</p>');
		html.push('<p class="twitter-content">', tweet.content,'</p>')
		html.push('<p class="twitter-date">', tweet.date,'</p>')
		html.push('</div>');
		return html.join('');
	}
	
	this.showPreviousTweet = function(event){
		this.currentIndex = this.currentIndex - 1;
		this.showTweet();
	}
	
	this.showNextTweet = function(event){
		this.currentIndex = this.currentIndex + 1;
		this.showTweet();
	}
}