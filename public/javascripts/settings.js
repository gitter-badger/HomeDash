var currentOrder = [];
var sizes = []
$(document).ready(function(){
	
	$(".glyphicon-chevron-up").click(moveUp);
	$(".glyphicon-chevron-down").click(moveDown);
	$(".glyphicon-resize-small").click(shrink);
	$(".glyphicon-resize-full").click(expand);
	
	currentOrder = getOrder();
	
	$("#save-settings").click(saveSettings);
	
});

function saveSettings(){
	var sizesStr = [];
	$.each($(".module"), function(index, object){
		var item = $(object);
		
		var classes = item.attr("class");
		var split = classes.split(" ");
		
		var colSplit = split[1].split("-");
		
		var currentSize = colSplit[2];
		
		sizesStr.push(item.attr("data")+'-'+currentSize);
	});
	
	var orderStr = [];
	$.each($(".module"), function(index, object){
		orderStr.push($(object).attr("data")+'-'+index);
	});
	
	
	$("#settings-form-order").val(orderStr.join('|'));
	$("#settings-form-sizes").val(sizesStr.join('|'));
	
	var data = $("#settings-form").serialize();
	
	$.post('/settings', data, function(){
		showSuccessMessage('Settings saved !');
		$(".settings").slideToggle("fast");
	});
}

function getOrder(){
	var order = [];
	$.each($(".module"), function(index, object){
		order[index] = $(object).attr("data")
	});
	
	return order
}

function moveUp(event){
	var moduleId = $(this).attr("data");
	var before = findBefore(moduleId);
	if(moduleId != before){
		$("#module"+before).before($("#module"+moduleId));
	}
	
	currentOrder = getOrder();
}


function moveDown(event){
	var moduleId = $(this).attr("data");
	var after = findAfter(moduleId);
	if(moduleId != after){
		$("#module"+after).after($("#module"+moduleId));
	}
	
	currentOrder = getOrder();
}

function shrink(event){
	var moduleId = $(this).attr("data");
	var item = $("#module"+moduleId);
	
	var classes = item.attr("class");
	var split = classes.split(" ");
	
	var colSplit = split[1].split("-");
	
	var currentSize = colSplit[2];

	if(currentSize > 3){
		item.removeClass("col-md-"+currentSize);
		currentSize--;
		item.addClass("col-md-"+currentSize);
	}
	
	sizes[moduleId] = currentSize;
	console.log(sizes[moduleId]);
}

function expand(event){
	var moduleId = $(this).attr("data");
	var item = $("#module"+moduleId);
	
	var classes = item.attr("class");
	var split = classes.split(" ");
	
	var colSplit = split[1].split("-");
	
	var currentSize = colSplit[2];

	if(currentSize < 12){
		item.removeClass("col-md-"+currentSize);
		currentSize++;
		item.addClass("col-md-"+currentSize);
	}
	
	sizes[moduleId] = currentSize;
	console.log(sizes[moduleId]);
}

function findBefore(moduleId){
	var before = moduleId;
	for(i = 0; i < currentOrder.length; i++){
		value = currentOrder[i];
		if(value != moduleId){
			before = value;
		}else{
			break;
		}
	}
	
	return before;
}

function findAfter(moduleId){
	var after = moduleId;
	for(i = currentOrder.length - 1; i >= 0; i--){
		value = currentOrder[i];
		if(value != moduleId){
			after = value;
		}else{
			break;
		}
	}
	
	return after;
}