var currentOrder = [];
var sizes = [];

$(document).ready(function() {

	$(".glyphicon-resize-small").click(shrink);
	$(".glyphicon-resize-full").click(expand);

	currentOrder = getOrder();

	$("#save-settings").click(function() {
		toggleSettings();
	});

	$(".overlay-settings").click(function(event) {
		var source = $(this);

		var fn = window[source.attr("action")];
		if (typeof fn === 'function') {
			fn(source.attr("data"));
		}
	});

	$("#showSettings").click(function() {
		toggleSettings();
	});

	$("#modules").sortable({
		// disabled: true,
		items : ".module",
		opacity : 0.5,
		handle : '.sort-module',
		stop : afterSort
	});
});

function afterSort(event, ui) {
	saveSettings(false);
}

function saveSettings(hideSettings) {
	var sizesStr = [];
	$('.bouncer').show();
	$.each($(".module"), function(index, object) {
		var item = $(object);

		var classes = item.attr("class");
		var split = classes.split(" ");

		var colSplit = split[1].split("-");

		var currentSize = colSplit[2];

		sizesStr.push(item.attr("data") + '-' + currentSize);
	});

	var orderStr = [];
	$.each($(".module"), function(index, object) {
		orderStr.push($(object).attr("data") + '-' + index);
	});

	$("#settings-form-order").val(orderStr.join('|'));
	$("#settings-form-sizes").val(sizesStr.join('|'));

	var data = $("#settings-form").serialize();

	$.post('/settings', data, function() {
		if (hideSettings) {
			showSuccessMessage('Settings saved !');
			toggleSettings();
		}
		// $(".settings").slideToggle("fast");
		// $(".module-settings-overlay").slideToggle("fast");
		$('.bouncer').hide();
	});
}

function toggleSettings() {
	var overlay = $(".module-settings-overlay");
	var settings = $('.settings');
	if (settings.hasClass('fadeInLeftNoDelay')) {
		settings.removeClass('fadeInLeftNoDelay');
		settings.addClass('fadeOutSmall');
		overlay.removeClass('fadeInLeftNoDelay');
		overlay.addClass('fadeOutSmall');
		setTimeout(function() {
			overlay.hide();
		}, 500);

	} else {
		settings.removeClass('fadeOutSmall');
		settings.addClass('fadeInLeftNoDelay');
		overlay.removeClass('fadeOutSmall');
		overlay.addClass('fadeInLeftNoDelay');
		settings.show();
		overlay.show();

	}
}

function getOrder() {
	var order = [];
	$.each($(".module"), function(index, object) {
		order[index] = $(object).attr("data")
	});

	return order
}

function shrink(moduleId) {

	var item = $("#module" + moduleId);

	var classes = item.attr("class");
	var split = classes.split(" ");

	var colSplit = split[1].split("-");

	var currentSize = colSplit[2];

	if (currentSize > 3) {
		var classToRemove = "col-md-" + currentSize;
		currentSize--;
		var classToAdd = "col-md-" + currentSize;
		item.switchClass(classToRemove, classToAdd, 400, 'swing', function() {
			sizes[moduleId] = currentSize;
			console.log(sizes[moduleId]);
			saveSettings(false);
		});
	}

}

function expand(moduleId) {

	var item = $("#module" + moduleId);

	var classes = item.attr("class");
	var split = classes.split(" ");

	var colSplit = split[1].split("-");

	var currentSize = colSplit[2];

	if (currentSize < 12) {
		var classToRemove = "col-md-" + currentSize;
		currentSize++;
		var classToAdd = "col-md-" + currentSize;
		item.switchClass(classToRemove, classToAdd, 400, 'swing', function() {
			sizes[moduleId] = currentSize;
			console.log(sizes[moduleId]);
			saveSettings(false);
		});
	}

}

function findBefore(moduleId) {
	var before = moduleId;
	for (i = 0; i < currentOrder.length; i++) {
		value = currentOrder[i];
		if (value != moduleId) {
			before = value;
		} else {
			break;
		}
	}

	return before;
}

function findAfter(moduleId) {
	var after = moduleId;
	for (i = currentOrder.length - 1; i >= 0; i--) {
		value = currentOrder[i];
		if (value != moduleId) {
			after = value;
		} else {
			break;
		}
	}

	return after;
}