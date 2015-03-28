function lychee(moduleId) {

	this.moduleId = moduleId;
	this.inAnimations = [ 'slideInLeft', 'slideInUp', 'slideInRight', 'slideInDown', 'flipInX', 'flipInY', 'zoomIn' ];
	this.outAnimations = [ 'slideOutRight', 'slideOutUp', 'slideOutLeft', 'slideOutDown', 'flipOutX', 'flipOutY', 'zoomOut' ];
	this.recentPicture = [];
	this.showingPicture;
	this.documentReady = function() {

		var parent = this;
		$('#lychee' + this.moduleId + '-recent').click(function(event) {
			parent.showRecent(event)
		});
		$('#lychee' + this.moduleId + '-upload').click(function(event) {
			parent.upload(event);
		});

		$(document).on('click', '#lychee' + this.moduleId + '-modal .picture', function() {
			parent.showPictureDetail($(this).attr('data-picture'));
		});

		$(document).on('click', '#lychee' + this.moduleId + '-picture .picture i', function() {
			$('#lychee' + parent.moduleId + '-picture').hide();
			$('#lychee' + parent.moduleId + '-modal .content').show();
		})

		$(document).on('click', '#lychee' + this.moduleId + '-picture .star', function() {
			parent.starPicture($(this).attr('data-id'));
		});

		$(document).on('click', '#lychee' + this.moduleId + '-picture .share', function() {
			parent.sharePicture($(this).attr('data-id'));
		});

		$('#lychee' + this.moduleId + '-file').change(function() {
			parent.uploadfiles(this.files);
		});

		setInterval(function() {
			parent.switchAlbum();
		}, 6000);

	}

	this.showRecent = function(event) {
		$('#lychee' + this.moduleId + '-picture').hide();
		$('#lychee' + this.moduleId + '-modal .content').show();
		$('#lychee' + this.moduleId + '-modal').appendTo("body").modal('show');
		sendMessage(this.moduleId, 'getRecent', '');
	}

	this.upload = function(event) {
		$('#lychee' + this.moduleId + '-file').click();
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
			$('#lychee' + this.moduleId + '-upload').attr('disabled', true);
			$('#lychee' + this.moduleId + '-upload .progress').html('0%');
			sendFile(this.moduleId, 'uploadPicture', 'r', files, this.progressHandlingFunction);
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

	this.starPicture = function(pictureId) {
		sendMessage(this.moduleId, 'toggleStar', pictureId);
		var button = $('#lychee' + this.moduleId + '-picture .star');
		if (button.hasClass('is-star')) {
			button.removeClass('is-star');
			button.html('<i class="fa fa-star-o"></i>');
		} else {
			button.addClass('is-star');
			button.html('<i class="fa fa-star"></i>');
		}
	}

	this.sharePicture = function(pictureId) {
		sendMessage(this.moduleId, 'togglePublic', pictureId);
		var button = $('#lychee' + this.moduleId + '-picture .share');
		if (button.hasClass('is-public')) {
			button.removeClass('is-public');
			$('#lychee' + this.moduleId + '-picture .picture-shared').slideUp();
		} else {
			button.addClass('is-public');
			$('#lychee' + this.moduleId + '-picture .picture-shared').slideDown();
		}
	}

	this.showPictureDetail = function(pictureId) {
		var parent = this;
		$.each(this.recentPicture, function(index, picture) {
			if (picture.id == pictureId) {
				// showing the picture interface;
				parent.showingPicture = picture;

				var id = '#lychee' + parent.moduleId + '-picture';

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

				var content = $('#lychee' + parent.moduleId + '-modal .content');
				content.hide();
				$(id).html(html.join(''));

				if (picture.isPublic) {
					$('#lychee' + parent.moduleId + '-picture .picture-shared').show();
				} else {
					$('#lychee' + parent.moduleId + '-picture .picture-shared').hide();
				}

				$(id).show();
				$(id).addClass('animated fadeIn');
			}
		});
	}

	this.onMessage = function(method, message) {
		if (method == 'refresh') {
			this.processData(message);
		} else if (method == 'getRecent') {
			this.processRecent(message);
		} else if (method == 'uploadPicture') {
			$('.lychee .progress').html('Upload');
			$('#lychee' + this.moduleId + '-upload').removeAttr('disabled', true);

			if (message) {
				showSuccessMessage('Pictures uploaded');
				this.showRecent(null);
			} else {
				showErrorMessage('Error while uploading pictures');
			}
		}
	}

	this.processData = function(json) {
		this.albums2html(json);
	}

	this.albums2html = function(json) {
		var albumsDiv = $('#lychee' + this.moduleId + '-albums');
		albumsDiv.html('');
		albumsDiv.append('<div class="lychee-album album-count animated">' + json.count + ' <span>Albums</span></div>');
		$.each(json.thumbs, function(index, thumb) {

			var html = '<div class="lychee-album animated" style="background-image: url(\'' + thumb + '\')"></div>';
			albumsDiv.append(html);

		});

		var parent = this;
		this.switchAlbum();

	}

	this.processRecent = function(album) {
		var content = $('#lychee' + this.moduleId + '-modal .content');
		var html = [];
		html.push('<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>');
		html.push('<h3>Recent pictures</h3>');
		html.push('<div class="picture-list">');
		this.recentPicture = album.pictures;
		$.each(album.pictures, function(index, picture) {
			html.push('<div class="picture" data-picture="', picture.id, '" style="background-image: url(\'', picture.thumbUrl, '\')">');
			html.push('</div>');
		});

		html.push('</div>');

		content.html(html.join(''));
	}

	this.switchAlbum = function() {
		var oldAlbum = $("#lychee" + this.moduleId + '-albums .lychee-album.active');

		var allAlbums = $("#lychee" + this.moduleId + '-albums .lychee-album');
		var index = Math.floor((Math.random() * allAlbums.length));
		// var index = 0;

		var animation = Math.floor((Math.random() * this.inAnimations.length));

		var newAlbum = $(allAlbums[index]);

		oldAlbum.addClass(this.outAnimations[animation]);
		newAlbum.addClass(this.inAnimations[animation] + ' active');

		var parent = this;
		setTimeout(function() {
			oldAlbum.removeClass(parent.outAnimations[animation]);
			oldAlbum.removeClass('active');
			newAlbum.removeClass(parent.inAnimations[animation]);

		}, 600);

	}
}