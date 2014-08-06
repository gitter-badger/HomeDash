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
			$("#cp"+parent.moduleId+"-movieList").append('<hr style="margin:0"/>');
		});
		
		$("#cp"+this.moduleId+"-searchmovie-input").val("");
				
	}
	
	this.movieToHtml = function(movie){
		var html = [];
		html.push('<div class="cp',this.moduleId,'-movie" data-imdb="',movie.imdbId,'" data-title="',movie.originalTitle);
		html.push('" style="cursor:pointer; padding:20px 0 20px 0">');
		html.push('<p style="margin:0"><strong>',movie.originalTitle,' </strong>');
		
		if(movie.wanted){
		 	html.push('<small>(already wanted)</small>');
		}
		
		if(movie.inLibrary){
		 	html.push('<small>(already in library)</small>');
		}
		
		html.push('<span style="float:right">',movie.year,'</span></p>');
		
		html.push('</div>')
		return html.join('');
		
	}
	
	this.processData = function (message){
		if(message){
			$("#cp"+this.moduleId+"-overlay").hide();
		}else{
			$("#cp"+this.moduleId+"-overlay").show();
		}
	}
}