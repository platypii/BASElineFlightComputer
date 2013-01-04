var express = require('express');
var format = require('util').format;
var fs = require('fs');
var spawn = require('child_process').spawn;

var app = module.exports = express()

// bodyParser in connect 2.x uses node-formidable to parse multipart form data.
app.use(express.bodyParser())

// Static files
app.use(express.static(__dirname + '/public'));

// Routes
app.get('/jump/:id', renderJump);
app.get('/upload', renderUploadForm);
app.post('/upload', handleUpload);
app.get('/', function(req,res) {
  res.sendfile('public/index.html');
});

// Renders the jump
function renderJump(req, res) {
    var id = req.params.id;
    var html = '<html><body>';
    html += '<img src="/plot/'+id+'.png">';
    html += '</body></html>';
    res.send(html);
}


// Handles the receiving of CSV files
function handleUpload(req, res) {

    console.log(req);

    // Check upload
    var upload = req.files.csvfile;
    if(upload && upload.name.search(/\.csv$/) != -1) {
	var id = req.body.id;

	//var report = 'Received ' + upload.name + ' (' + upload.size + ')';
	var report = {status: 'ok', id: id, file: upload.name, filesize: upload.size};
	res.send(report);
	console.log(report);

	// Move file
	var tmpFile = upload.path;
	var uploadFile = './public/csv/' + id + '.csv';

	//fs.copy(source, dest, function(error){if(error) throw error;});
	spawn('cp', [tmpFile, uploadFile]);

	var plotFile = './public/plot/' + id + '.png';
	console.log('Plotting ' + uploadFile + ' -> ' + plotFile);
	spawn('bash', ['plot.sh', uploadFile, plotFile]);

    } else {
	res.send('error: not a CSV file.');
	console.log('warning: received non-csv file');
    }
}


// Renders a form allowing file upload. // TODO: Testing only.
function renderUploadForm(req, res) {
    var html = '<html><body>';
    html += '<form action="/upload" method="post" enctype="multipart/form-data">';
    html += '<p>ID: <input type="text" name="id" /></p>';
    html += '<p>CSV: <input type="file" name="csvfile" /></p>';
    html += '<p><input type="submit" value="Upload" /></p>';
    html += '</form>';
    html += '</body></html>';
    res.send(html);
}


// Launch server
if(!module.parent) {
    app.listen(12000);
    console.log('BASEline Web Services started on port 12000');
}




