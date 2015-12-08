<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	
	

  </head>
  <center>
  <body background = "images/bg.png">
  		<br><br><br><br>
    	<form method = "Post", action = "UploadImage", enctype="multipart/form-data">
 		    <div class="content">
 
 			<input type="file" name = "file1" accept="image/*" onchange="showMyImage(this)"/><br><br>
				<img id="preview" src="" alt=""><br>
			</div>
				<script type = "text/javascript">
			 		 
			 		function showMyImage(fileInput) {
        				var files = fileInput.files;
        				for (var i = 0; i < files.length; i++) {           
            				var file = files[i];
            				var imageType = /image.*/;     
            				if (!file.type.match(imageType)) {
                				continue;
            				}           
            				var img=document.getElementById("preview");            
            				img.file = file;    
            				var reader = new FileReader();
            				reader.onload = (function(aImg) { 
                				return function(e) { 
                    				aImg.src = e.target.result; 
                				}; 
            				})(img);
            				reader.readAsDataURL(file);
        				}    
    				}
			 	</script>
			<div class="footer">
    			<input type="submit" name="submit" value="Search" />
    		</div>
    	</form>
  </body>
  </center>
</html>
