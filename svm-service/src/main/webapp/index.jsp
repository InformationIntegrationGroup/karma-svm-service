<html>
<head>
	<title>SVM Testing app</title>
	<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.2.6/jquery.min.js" type="text/javascript"></script>
	
	<style>
		.outer_div {
			margin-top: 30px;
			margin-left: 20px;
		}
	</style>
	<script type="text/javascript">
		function sendData() {
			$.ajax({
				url : "/svm-service/api/svm/test",
				type: "POST",
				data: $('#txtData').val(),
				success : function(data) {
					
					
					$('#resultArea').html("test");
					
				}
				
			});
		}
	</script>
</head>
<body>
<div class="outer_div">
    <h2>SVM Testing app</h2>
    
    <textarea rows="50" cols="100" id="txtData"></textarea>
    <br /> <input type="button" value="Submit" onclick="sendData()" />
    
    <div id="resultArea"></div>
</div>

<br /><br /><br />
<form action="/svm-service/svm/upload_svm?kernel=linear" method="post" enctype="multipart/form-data">
	<label for="file">Filename:</label>
	<input type="file" name="file" id="file"><br>
	<input type="submit" name="submit" value="Submit">
</form>
</body>
</html>
