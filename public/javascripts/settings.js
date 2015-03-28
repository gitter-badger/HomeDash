var currentOrder = [];
var sizes = [];
var windowWidth = window.innerWidth;
$(document).ready(function() {

	currentOrder = getOrder();

	$("#save-settings").click(function() {
		toggleSettings();
	});

	$(".overlay-settings, .setting-action").click(function(event) {
		var source = $(this);

		var fn = window[source.attr("action")];
		if (typeof fn === 'function') {
			fn(source.attr("data"));
		}
	});

	$("#showSettings").click(function() {
		toggleSettings();
	});

	$(document).on('click', ".move-destination", moveModule);

	/*
	 * if(is.desktop()){ if(window.innerWidth > 992){ gridster(); } }else
	 * if(is.mobile()){ mobileSort(); }else if(is.tablet()){
	 * if(window.innerHeight < window.innerWidth && window.innerWidth > 992){
	 * //landscape gridster() }else{ //portait or too narrow tabler width;
	 * mobileSort(); } }
	 */

	if (window.innerWidth > 992) {
		gridster();
	} else {
		mobileSort();
	}

	$(window).resize(function() {
		if (is.desktop()) {
			if (windowWidth > 992 && window.innerWidth <= 992) {
				location.reload();
			} else if (windowWidth <= 992 && window.innerWidth > 992) {
				location.reload();
			}
			windowWidth = window.innerWidth;
		}
	});
	window.addEventListener('orientationchange', function() {
		if (is.tablet()) {
			location.reload();
		}
	});

});

function gridster() {
	$('.module-container').each(function(index, element) {
		var gridster = $(element).gridster({
			widget_margins : [ 0, 0 ],
			widget_base_dimensions : [ 99, 99 ],
			widget_selector : '.module',
			min_cols : 10,
			max_cols : 10,
			draggable : {
				handle : '.sort-module',
				stop : afterDesktopSort
			}
		}).data('gridster');

		gridster.recalculate_faux_grid();
	});

}

function afterDesktopSort(event, ui) {
	var links = $('.module-setting-link');
	links.css('pointer-events', 'none');

	var orderStr = [];
	$(".module").each(function(index, object) {
		orderStr.push($(object).attr("data") + '-' + $(object).attr("data-col") + '-' + $(object).attr("data-row"));
	});

	var data = 'order=' + orderStr.join('|')
	$('.settings-loading').show();
	$('.settings-loading').addClass('fadeInUp');
	$('.settings-loading').removeClass('fadeOutDown');
	$.post('/saveDesktopOrder', data, function() {
		$('.settings-loading').removeClass('fadeInUp');
		$('.settings-loading').addClass('fadeOutDown');
		links.css('pointer-events', 'auto');
		reloadOthers();
	});
}

function mobileSort() {
	$('modules').addClass('col-md-12');
	$("#modules").sortable({
		// disabled: true,
		items : ".module",
		opacity : 0.5,
		handle : '.sort-module',
		stop : afterMobileSort
	});
}

function afterMobileSort(event, ui) {
	var links = $('.module-setting-link');
	links.css('pointer-events', 'none');

	var orderStr = [];
	$(".module").each(function(index, object) {
		orderStr.push($(object).attr("data") + '-' + index);
	});

	var data = 'order=' + orderStr.join('|')
	$('.settings-loading').show();
	$('.settings-loading').addClass('fadeInUp');
	$('.settings-loading').removeClass('fadeOutDown');
	$.post('/saveMobileOrder', data, function() {
		$('.settings-loading').removeClass('fadeInUp');
		$('.settings-loading').addClass('fadeOutDown');
		links.css('pointer-events', 'auto');
		reloadOthers();
	});
}

function toggleSettings() {
	var IN_ANIMATION = 'fadeIn';
	var OUT_ANIMATION = 'fadeOut';

	var overlay = $(".module-settings-overlay");
	var settings = $('.settings');
	var container = $('.module-container');
	if (settings.hasClass(IN_ANIMATION)) {
		settings.removeClass('animated ' + IN_ANIMATION);
		settings.addClass('animated ' + OUT_ANIMATION);
		overlay.removeClass('animated ' + IN_ANIMATION);
		overlay.addClass('animated ' + OUT_ANIMATION);
		container.removeClass('active');
		setTimeout(function() {
			overlay.hide();
		}, 500);
		$("#showSettings i").removeClass("fa-spin");

	} else {
		settings.removeClass('animated ' + OUT_ANIMATION);
		settings.addClass('animated ' + IN_ANIMATION);
		overlay.removeClass('animated ' + OUT_ANIMATION);
		overlay.addClass('animated ' + IN_ANIMATION);
		container.addClass('active');
		settings.show();
		overlay.show();
		$("#showSettings i").addClass("fa-spin");

	}
}

function getOrder() {
	var order = [];
	$.each($(".module"), function(index, object) {
		order[index] = $(object).attr("data")
	});

	return order
}

function move(moduleId) {
	$('.move-destination').attr('data-source', moduleId);
	$('#move-module').modal('show');
}

function moveModule() {
	var source = $(this).attr('data-source');
	var dest = $(this).attr('data');

	$.get('/module/' + source + '/move/' + dest);
}