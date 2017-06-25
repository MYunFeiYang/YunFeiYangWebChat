<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="css/main.css">
<title>YunFeiYang WebChat</title>
</head>
<!-- 百度富文本编辑器 -->
<link href="umeditor/themes/default/css/umeditor.css" type="text/css" rel="stylesheet">
    <script type="text/javascript" src="umeditor/third-party/jquery.min.js"></script>
    <script type="text/javascript" src="umeditor/third-party/template.min.js"></script>
    <script type="text/javascript" charset="utf-8" src="umeditor/umeditor.config.js"></script>
    <script type="text/javascript" charset="utf-8" src="umeditor/umeditor.min.js"></script>
    <script type="text/javascript" src="umeditor/lang/zh-cn/zh-cn.js"></script>

<body onload="startWebSocket();">
	<h1>YunFeiYang WebChat</h1>
	<%
		String name = request.getParameter("username");
		session.setAttribute("user", name);
	%>
	<div id="container">
		<!-- 状态栏 -->
		<div id="nav">
			<h4>登录状态：</h4>
			<span id="denglu" style="color: red;">正在登录</span>
			<h4>昵称：</h4>
			<span id="userName"></span>
			<h4>To：</h4>
			<select id='userlist'>
			</select><span style="color: red;">*</span>
			<a href="login.html" id="loginbtu" onclick="loginBox()">登录</a>
		</div>
		<!-- 聊天框 -->
		<div id="chatbox">
			<h4>聊天框：</h4>
			<div id="message"></div>
		</div>
		<!-- 发送内容 -->
		<div id="content">
			<input type="button" value="send" onclick="sendMsg()" />
			<div id="chatinput">
				<input type="text" id="writeMsg"/>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(function() {
			// 初始化消息输入框
			var um = UM.getEditor('writeMsg');
		});
	</script>
</body>
<!-- chat js -->
<script type="text/javascript" src="js/jquery-3.2.1min.js"></script>
<script type="text/javascript" src="js/main.js"></script>
<script type="text/javascript">
var self = "<%=name%>";
	var ws = null;
	function startWebSocket() {
		if ('WebSocket' in window)
			ws = new WebSocket("ws://localhost:8080/YunFeiYangWebChat/websocket");
		else if ('MozWebSocket' in window)
			ws = new MozWebSocket("ws://localhost:8080/YunFeiYangWebChat/websocket");
		else
			alert("not support");
		ws.onmessage = function(evt) {
			var data = evt.data;
			var o = eval('(' + data + ')');//将字符串转换成JSON

			if (o.type == 'message') {
				setMessageInnerHTML(o.data);
			} else if (o.type == 'user') {
				var userArry = o.data.split(',');
				$("#userlist").empty();
				$("#userlist").append("<option value ='all'>所有人</option>");
				$.each(userArry, function(n, value) {
					if (value != self && value != 'admin') {
						$("#userlist").append(
								'<option value = '+value+'>' + value
										+ '</option>');
					}
				});
			}
		};
		ws.onclose = function(evt) {
			$('#denglu').html("离线");
		};

		ws.onopen = function(evt) {
			$('#denglu').html("在线");
			$('#userName').html(self);
		};
	}

	function setMessageInnerHTML(innerHTML) {
		var temp = $('#message').html();
		temp += innerHTML + '<br/>';
		$('#message').html(temp);
	}

	function sendMsg() {
		var fromName = self;
		var toName = $("#userlist").val(); //发给谁
		var content = $("#writeMsg").val(); //发送内容
		var msg = fromName + "," + toName + "," + content;
		ws.send(msg);
	}
</script>
</html>