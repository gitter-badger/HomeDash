function systeminfo(moduleId) {
	this.moduleId = moduleId;
	this.torrents = [];
	this.selectedTorrent = null;
	this.cpuChart = null;
	this.ramChart = null;
	this.cpuData = null;
	this.ramData = null;
	this.count = 0;
	this.options = {
		width : '100%',
		height : 200,
		hAxis : {
			title : 'Time',
			textPosition: 'none',
			gridlines: {
				count: 0
			}
		},
		vAxis : {
			title : 'Usage (%)',
			minValue : 0,
			maxValue : 100,
			textPosition: 'none'
		},
		axisTitlesPosition: 'none',
		legend:{
			position: 'none'
		}
	};
	this.documentReady = function() {
		google.load('visualization', '1', {
			packages : [ 'corechart' ]
		});
		google.setOnLoadCallback(drawChart);

		var parent = this;
		
		function drawChart() {

			parent.cpuData = new google.visualization.DataTable();
			parent.cpuData.addColumn('number', 'X');
			parent.cpuData.addColumn('number', 'Load');

			parent.cpuChart = new google.visualization.AreaChart(document
					.getElementById('cpu-info'));

			parent.cpuChart.draw(parent.cpuData, parent.options);

			parent.ramData = new google.visualization.DataTable();
			parent.ramData.addColumn('number', 'X');
			parent.ramData.addColumn('number', 'Load');

			parent.ramChart = new google.visualization.AreaChart(document
					.getElementById('ram-info'));

			parent.ramChart.draw(parent.ramData, parent.options);

		}
	}

	this.initChart = function(json) {

		this.cpuData.addRows(json["cpu"]);
		this.ramData.addRows(json["ram"]);
		this.cpuChart.draw(this.cpuData, this.options);

		this.ramChart.draw(this.ramData, this.options);
	}

	this.onMessage = function(method, message) {
		if (method == 'refresh') {
			if (this.count == 0) {
				this.initChart(message);
			} else {
				this.processData(message);
			}

			this.count++;
		}
	}

	this.processData = function(json) {
		console.log(json);

		while(this.cpuData.getNumberOfRows() >= 100){
			this.cpuData.removeRow(0);
		}
		
		while(this.ramData.getNumberOfRows() >= 100){
			this.ramData.removeRow(0);
		}
		
		this.cpuData.addRows(json["cpu"]);
		this.ramData.addRows(json["ram"]);
		this.cpuChart.draw(this.cpuData, this.options);

		this.ramChart.draw(this.ramData, this.options);

	}

}