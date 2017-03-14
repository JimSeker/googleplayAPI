<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*"%>
<%@ page import="cs.dartmouth.edu.gcmdemo.backend.data.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Query Result</title>
</head>
<body>
	<%
		String retStr = (String) request.getAttribute("_retStr");
		if (retStr != null) {
	%>
	<%=retStr%><br>
	<%
		}
	%>

		---------------------------------------------------------------------<br>
	</b> send a new message:
	<br>
	<form name="input" action="/add.do" method="post">
		Name: <input type="text" name="name"> 
	  	      <input type="submit" value="Add">
	</form>
	---------------------------------------------------------------------

</body>
</html>
