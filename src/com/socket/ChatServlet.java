package com.socket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import utils.MessageUtil;



@ServerEndpoint(value = "/websocket",configurator=GetHttpSessionConfigurator.class)
public class ChatServlet {


	private static final Map<HttpSession,ChatServlet> onlineUsers = new HashMap<HttpSession, ChatServlet>();

	private static int onlineCount = 0;

	private HttpSession httpSession;

	private Session session;


	@OnOpen
	public void onOpen(Session session,EndpointConfig config){

		this.session = session;
		this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		if(httpSession.getAttribute("user") != null){
			onlineUsers.put(httpSession, this);
		}
		String names = getNames();
		String content = MessageUtil.sendContent(MessageUtil.USER,names);
		broadcastAll(content);
		addOnlineCount();           //��������1
		System.out.println("�������Ӽ���!��ǰ��������Ϊ" + onlineUsers.size());
	}

	@OnClose
	public void onClose(){
		onlineUsers.remove(this);  //��set��ɾ��
		subOnlineCount();           //��������1   
		System.out.println("��һ���ӹرգ���ǰ��������Ϊ" + getOnlineCount());
	}

	@OnMessage
	public void onMessage(String message, Session session) throws IOException {

		HashMap<String,String> messageMap = MessageUtil.getMessage(message);    //������Ϣ��
		String fromName = messageMap.get("fromName");    //��Ϣ������ ��userId
		String toName = messageMap.get("toName");       //��Ϣ�����˵� userId
		String mapContent = messageMap.get("content");

		
		if(toName.isEmpty()){
			sendOffLine(fromName,toName);
			return;
		}

		if("all".equals(toName)){
			String msgContentString = fromName + "��������˵: " + mapContent;   //���췢�͵���Ϣ
			String content = MessageUtil.sendContent(MessageUtil.MESSAGE,msgContentString);
			broadcastAll(content);
		}else{
			try {
				String content = MessageUtil.sendContent(MessageUtil.MESSAGE,mapContent);
				singleChat(fromName,toName,content);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		System.out.println("���Կͻ��˵���Ϣ:" + message);
		broadcastAll(message);
	}

	private void singleChat(String fromName, String toName, String mapContent) throws IOException {
		String msgContentString = fromName + "��" + toName + "˵: " + mapContent;
		String contentTemp = MessageUtil.sendContent(MessageUtil.MESSAGE,msgContentString);
		boolean isExit = false;
		for (HttpSession key : onlineUsers.keySet()) {
			if(key.getAttribute("user").equals(toName)){
				isExit = true;
			}
		}
		if(isExit){
			for (HttpSession key : onlineUsers.keySet()) {
				if(key.getAttribute("user").equals(fromName) || key.getAttribute("user").equals(toName)){
					onlineUsers.get(key).session.getBasicRemote().sendText(contentTemp);
				}
			}
		}else{
			String content = MessageUtil.sendContent(MessageUtil.MESSAGE,"�ͷ�������������...");
			broadcastAll(content);
		}

	}
	private void sendOffLine(String fromName, String toName) throws IOException {
		String msgContentString = toName + "������";
		String content = MessageUtil.sendContent(MessageUtil.MESSAGE,msgContentString);
		for (HttpSession key : onlineUsers.keySet()) {
			if(key.getAttribute("user").equals(fromName) || key.getAttribute("user").equals(toName)){
				onlineUsers.get(key).session.getBasicRemote().sendText(content);
			}
		}
	}
	private static void broadcastAll(String msg) {
		for (HttpSession key : onlineUsers.keySet()) {
			try {
				onlineUsers.get(key).session.getBasicRemote().sendText(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@OnError
	public void onError(Session session, Throwable error){
		System.out.println("��������");
		error.printStackTrace();
	}


	private String getNames() {
		String names = "";
		for (HttpSession key : onlineUsers.keySet()) {
			String name = (String) key.getAttribute("user");
			names += name + ",";
		}
		String namesTemp = names.substring(0,names.length()-1);
		return namesTemp;
	}

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		ChatServlet.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		ChatServlet.onlineCount--;
	}

}