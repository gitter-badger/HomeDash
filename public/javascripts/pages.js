$(document).ready(function(){
	showCurrentPage();
	$(document).on('click touchend', '.page-icon', changePage);

	
	$(document).on('click touchend', '.remove-page', removePage);
	
	$(document).on('click touchend', '.edit-page', renamePage);

	$('#showPages').click(showPages);
	
	$('.add-page').click(addPage);
});


function changePage(){
	//alert('chaging to page '+$(this).attr('data'));
	CURRENT_PAGE = $(this).attr('data');
	if(typeof(Storage) !== "undefined") {
		var page = localStorage.setItem("page", CURRENT_PAGE);
	}
	
	var wsMsg = new WebsocketMessage();
	wsMsg.message = CURRENT_PAGE;
	wsMsg.method = "changePage";
	
	ws.send(JSON.stringify(wsMsg));
	
	showCurrentPage();
}

function showCurrentPage(){
	$('.page-container').hide();
	$('.page-container[data='+CURRENT_PAGE+']').show();
	$('.page-icon').removeClass('active');
	$('.page-icon[data='+CURRENT_PAGE+']').addClass('active');
}

function showPages(){
	$('#pages').slideToggle();
}

function addPage(){
	$.get('/pages/add');
}

function removePage(){
	if(confirm('Removing this screen will delete all the modules from it. Continue ?')){
		$.get('/pages/remove/'+$(this).attr('data'));
	}
}

function renamePage(){
	var name = prompt('New screen name');
	if(name != ""){
		$.get('/pages/rename/'+$(this).attr('data')+'/'+name);
	}
}