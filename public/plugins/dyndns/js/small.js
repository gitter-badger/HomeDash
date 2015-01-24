function dyndns(moduleId) {
	this.moduleId = moduleId;

	this.documentReady = function() {
		var parent = this;
		$(document).on('change', '#ddns-' + this.moduleId + '-provider-type', function() {
			parent.getFields($(this).val());
		});

		$('#ddns-' + this.moduleId + '-add').click(function() {
			$('#ddns-'+parent.moduleId+'-provider-type').val(-1);
			$('#ddns-'+parent.moduleId+'-provider-type').change();
			$('#ddns-' + parent.moduleId + '-add-modal').appendTo('body').modal('show');
		});

		$(document).on('submit', '#ddns-' + this.moduleId + '-add-form', this.submitProvider);

		$(document).on('click', '#ddns-' + this.moduleId + '-submit', function() {
			parent.submitProvider();
		})
		
		$(document).on('click', '.dyndns .delete-provider', function(){
			sendMessage(parent.moduleId, 'deleteProvider', $(this).attr('data'));
		});
		
		$('#ddns-' + this.moduleId + '-force-refresh').click(function(){
			sendMessage(parent.moduleId, 'forceRefresh', '');
		});
	};

	this.onMessage = function(method, message, extra) {
		switch (method) {
		case 'refresh':
			this.processData(message);
			break;
		case 'getFields':
			this.processFields(message);
			break;
		case 'success':
		case 'error':
			this.processProviders(extra);
			break;
		}
	};

	this.processData = function(json) {
		$('#ddns-' + this.moduleId + '-ip').html(json.ip);
		$('#ddns-' + this.moduleId + '-update').html('Last update:'+json.lastUpdate);
		this.processProviders(json.providers);
	};

	this.processProviders = function(providers){
		var body = $('#ddns-' + this.moduleId + '-providers tbody');
		body.html('');
		var parent = this;
		
		if(providers.length > 0){
		
		$.each(providers, function(index, provider) {
			body.append(parent.provider2html(provider));
		});
		}else{
			body.append('<tr><td colspan="3">No providers available.</td></tr>');
		}
	}
	
	this.processFields = function(json) {
		console.log(json);

		var html = [];

		var parent = this;
		$.each(json, function(index, input) {
			switch (input.type) {
			case 0:
				html.push(parent.inputText2html(input));
				break;
			case 1:
				html.push(parent.inputPassword2html(input));
				break;
			}
		});

		console.log($('#ddns-' + this.modueId + '-fields'));
		$('#ddns-' + this.moduleId + '-fields').html(html.join(''));
	};

	this.inputPassword2html = function(input) {
		var html = [];
		html.push('<div class="form-group">');
		html.push('<label for="ddns-', this.moduleId, '-input-', input.name, '">', input.label, '</label>');
		html.push('<input type="password" class="form-control" id="ddns-', this.moduleId, '-input-', input.name, '" name="', input.name, '">');
		html.push('</div>');

		return html.join('');
	};

	this.inputText2html = function(input) {
		var html = [];
		html.push('<div class="form-group">');
		html.push('<label for="ddns-', this.moduleId, '-input-', input.name, '">', input.label, '</label>');
		html.push('<input type="text" class="form-control" id="ddns-', this.moduleId, '-input-', input.name, '" name="', input.name, '">');
		html.push('</div>');
		return html.join('');
	}

	this.getFields = function(provider) {
		if (provider != -1) {
			sendMessage(this.moduleId, 'getFields', provider);
		} else {
			$('#ddns-' + this.moduleId + '-fields').html('');
		}
	};

	this.submitProvider = function() {
		var data = $('#ddns-' + this.moduleId + '-add-form').serializeArray();
		sendMessage(this.moduleId, 'addProvider', data);
		$('#ddns-' + this.moduleId + '-add-modal').modal('hide');
		return false;
	}

	this.provider2html = function(provider) {
		var html = [];
		html.push('<tr>');
		html.push('<td>', provider.name, '</td>');
		html.push('<td>', provider.hostname, '</td>');
		html.push('<td>');
		//html.push('<a class="btn btn-sm btn-primary edit-provider" data="', provider.id, '">edit</a>');
		html.push('<a class="btn btn-sm btn-danger delete-provider" data="', provider.id, '">delete</a>');
		html.push('</td>');

		html.push('</tr>');
			
		return html.join('');
	}
}