
$(document).ready(function(){
		checkBoxesDependencies();
	
		$('input[type=checkbox]').click(updateCheckbox);
		
		$('#test-notifications').click(testNotifications);
		$('#generate-api').click(generateAPI);
});

function updateCheckbox(){
	console.log($(this));
	displayCheckBoxDependencies($(this));
}

function checkBoxesDependencies(){
	
	$('input[type=checkbox]').each(function(index, checkbox){
		var cb = $(checkbox);
		displayCheckBoxDependencies(cb);
	});
	
}

function displayCheckBoxDependencies(checkbox){
	var cb = checkbox[0]
	if(checkbox.attr('data-dep') != undefined){
		console.log('Has dependency');
		if(cb.checked == true){
			console.log('checked');
			$('.'+checkbox.attr('data-dep')).slideDown("fast");
		}else{
			console.log('unchecked');
			$('.'+checkbox.attr('data-dep')).slideUp("fast");
		}
	}
}

function testNotifications(){
	$.get('/globalSettings/testNotifications');
	return false;
}

function generateAPI(){
	$.get('/globalSettings/generateAPIKey', function(result){
		$('#api_key').val(result);
	});
}