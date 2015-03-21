$(document).ready(function() {
	$('#instance-info').submit(exploreInstance);

	$(document).on('click', '.addRemote', addRemoteModule);

	$(document).on('click', '#add-favorite', addFavorite);
	$(document).on('click', '#remove-favorite', removeFavorite);

	$('.explore-favorite').click(exploreFavorite);
})

function addRemoteModule(event) {
	var button = $(this);

	$('#module-info input[name=url]').val($('#url').val());
	$('#module-info input[name=api_key]').val($('#api_key').val());
	$('#module-info input[name=class]').val(button.attr('data-class'));
	$('#module-info input[name=id]').val(button.attr('data-id'));

	$('#module-info').submit()
}

function exploreFavorite(event) {
	var button = $(this);
	$('#url').val(button.attr('data-url'));
	$('#api_key').val(button.attr('data-apikey'));

	exploreInstance();
}

function removeFavorite(event) {
	var fav = $(this);
	var id = fav.attr('data-id');
	var encodedUrl = fav.attr('data-url');
	var name = fav.attr('data-name');
	var apikey = fav.attr('data-apikey');
	$.get('/favorite/remove/' + id, function(result) {
		if (result == "1") {

			$('#fav-' + id).remove();

			$.get('/favorite/is/' + encodedUrl, function(result) {
				var html = '';
				if (result != '-1') {
					html = '<a id="remove-favorite" data-name="' + name + '" data-apikey="' + apikey + '" data-url="' + encodedUrl + '" data-id="' + result + '"><i class="fa fa-star"></i></a>';
				} else {
					html = '<a id="add-favorite" data-name="' + name + '" data-apikey="' + apikey + '" data-url="' + encodedUrl + '"><i class="fa fa-star-o"></i></a>';
				}

				$('#is-favorite').html(html);
			})
		}
	});

}

function addFavorite(event) {
	var fav = $(this);
	var encodedUrl = fav.attr('data-url');
	var name = fav.attr('data-name');
	var apikey = fav.attr('data-apikey');
	$.get('/favorite/add/' + name + '/' + encodedUrl + '/' + apikey, function(result) {

		$.get('/favorite/is/' + encodedUrl, function(result) {
			var html = '';
			if (result != '-1') {
				html = '<a id="remove-favorite" data-name="' + name + '" data-apikey="' + apikey + '" data-url="' + encodedUrl + '" data-id="' + result + '"><i class="fa fa-star"></i></a>';
			} else {
				html = '<a id="add-favorite" data-name="' + name + '" data-apikey="' + apikey + '" data-url="' + encodedUrl + '"><i class="fa fa-star-o"></i></a>';
			}

			$('#is-favorite').html(html);
		})
	});
};

function exploreInstance() {
	var url = $('#url').val() + '/api/explore';
	$('#results').html('');
	$.ajax({

		url : '/exploreRemoteHost',
		data : {
			api_key : $('#api_key').val(),
			url : url
		},
		type : 'post',
		crossDomain : true,
		success : function(result) {
			populateModules(JSON.parse(result));
		},
		error : function() {
			showErrorMessage("Can't get modules from " + $('#url').val());
		},
	});

	return false;
}

function populateModules(json) {
	console.log(json);

	$('#module-info input[name=name]').val(json.deviceName);

	var html = [];

	html.push('<h2>', json.deviceName, '\'s modules <span id="is-favorite"</span></h2>');

	$.each(json.modules, function(index, module) {
		html.push('<div class="row"><div class="card col-md-12">');
		html.push('<h3>', module.moduleName, '</h3>');
		if (module.exposedSettings != undefined) {
			$.each(module.exposedSettings, function(index, value) {
				html.push('<p><strong>', index, '</strong>: ', value, '</p>');
			});
		}

		html.push('<p>', module.description, '</p>');
		html.push('<p><a class="internal-link addRemote" data-class="', module.pluginId, '" data-id="', module.id, '"><button class="btn btn-success">Add ', module.moduleName, '</button></a></p>');
		html.push('</div></div>');
	});

	var url = $('#url').val();
	var encodedUrl = encodeURIComponent(url);
	var name = encodeURIComponent(json.deviceName);
	var apikey = encodeURIComponent($('#api_key').val());

	$.get('/favorite/is/' + encodeURIComponent(url), function(result) {
		var html = '';
		if (result != '-1') {
			html = '<a id="remove-favorite" data-name="' + name + '" data-apikey="' + apikey + '" data-url="' + encodedUrl + '" data-id="' + result + '"><i class="fa fa-star"></i></a>';
		} else {
			html = '<a id="add-favorite" data-name="' + name + '" data-apikey="' + apikey + '" data-url="' + encodedUrl + '"><i class="fa fa-star-o"></i></a>';
		}

		$('#is-favorite').html(html);
	})

	$('#results').html(html.join(''));
}

function showErrorMessage(message) {
	var box = $("#message-box");
	box.html(message);
	box.removeClass("success");
	box.addClass('error');
	box.slideDown();

	setInterval(function() {
		box.slideUp()
	}, 3000);
}
