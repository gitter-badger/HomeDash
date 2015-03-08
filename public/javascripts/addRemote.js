$(document).ready(function() {
	$('#instance-info').submit(exploreInstance);

	$(document).on('click', '.addRemote', addRemoteModule);
})

function addRemoteModule(event) {
	var button = $(this);

	$('#module-info input[name=url]').val($('#url').val());
	$('#module-info input[name=api_key]').val($('#api_key').val());
	$('#module-info input[name=class]').val(button.attr('data-class'));
	$('#module-info input[name=id]').val(button.attr('data-id'));

	$('#module-info').submit()
}

function exploreInstance() {
	var url = $('#url').val() + '/api/explore';
	$('#results').html('');
	$.ajax({

		url : '/exploreRemoteHost',
		data : {
			api_key : $('#api_key').val(),
			url: url
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

	html.push('<h2>', json.deviceName, '\'s modules</h2>');

	$.each(json.modules, function(index, module) {
		html.push('<div class="row"><div class="card col-md-12">');
		html.push('<h3>', module.moduleName, '</h3>');
		html.push('<p><a class="internal-link addRemote" data-class="',
				module.pluginId, '" data-id="', module.id,
				'"><button class="btn btn-success">Add ', module.moduleName,
				'</button></a></p>');
		html.push('</div></div>');
	});

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
