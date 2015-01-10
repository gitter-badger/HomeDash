$(document).ready(function() {
	showCurrentPage();
	$(document).on('click', '.page-icon', changePage);

	$(document).on('click', '.remove-page', removePage);

	$(document).on('click', '.edit-page', renamePage);

	$('#showPages').click(showPages);

	$('.add-page').click(addPage);
});

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
}

function showCurrentPage() {
	var oldPages = $('.page-container:not(.page-container[data=' + CURRENT_PAGE + '])');
	var newPage = $('.page-container[data=' + CURRENT_PAGE + ']');
	
	oldPages.removeClass('fadeInLeft');
	newPage.removeClass('fadeOutSmall');
	
	oldPages.addClass('fadeOutSmall');
	newPage.addClass('fadeInLeft');
	
	setTimeout(function(){
		oldPages.hide();
		newPage.show();
	},300);
	
	/*$('.page-container').hide();
	$('.page-container[data=' + CURRENT_PAGE + ']').show();
	 */
	$('.page-icon').removeClass('active');
	$('.page-icon[data=' + CURRENT_PAGE + ']').addClass('active');
}

function showPages() {
	 
	var pages = $('#pages');
	var showAnimation = 'bounceDown';
	var hideAnimation = 'bounceUp';
	if (!pages.hasClass(showAnimation)) {
		scrolltoTop();
		pages.show();
		pages.removeClass(hideAnimation);
		pages.addClass(showAnimation);
	} else {
		pages.removeClass(showAnimation);
		pages.addClass(hideAnimation);
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