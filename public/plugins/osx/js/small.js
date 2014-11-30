function osx(moduleId) {
	this.moduleId = moduleId;
	this.selectedApp;
	this.documentReady = function() {
		var parent = this;
		$(document).on('click', '.osx-app', function(event) {
			parent.onAppClick($(this));
		});

		$(document).on('click', '#osx' + this.moduleId + '-activate',
				function(event) {
					parent.activateApp($(this));
				});

		$(document).on('click', '#osx' + this.moduleId + '-quit',
				function(event) {
					parent.quitApp($(this));
				});
	}

	this.onMessage = function(method, message) {
		if (method == 'refresh') {
			this.showApps(message);
		}
	}

	this.showApps = function(message) {

		var list = $('#osx' + this.moduleId + '-apps');
		var parent = this;
		list.html("");
		$.each(message, function(app, info) {
			list.append(parent.app2Html(info));
		});
	}

	this.app2Html = function(info) {
		var html = [];
		html.push('<li>');
		if (info.running) {
			html.push('<button class="osx-app running" data-name="' + info.name
					+ '">');
		} else {
			html.push('<button class="osx-app" data-name="' + info.name + '">');
		}
		html.push('<div class="osx-app-icon" style="background-image: url(\'',
				info.pngPath, '\');"></div>');
		html.push('<p>');

		if (info.running) {
			html.push('<strong>', info.name, '</strong>');
		} else {
			html.push(info.name);
		}

		html.push('</p>');
		html.push('</button>');
		html.push('</li>');

		return html.join('');
	}

	this.onAppClick = function(button) {
		this.selectedApp = button.attr('data-name');
		if (button.hasClass('running')) {
			$('#osx' + this.moduleId + '-modal .modal-title').html(
					this.selectedApp);
			$('#osx' + this.moduleId + '-modal').modal('show');
		} else {
			sendMessage(this.moduleId, 'startApp', this.selectedApp);
		}
	}

	this.activateApp = function(button) {
		sendMessage(this.moduleId, 'activateApp', this.selectedApp);
		$('#osx' + this.moduleId + '-modal').modal('hide');
	}

	this.quitApp = function(button) {
		sendMessage(this.moduleId, 'quitApp', this.selectedApp);
		$('#osx' + this.moduleId + '-modal').modal('hide');
	}

}