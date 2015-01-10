$(document).ready(function() {

	var modules = $('.page-container');
	
	modules.packery({
		itemSelector : '.module',
		gutter :0,
		columnWidth: ".grid-sizer",
		rowHeight: ".grid-sizer"
	});
	
	modules.packery( 'bindUIDraggableEvents', $('.module') );
	modules.packery( 'on', 'layoutComplete', function( pckryInstance, laidOutItems ) {
		$.each(laidOutItems, function(index, item){
			console.log(item);
		});
	});
});