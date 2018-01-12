<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONObject"%>
<%@page import="java.util.HashMap"%>
<link href="css/w3.css" rel="stylesheet" type="text/css" />
<%

JSONObject obj=(JSONObject)session.getAttribute("userinfo");
out.println(obj.toString());

%>

