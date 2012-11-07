var express = require('express');
var format = require('util').format;
var fs = require('fs');
var spawn = require('child_process').spawn;

var app = module.exports = express()

// bodyParser in connect 2.x uses node-formidable to parse multipart form data.
app.use(express.bodyParser())

app.get('/', function(req, res) {
    res.send('<form method="post" enctype="multipart/form-data">'
	     + '<p>Title: <input type="text" name="title" /></p>'
	     + '<p>CSV: <input type="file" name="upload" /></p>'
	     + '<p><input type="submit" value="Upload" /></p>'
	     + '</form>');
});

app.post('/', function(req, res, next) {

    console.log(req.files);

    // the uploaded file can be found as `req.files.image` and the
    // title field as `req.body.title`
    res.send(format('\nuploaded %s (%d Kb) to %s as %s'
		    , req.files.upload.name
		    , req.files.upload.size / 1024 | 0 
		    , req.files.upload.path
		    , req.body.title));

    // Check upload
    if(req.files.upload.name.search(/\.csv/) != -1) {

	// Move file
	var tmpFile = req.files.upload.path;
	var uploadFile = './upload/' + req.files.upload.name;

	console.log('tmp: ' + tmpFile);
	console.log('upload: ' + uploadFile);

	//fs.copy(source, dest, function(error){if(error) throw error;});
	spawn('cp', [tmpFile, uploadFile]);

	var plotFile = './plots/' + req.files.upload.name.replace(/\.csv/, '.png');
	console.log('Plotting ' + uploadFile + ' ' + plotFile);
	spawn('bash', ['plot.sh', uploadFile, plotFile]);

    }

});

if(!module.parent) {
    app.listen(12000);
    console.log('BASEline Web Services started on port 12000');
}




