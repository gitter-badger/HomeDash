$(document).ready(function() {
	showCurrentPage();
	$(document).on('click touchend', '.page-icon', changePage);

	$(document).on('click', '.remove-page', removePage);

	$(document).on('click', '.edit-page', renamePage);

	$('#showPages').click(showPages);

	$('.add-page').click(addPage);
});

var IN_ANIMATION = 'fadeIn';
var OUT_ANIMATION = 'fadeOut';

function changePage() {
	// alert('chaging to page '+$(this).attr('data'));
	CURRENT_PAGE = $(this).attr('data');
	if (typeof (Storage) !== "undefined") {
		var page = localStorage.setItem("page", CURRENT_PAGE);
	}

	var wsMsg = new WebsocketMessage();
	wsMsg.message = CURRENT_PAGE;
	wsMsg.method = "changePage";

	ws.send(JSON.stringify(wsMsg));

	showCurrentPage();
	showPages();
	fireCallbacks(CURRENT_PAGE);
}

function showCurrentPage() {
	var oldPages = $('.page-container:not(.page-container[data=' + CURRENT_PAGE + '])');
	var newPage = $('.page-container[data=' + CURRENT_PAGE + ']');
	
	oldPages.removeClass('animated '+IN_ANIMATION);
	newPage.removeClass('animated '+OUT_ANIMATION);
	
	oldPages.addClass('animated '+OUT_ANIMATION);
	newPage.addClass('animated '+IN_ANIMATION);
	
	setTimeout(function(){
		oldPages.hide();
		newPage.show();
	},300);
	
	/*
	 * $('.page-container').hide(); $('.page-container[data=' + CURRENT_PAGE +
	 * ']').show();
	 */
	$('.page-icon').removeClass('active');
	$('.page-icon[data=' + CURRENT_PAGE + ']').addClass('active');
}

function fireCallbacks(page){
	var selector = '.page-container[data='+page+'] .module';
	
	$(selector).each(function(index, module){
		var moduleId = $(module).attr('data');
		if(modules[moduleId].onPageChange != undefined){
			modules[moduleId].onPageChange(page);
		}
	});
	
}



function PageChangeCallback() {
	this.page = -1;
	this.moduleId = "";
	this.func = "";
}

function showPages() {
	 
	var pages = $('#pages');
	var showAnimation = 'bounceInDown';
	var hideAnimation = 'bounceOutUp';
	if (!pages.hasClass(showAnimation)) {
		scrolltoTop();
		pages.show();
		pages.removeClass('animated '+hideAnimation);
		pages.addClass('animated '+showAnimation);
	} else {
		pages.removeClass('animated '+showAnimation);
		pages.addClass('animated '+hideAnimation);
	}
}

function addPage() {
	$.get('/pages/add');
}

function removePage() {
	if (confirm('Removing this screen will delete all the modules from it. Continue ?')) {
		$.get('/pages/remove/' + $(this).attr('data'));
	}
}

function renamePage() {
	var name = prompt('New screen name');
	if (name != "") {
		$.get('/pages/rename/' + $(this).attr('data') + '/' + name);
	}
}


function scrolltoTop(){
	$('html,body').animate({
        scrollTop: 0
     }, 250);
}