function twitter(moduleId){
	this.moduleId = moduleId;
	this.tweets;
	this.currentIndex;
	this.documentReady = function(){

		var parent = this;
		$('#twitter-'+this.moduleId+'-previous').click(function(){parent.showPreviousTweet()});
		$('#twitter-'+this.moduleId+'-next').click(function(){parent.showNextTweet()});
		$('#twitter-'+this.moduleId+'-tweet').click(function(){parent.newTweet()});
		$(document).on('click', '.twitter-userid', function(){
			
			$("#twitter-"+parent.moduleId+"-modal .modal-body").html('<p style="text-align:center"><img src="/assets/images/loading.gif" /></p>');
			$("#twitter-"+parent.moduleId+"-modal").appendTo("body").modal('show');
			
			sendMessage(parent.moduleId, 'showUser', $(this).attr('data'));
		});

		$(document).on('click', '.twitter-hashtag', function(){
	
			$("#twitter-"+parent.moduleId+"-modal .modal-body").html('<p style="text-align:center"><img src="/assets/images/loading.gif" /></p>');
			$("#twitter-"+parent.moduleId+"-modal").appendTo("body").modal('show');
			
			sendMessage(parent.moduleId, 'showHashtag', $(this).attr('data'));
		});
	}

	this.onMessage = function (method, message, extra){
		if(method == 'refresh'){
			this.processData(message);
		}else if(method == 'showUser'){
			this.showTweets(message);
		}else if( method == 'showHashtag'){
			this.showTweets(message);
		}else if(method == 'error'){
			$("#twitter-"+this.moduleId+"-modal").modal('hide');
		}
		else if(method == 'success'){
			this.processData(extra);
		}
	}
	
	this.processData = function(tweets){
		this.tweets = tweets;
		this.currentIndex = 0;
		this.showTweet();
	}
	
	this.showTweet = function(){
		
		if(this.currentIndex >= this.tweets.length){
			this.currentIndex = 0;
		}
		
		if(this.currentIndex < 0){
			this.currentIndex =  this.tweets.length-1;
		}
		$('#twitter-'+this.moduleId).html(this.tweetToHtml(this.tweets[this.currentIndex]));
	}
	
	this.tweetToHtml = function(tweet){
		var html = [];
		var content = tweet.content;
	    var pattern = new RegExp("@([0-9A-Za-z\u00C0-\u017F-_]+)", 'g');
	    content = content.replace(pattern, '<a class="twitter-userid" data="$1">@$1</a>');
	    
	    var hashtag = new RegExp("#([0-9A-Za-z\u00C0-\u017F-_]+)", 'g');
	    content = content.replace(hashtag, '<a class="twitter-hashtag" data="$1">#$1</a>');
	    
	    var urlRegex = new RegExp("[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)", 'ig');
	    content = content.replace(urlRegex, '<a href="$&" target="_blank" class="twitter-link">$&</a>');
	    
		html.push('<div class="animated fadeIn" id="twitter-',this.moduleId,'-tweet">');
		html.push('<p class="twitter-username">');
		html.push('<img src="',tweet.userPicture,'"/><a class="twitter-userid" data="',tweet.userId,'">',tweet.username, '</a></p>');
		html.push('<p class="twitter-content">', content,'</p>')
		html.push('<p class="twitter-date">', tweet.date,'</p>')
		html.push('</div>');
		return html.join('');
	}
	
	this.showPreviousTweet = function(event){
		this.currentIndex = this.currentIndex + 1;
		this.showTweet();
	}
	
	this.showNextTweet = function(event){
		this.currentIndex = this.currentIndex - 1;
		this.showTweet();
	}
	
	
	this.showTweets = function(tweets){
		var body = $("#twitter-"+this.moduleId+"-modal .modal-body");
		body.html('');
		var parent=  this;
		$.each(tweets, function(index, tweet){
			body.append(parent.tweetToHtml(tweet));
			body.append('<hr />');
		});
		
	}
	
	this.newTweet = function(){
		var tweet = prompt('Tweet', '#homedash');
		if(tweet != undefined){
			tweet = $.trim(tweet);
			if(tweet != ''){
				sendMessage(this.moduleId, 'newTweet', tweet);
			}
		}
	}
	
}