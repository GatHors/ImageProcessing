<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String imgsource = (String)request.getSession().getAttribute("uid");
//out.println(imgsource);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>Results</title>
		<meta charset="utf-8" />
		<meta name="viewport" content="width=device-width, initial-scale=1" />
		<meta http-equiv="pragma" content="no-cache"/>
		<meta http-equiv="cache-control" content="no-cache"/>
		<meta http-equiv="expires" content="0"/>
		<!--[if lte IE 8]><script src="assets/js/ie/html5shiv.js"></script><![endif]-->
		<link rel="stylesheet" href="assets/css/main.css" />
		<!--[if lte IE 8]><link rel="stylesheet" href="assets/css/ie8.css" /><![endif]-->
		<!--[if lte IE 9]><link rel="stylesheet" href="assets/css/ie9.css" /><![endif]-->
		<noscript><link rel="stylesheet" href="assets/css/noscript.css" /></noscript>
	</head>
	<body class="is-loading-0 is-loading-1 is-loading-2">

		<!-- Main -->
			<div id="main">
				
				<!-- Header -->
					<header id="header">
						<h1>gatHors</h1>
						<p>Click here to go back <a href="upload.jsp">Back to upload</a></p>

					</header>

				<!-- show the results -->

					<section id="thumbnails">
						
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/01.jpg"%>" data-position="left center">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/01.jpg"%>" alt="" /></a>
						</article>
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/02.jpg"%>">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/02.jpg"%>" alt="" /></a>
						</article>
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/03.jpg"%>" data-position="top center">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/03.jpg"%>" alt="" /></a>
						</article>
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/04.jpg"%>">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/04.jpg"%>" alt="" /></a>
						</article>
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/05.jpg"%>" data-position="top center">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/05.jpg"%>" alt="" /></a>
						</article>
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/06.jpg"%>">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/06.jpg"%>" alt="" /></a>
						</article>
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/07.jpg"%>">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/07.jpg"%>" alt="" /></a>
						</article>
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/08.jpg"%>">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/08.jpg"%>" alt="" /></a>
						</article>
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/09.jpg"%>">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/09.jpg"%>" alt="" /></a>
						</article>
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/10.jpg"%>">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/10.jpg"%>" alt="" /></a>
						</article>
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/11.jpg"%>">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/11.jpg"%>" alt="" /></a>
						</article>
						<article>
							<a class="thumbnail" href="images/<%= (String)request.getSession().getAttribute("uid") + "/12.jpg"%>">
							<img src="images/<%= (String)request.getSession().getAttribute("uid") + "/12.jpg"%>" alt="" /></a>
						</article>
					</section>

			</div>

		<!-- Scripts -->
			<script src="assets/js/jquery.min.js"></script>
			<script src="assets/js/skel.min.js"></script>
			<!--[if lte IE 8]><script src="assets/js/ie/respond.min.js"></script><![endif]-->
			<script src="assets/js/main.js"></script>

	</body>

</html>
