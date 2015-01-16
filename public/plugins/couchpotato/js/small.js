function couchpotato(moduleId){
	this.moduleId = moduleId;
	
	
	this.searchMovie = function (){
		$("#cp"+this.moduleId+"-movieList").html('<p style="text-align:center"><img src="/assets/images/loading.gif" /></p>');
		$("#cp"+this.moduleId+"-modal").modal('show');
		sendMessage(this.moduleId, 'searchMovie', $("#cp"+this.moduleId+"-searchmovie-input").val());
	}

	this.onMessage = function (method, message){
		if(method == 'refresh'){
			this.processData(message);
		}else if(method == 'movieList'){
			this.populateMovieList(message);
		}else if(method == 'error'){
			$("#cp"+this.moduleId+"-modal").modal('hide');
		}
	}
	
	this.documentReady = function(){
			var parent = this;

			$("#cp"+this.moduleId+"-searchmovie").click(function(event){
				parent.searchMovie();
			});
			
			$("#cp"+this.moduleId+"-form").submit(function(){
				parent.searchMovie();
				return false;
			});
			
			$(document).on('click', ".cp"+this.moduleId+"-movie" , function(event){

				var movieName =$(this).attr("data-title");
				var imdb = $(this).attr("data-imdb");
				parent.addMovie(movieName, imdb);

			});
	}
	
	this.addMovie = function (movieName, imdb){
	
		sendMessage(this.moduleId, 'addMovie', movieName+'___'+imdb);
		$("#cp"+this.moduleId+"-modal").modal('hide');
	}
	
	this.populateMovieList = function (message){
		var parent = this;
		$("#cp"+this.moduleId+"-movieList").html('');
		$.each(message, function (index, value){
			$("#cp"+parent.moduleId+"-movieList").append(parent.movieToHtml(value));
			$("#cp"+parent.moduleId+"-movieList").append('<hr style="border-color: black; margin:0"/>');
		});
		
		$("#cp"+this.moduleId+"-searchmovie-input").val("");
				
	}
	
	this.movieToHtml = function(movie){
		var html = [];
		html.push('<div class="cp',this.moduleId,'-movie  cp-movie" data-imdb="',movie.imdbId,'" data-title="',movie.originalTitle);
		html.push('" style="background-image:url(',movie.poster,');">');
		html.push('<p class="cp-movie-name"><strong>',movie.originalTitle,' </strong>');
		
		if(movie.wanted){
		 	html.push('<small>(already wanted)</small>');
		}
		
		if(movie.inLibrary){
		 	html.push('<small>(already in library)</small>');
		}
		
		html.push('<span class="cp-movie-year" style="float:right">',movie.year,'</span></p>');
		
		html.push('</div>')
		return html.join('');
		
	}
	
	this.processData = function (message){
		if(!message){
			$("#"+this.moduleId+"-overlay").html('Couch potato is not available at the moment');
			$("#"+this.moduleId+"-overlay").show();
		}else{
			$("#module"+this.moduleId).css('background-image', 'url('+message+')');
		}
	}
}