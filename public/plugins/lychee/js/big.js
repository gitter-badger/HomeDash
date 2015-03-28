function lychee(moduleId) {
	this.moduleId = moduleId;
	this.currentAlbum;
	this.shownPictures = [];
	this.selectedPictureIndex = 0;
	this.documentReady = function() {
		
		var parent = this;
		$(document).on('click', '.album', function(){
			parent.showAlbum($(this).attr('data-id'), $(this).attr('data-title'));
		});
		
		
		$(document).on('click', '#lychee-modal .picture', function() {
			parent.showPictureDetail($(this).attr('data-picture'));
		});

		$(document).on('click', '#lychee-picture .picture i', function() {
			$('#lychee-picture').hide();
			$('#lychee-modal .content').show();
		})

		$(document).on('click', '#lychee-picture .star', function() {
			parent.starPicture($(this).attr('data-id'));
		});

		$(document).on('click', '#lychee-picture .share', function() {
			parent.sharePicture($(this).attr('data-id'));
		});
		
		$('#lychee-file').change(function() {
			parent.uploadfiles(this.files);
		});
		
		$(document).on('click', '#upload', function(event) {
			parent.upload(event);
		});
	}

	

	this.onMessage = function(method, message) {
		if (method == 'refresh') {
			this.processData(message);
		} else if(method == 'getAlbum'){
			this.processAlbum(message);
		}else if (method == 'uploadPicture') {
			$('.lychee .progress').html('Upload');
			$('#upload').removeAttr('disabled', true);

			if (message) {
				showSuccessMessage('Pictures uploaded');
				$('#upload').remove();
				$('#lychee-modal .picture').remove();
				sendMessage(this.moduleId, 'getAlbum', this.currentAlbum.id);
			} else {
				showErrorMessage('Error while uploading pictures');
			}
		}
	}
	
	/// ALBUM METHODS
	this.processAlbum = function(album) {
		this.currentAlbum = album;
		$('#lychee-modal .fa-spinner').remove();
		var content = $('#lychee-modal .content');
		var html = [];
		html.push('<button id="upload" data-id="',album.id,'" class="btn btn-primary upload"><i class="fa fa-cloud-upload"></i> <span id="progress" class="progress">Upload</span></button>');
		html.push('<div class="picture-list">');
		this.shownPictures = album.pictures;
		$.each(album.pictures, function(index, picture) {
			html.push('<div class="picture" data-picture="', picture.id, '" style="background-image: url(\'', picture.thumbUrl, '\')">');
			html.push('</div>');
		});

		html.push('</div>');

		content.append(html.join(''));
	}
	
	

	
	this.showAlbum = function(album, title){
		$('#lychee-picture').hide();
		$('#lychee-modal .content').html('<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button><h3>'+title+'</h3><p><i class="fa fa-spinner fa-spin"></i></p>');
		$('#lychee-modal .content').show();
		$('#lychee-modal').modal('show');
		sendMessage(this.moduleId, 'getAlbum', album);
	}
	///// PICTURE METHODS

	this.processData = function(json) {
		var html = [];
		for(i = 0; i< json.length; i++){
			var album = json[i];
			html.push('<div class="album col-md-2" data-title="',album.title,'" data-id="',album.id,'" style="background-image:url(\'',album.thumb0,'\')"><h4>',album.title,'</h4></div>');
		}
		
		$('#albums').html(html.join(''));
	}

	
	this.starPicture = function(pictureId) {
		sendMessage(this.moduleId, 'toggleStar', pictureId);
		var button = $('#lychee-picture .star');
		if (button.hasClass('is-star')) {
			this.shownPictures[this.selectedPictureIndex].star = true;
			button.removeClass('is-star');
			button.html('<i class="fa fa-star-o"></i>');
		} else {
			this.shownPictures[this.selectedPictureIndex].star = true;
			button.addClass('is-star');
			button.html('<i class="fa fa-star"></i>');
		}
	}

	this.sharePicture = function(pictureId) {
		sendMessage(this.moduleId, 'togglePublic', pictureId);
		var button = $('#lychee-picture .share');
		if (button.hasClass('is-public')) {
			button.removeClass('is-public');
			this.shownPictures[this.selectedPictureIndex].isPublic = false;
			$('#lychee-picture .picture-shared').slideUp();
		} else {
			this.shownPictures[this.selectedPictureIndex].isPublic = true;
			button.addClass('is-public');
			$('#lychee-picture .picture-shared').slideDown();
		}
	}

	this.showPictureDetail = function(pictureId) {
		var parent = this;
		$.each(this.shownPictures, function(index, picture) {
			if (picture.id == pictureId) {
				// showing the picture interface;
				parent.showingPicture = picture;
				parent.selectedPictureIndex = index;
				var id = '#lychee-picture';

				var html = [];
				html.push('<div class="picture" style="background-image: url(\'', picture.url, '\')"><i class="fa fa-times"></i></div>');
				html.push('<div class="picture-actions">');
				if (picture.isPublic) {
					html.push('<button class="btn share is-public" data-id="', picture.id, '"><i class="fa fa-share-alt"></i></button>');
				} else {
					html.push('<button class="btn share " data-id="', picture.id, '"><i class="fa fa-share-alt"></i></button>');
				}

				if (picture.star) {
					html.push('<button class="btn star is-star" data-id="', picture.id, '"><i class="fa fa-star"></i></button>');
				} else {
					html.push('<button class="btn star" data-id="', picture.id, '"><i class="fa fa-star-o"></i></button>');
				}

				html.push('</div>');
				html.push('<div class="picture-shared">');
				html.push('<p><label>Direct link: </label>');
				html.push('<input onclick="select()" type="text" value="', picture.url, '" readonly/></p>');
				html.push('<p><label>BBCode thumbnail: </label>');
				html.push('<input onclick="select()" type="text" value="[url=', picture.url, '][img]', picture.thumbUrl, '[/img][/url]" readonly/></p>');
				html.push('<p><label>BBCode: </label>');
				html.push('<input onclick="select()" type="text" value="[url=', picture.url, '][img]', picture.url, '[/img][/url]" readonly/></p>');
				html.push('</div>');

				var content = $('#lychee-modal .content');
				content.hide();
				$(id).html(html.join(''));

				if (picture.isPublic) {
					$('#lychee-picture .picture-shared').show();
				} else {
					$('#lychee-picture .picture-shared').hide();
				}

				$(id).show();
				$(id).addClass('animated fadeIn');
			}
		});
	}
	

	
	//UPLOAD METHODS
	this.upload = function(event) {
		$('#lychee-file').click();
	}

	this.uploadfiles = function(files) {
		
		if (files.length > 0) {
			for (i = 0; i < files.length; i++) {
				var file = files[i];
				console.log(file);
				if (!file.name.match(/\.(jpg|jpeg|png|gif)$/i)) {
					showErrorMessage("You can only upload images.");
					break;
				}
			}
			$('#upload').attr('disabled', true);
			$('#upload .progress').html('0%');
			
			sendFile(this.moduleId, 'uploadPicture', $('#upload').attr('data-id'), files, this.progressHandlingFunction, true);
		}
	}

	this.progressHandlingFunction = function(e) {
		if (e.lengthComputable) {
			var percent = Math.ceil((e.loaded / e.total) * 100);
			// console.log(percent)
			$('.lychee .progress').html(percent + '%');
			if (percent == 100) {
				$('.lychee .progress').html('Processing...');
			}
		}
	}
}