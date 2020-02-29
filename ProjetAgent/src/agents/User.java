package agents;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;

import jade.domain.FIPAAgentManagement.Property;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;

public class User extends GuiAgent{
	private static final int OP_NONE = 0;
	private static final int OP_SEND_FILE = 1;
	private static final int OP_SEND_REQUEST = 2;
	private boolean loggedIn = false;
	private boolean registered = false;
	private UserContainer gui;
	private UserAccount account = null;
	private Hashtable < String, AgentFileDescriptor > files = new Hashtable < String, AgentFileDescriptor >();
	private String tmpFileName;
	private int op = OP_NONE;
	public void setUserAccount(UserAccount account){
		this.account = account;
	}
	public UserAccount getUserAccount() { return account ;}
	@Override
	protected void setup(){
		gui = (UserContainer)getArguments()[0];
		gui.setUser(this);
		addBehaviour(new CyclicBehaviour() {			
			@Override
			public void action() {
				ACLMessage message = receive();
				if (message != null){
				   processMessage(message);
				}
				block();
			}
	  });
		gui.updateFileList(files);
	}
	@Override
	protected void takeDown(){
		if (loggedIn){
			sendDisconnectMessage();
		}
		try {
			if (registered == true)
			DFService.deregister(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void addFile(String filePath, String content){
		String fileName = Utils.getFileName(filePath);
		files.put(fileName, new AgentFileDescriptor(content, false));
		try {
			if (registered == true)
			DFService.deregister(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		for(Map.Entry < String, AgentFileDescriptor >  it : files.entrySet()){
			ServiceDescription sd = new ServiceDescription();
			sd.setType("file");
			sd.setName(it.getKey());
			Property property = new Property();
			property.setName("recommend");
			property.setValue(it.getValue().getRecommend());
			sd.addProperties(property);
			dfd.addServices(sd);
		}
		try{
			DFService.register(this, dfd);
			registered = true;
		} catch (FIPAException fe){
			fe.printStackTrace();
		}
		gui.updateFileList(files);
	}
	void recommendFile(String fileName){
		AgentFileDescriptor descriptor = files.get(fileName);
		if (descriptor == null) return;
		descriptor.setRecommend(true);
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		for(Map.Entry < String, AgentFileDescriptor >  it : files.entrySet()){
			ServiceDescription sd = new ServiceDescription();
			sd.setType("file");
			sd.setName(it.getKey());
			Property property = new Property();
			property.setName("recommend");
			property.setValue(it.getValue().getRecommend());
			sd.addProperties(property);
			dfd.addServices(sd);
		}
		try{
			DFService.register(this, dfd);
		} catch (FIPAException fe){
			fe.printStackTrace();
		}
		gui.updateFileList(files);
	}
	AID[] findFile(String file){
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setName(file);
		template.addServices(sd);
		try {
			DFAgentDescription[] results = DFService.search(this, template);
			AID[] agentsFound = new AID[results.length];
			for(int i = 0; i < results.length; i++){
				agentsFound[i] = results[i].getName();
			}
			return agentsFound;
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	void sendFile(String name, String content, AID id){
		ACLMessage fileContent = new ACLMessage(ACLMessage.INFORM);
		fileContent.setOntology("fileContent");
		fileContent.setContent(content);
		fileContent.addUserDefinedParameter("name", name);
		fileContent.addReceiver(id);
		send(fileContent);
	}
	void sendSignupMessage(String name, String password, String community){
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.addReceiver(new AID("serveur", AID.ISLOCALNAME));
		message.setOntology("signUp");
		message.addUserDefinedParameter("name", name);
		message.addUserDefinedParameter("password", Integer.toString(password.hashCode()));
		message.addUserDefinedParameter("community", community);
		send(message);
	}
	void sendLoginMessage(String name, String password){
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.addReceiver(new AID("serveur", AID.ISLOCALNAME));
		message.setOntology("login");
		message.addUserDefinedParameter("name", name);
		message.addUserDefinedParameter("password", Integer.toString(password.hashCode()));
		send(message);
	}
	void sendAddContactMessage(String contact){
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.addReceiver(new AID("serveur", AID.ISLOCALNAME));
		message.setOntology("addContact");
		message.addUserDefinedParameter("name", account.getName());
		message.addUserDefinedParameter("password", Integer.toString(account.getPassword()));
		message.addUserDefinedParameter("contact", contact);
		send(message);
	}
	void sendDisconnectMessage(){
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.addReceiver(new AID("serveur", AID.ISLOCALNAME));
		message.setOntology("disconnect");
		message.addUserDefinedParameter("name", account.getName());
		message.addUserDefinedParameter("password", Integer.toString(account.getPassword()));
		send(message);
	}
	void sendAIDRequest(String name){
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.addReceiver(new AID("serveur", AID.ISLOCALNAME));
		message.setOntology("AIDRequest");
		message.addUserDefinedParameter("name", name);
		send(message);
	}
	void sendFileRequest(String fileName, AID id){
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setContent(Message.SUCCESS);
		message.addReceiver(id);
		message.setOntology("fileRequest");
		message.addUserDefinedParameter("fileName", fileName);
		message.addUserDefinedParameter("name", account.getName());
		send(message);
	}
	void processMessage(ACLMessage message){
		if (message.getOntology().contentEquals("fileContent")){
			onReceiveFile(message);
		} else if (message.getOntology().contentEquals("loginReply")){
			onReceiveLoginReply(message);
		} else if (message.getOntology().contentEquals("signUpReply")){
			onReceiveSignUpReply(message);
		} else if (message.getOntology().contentEquals("disconnectReply")){
			onReceiveDisconnectReply(message);
		} else if (message.getOntology().contentEquals("addContactReply")){
			onReceiveAddContactReply(message);
		} else if (message.getOntology().contentEquals("AIDReply")){
			onReceiveAIDReply(message);
		} else if (message.getOntology().equals("fileRequest")){
			onReceiveFileRequest(message);
		}
	}
	static public final int EVENT_TYPE_ADD_FILE = 0;
	static public final int EVENT_TYPE_SEND_FILE = 1;
	static public final int EVENT_TYPE_REQUEST_FILE = 2;
	static public final int EVENT_TYPE_FIND_FILE = 3;
	static public final int EVENT_TYPE_LOGIN = 4;
	static public final int EVENT_TYPE_SIGNUP = 5;
	static public final int EVENT_TYPE_ADD_CONTACT = 6;
	@Override
	public void onGuiEvent(GuiEvent guiEvent){
		switch(guiEvent.getType()){
		case EVENT_TYPE_ADD_FILE:
		{
			String path = (String)guiEvent.getParameter(0);
			String content = (String)guiEvent.getParameter(1);
			addFile(path, content);
		}
			break;
		case EVENT_TYPE_SEND_FILE:
		{
			String target = (String)guiEvent.getParameter(0);
			String name = (String)guiEvent.getParameter(1);
			tmpFileName = name;
			op = OP_SEND_FILE;
			sendAIDRequest(target);
		}
			break;
		case EVENT_TYPE_REQUEST_FILE:
		{
			String target = (String)guiEvent.getParameter(0);
			String name = (String)guiEvent.getParameter(1);
			tmpFileName = name;
			op = OP_SEND_REQUEST;
			sendAIDRequest(target);
		}
			break;
		/*case EVENT_TYPE_FIND_FILE:
			findFile(file);
			break;*/
		case EVENT_TYPE_LOGIN:
		{
			String name = (String)guiEvent.getParameter(0);
			String password = (String)guiEvent.getParameter(1);	
			sendLoginMessage(name, password);
		}
			break;
		case EVENT_TYPE_SIGNUP:
		{
			String name = (String)guiEvent.getParameter(0);
			String password = (String)guiEvent.getParameter(1);
			String community = (String)guiEvent.getParameter(2);	
			sendSignupMessage(name, password, community);
		}
			break;
		case EVENT_TYPE_ADD_CONTACT:
		{
			String contact = (String)guiEvent.getParameter(0);	
			sendAddContactMessage(contact);
		}
			break;
		}
	}
	void onReceiveFile(ACLMessage message){
		String name = message.getUserDefinedParameter("name");
		String content = message.getContent();
		if (files.contains(name)) return;
		files.put(name, new AgentFileDescriptor(content, false));
		gui.updateFileList(files);
	}
	void onReceiveLoginReply(ACLMessage message){
		if (message.getContent().equals(Message.FAIL)) return;
		loggedIn = true;
		gui.updateContactList(Message.parseStringList(message.getUserDefinedParameter("contacts")));
		account = new UserAccount(message.getUserDefinedParameter("name"), 
				Integer.parseInt(message.getUserDefinedParameter("password")),
				message.getUserDefinedParameter("community"));
		gui.setMainScene();
	}
	void onReceiveSignUpReply(ACLMessage message){
		if (message.getContent().equals(Message.FAIL)) return;
		gui.setLoginScene();
	}
	void onReceiveDisconnectReply(ACLMessage message){
		if (message.getContent().equals(Message.FAIL)) return;
		loggedIn = false;
		account = null;
		gui.setLoginScene();
	}
	void onReceiveAddContactReply(ACLMessage message){
		if (message.getContent().equals(Message.FAIL)) return;
		String contact = message.getUserDefinedParameter("contact");
		gui.addContact(contact);
	}
	void onReceiveAIDReply(ACLMessage message){
		if (message.getContent().equals(Message.FAIL)){
			tmpFileName = null;
			return;
		}
		String targetAID = message.getUserDefinedParameter("name");
		if (op == OP_SEND_FILE)
		    sendFile(tmpFileName, files.get(tmpFileName).getContent(), new AID(targetAID, AID.ISLOCALNAME));
		else if (op == OP_SEND_REQUEST)
			sendFileRequest(tmpFileName, new AID(targetAID, AID.ISLOCALNAME));
	}
	void onReceiveFileRequest(ACLMessage message){
		if (message.getContent().equals(Message.FAIL)) return;
		String name = message.getUserDefinedParameter("name");
		String fileName = message.getUserDefinedParameter("fileName");
		
		gui.addFileRequest(name, fileName);
		
	}
}
