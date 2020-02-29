package agents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

public class Serveur extends Agent {
	Hashtable < String, UserAccount > userAccounts = new Hashtable < String, UserAccount >();
	Hashtable < String, AID > loggedInUsers = new Hashtable < String, AID >();
	Hashtable < String, ArrayList < UserAccount >> communities = new Hashtable < String, ArrayList < UserAccount >>();
	@Override
	protected void setup(){
	  communities.put(Community.SCIENCE.toString(), new ArrayList < UserAccount >());
	  communities.put(Community.MATH.toString(), new ArrayList < UserAccount >());
	  communities.put(Community.ART.toString(), new ArrayList < UserAccount >());
	  communities.put(Community.LITERATURE.toString(), new ArrayList < UserAccount >());
	  communities.put(Community.AGRICULTURE.toString(), new ArrayList < UserAccount >());
	  communities.put(Community.BUSNESS.toString(), new ArrayList < UserAccount >());
	  communities.put(Community.ASTRONOMY.toString(), new ArrayList < UserAccount >());
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
	}
	@Override
	protected void takeDown(){
	  for(Map.Entry < String, AID > it : loggedInUsers.entrySet()){
		 ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		 message.addReceiver(it.getValue());
		 message.setOntology("disconnectReply");
		 message.setContent(Message.SUCCESS);
		 send(message);
	  }
	}	
	void processMessage(ACLMessage message) {
		if (message.getOntology().equals("signUp")){
			onReceiveSignupMessage(message);
		} else if (message.getOntology().equals("login")){
			onReceiveLoginMessage(message);
		} else if (message.getOntology().equals("disconnect")){
			onReceiveDisconnectMessage(message);
		} else if (message.getOntology().equals("addContact")){
			onReceiveAddContactMessage(message);
		} else if (message.getOntology().equals("AIDRequest")){
			onReceiveAIDRequest(message);
		}
	}
	
	boolean isValidName(String name){
		return name.indexOf(';') == -1;
	}
	void sendReply(ACLMessage message, String ontology, String content){
		ACLMessage replyMessage = message.createReply();
		replyMessage.setOntology(ontology);
		replyMessage.setContent(content);	
		send(replyMessage);
	}
	void sendAddContactReply(String contact, AID id){
		ACLMessage replyMessage = new ACLMessage(ACLMessage.INFORM);
		replyMessage.addReceiver(id);
		replyMessage.setOntology("addContactReply");
		replyMessage.setContent(Message.SUCCESS);	
		replyMessage.addUserDefinedParameter("contact", contact);
		send(replyMessage);
	}
	void onReceiveSignupMessage(ACLMessage message){
		String name = message.getUserDefinedParameter("name");
		int password = Integer.parseInt(message.getUserDefinedParameter("password"));
		String community = message.getUserDefinedParameter("community");
		
		if (!isValidName(name) || userAccounts.get(name) != null){
			sendReply(message, "signUpReply", Message.FAIL);
			return;
		}
		UserAccount newAccount = new UserAccount(name, password, community);
		
		userAccounts.put(name, newAccount);
		communities.get(community).add(newAccount);
		
		sendReply(message, "signUpReply", Message.SUCCESS);
		for(Map.Entry < String, AID > it : loggedInUsers.entrySet()){
			if (community.equals(userAccounts.get(it.getKey()).getCommunity()))
			sendAddContactReply(name, it.getValue());
		}
		
	}
	void onReceiveLoginMessage(ACLMessage message){
		String name = message.getUserDefinedParameter("name");
		int password = Integer.parseInt(message.getUserDefinedParameter("password"));
		
		UserAccount account = userAccounts.get(name);
		if (account == null){
			sendReply(message, "loginReply", Message.FAIL);
			return;
		}
		if (account.getPassword() != password){
			sendReply(message, "loginReply", Message.FAIL);
			return;
		}
		if (loggedInUsers.get(account.getName()) != null){
			sendReply(message, "loginReply", Message.FAIL);
			return;
		}
		loggedInUsers.put(account.getName(), message.getSender());
		
		ACLMessage replyMessage = message.createReply();
		replyMessage.addUserDefinedParameter("name", account.getName());
		replyMessage.addUserDefinedParameter("password", Integer.toString(account.getPassword()));
		replyMessage.addUserDefinedParameter("community", account.getCommunity());
		ArrayList < String > communityCAL = new ArrayList < String >();
		for(UserAccount acc : communities.get(account.getCommunity())){
			if (!acc.getName().equals(account.getName()))
			communityCAL.add(acc.getName());
		}
		String myContacts = Message.createStringListMessage(account.getContacts());
		String communityContacts = Message.createStringListMessage(communityCAL);
		replyMessage.addUserDefinedParameter("contacts", myContacts + communityContacts);
		replyMessage.setOntology("loginReply");
		replyMessage.setContent(Message.SUCCESS);
		send(replyMessage);
		
	}
	void onReceiveDisconnectMessage(ACLMessage message){
		String name = message.getUserDefinedParameter("name");
		int password = Integer.parseInt(message.getUserDefinedParameter("password"));
		
		UserAccount account = userAccounts.get(name);
		if (account == null){
			sendReply(message, "disconnectReply", Message.FAIL);
			return;
		}
		if (account.getPassword() != password){
			sendReply(message, "disconnectReply", Message.FAIL);
			return;
		}
		loggedInUsers.remove(account.getName());
		sendReply(message, "disconnectReply", Message.SUCCESS);
	}
	void onReceiveAddContactMessage(ACLMessage message){
		String name = message.getUserDefinedParameter("name");
		int password = Integer.parseInt(message.getUserDefinedParameter("password"));
		String contact = message.getUserDefinedParameter("contact");
		
		if (contact.equals(name)){
			sendReply(message, "addContactReply", Message.FAIL);
			return;
		}
		UserAccount account = userAccounts.get(name);
		if (account == null) {
			sendReply(message, "addContactReply", Message.FAIL);
			return;
		}
		if (password != account.getPassword()) {
			sendReply(message, "addContactReply", Message.FAIL);
			return;
		}
		if (userAccounts.get(contact) == null){
			sendReply(message, "addContactReply", Message.FAIL);
			return;
		}
		for(String it : account.getContacts()){
			if (it.equals(contact)){
				sendReply(message, "addContactReply", Message.FAIL);
				return;
			}
		}

		if (account.addContact(contact)){
			ACLMessage replyMessage = message.createReply();
			replyMessage.setOntology("addContactReply");
			replyMessage.setContent(Message.SUCCESS);	
			replyMessage.addUserDefinedParameter("contact", contact);
			send(replyMessage);
		} else {
			sendReply(message, "addContactReply", Message.FAIL);
		}
		
	}
	void onReceiveAIDRequest(ACLMessage message){
		String name = message.getUserDefinedParameter("name");
		AID id = loggedInUsers.get(name);
		if (id == null) {
			sendReply(message, "AIDReply", Message.FAIL);
			return;
		}
		ACLMessage replyMessage = message.createReply();
		replyMessage.setOntology("AIDReply");
		replyMessage.setContent(Message.SUCCESS);	
		replyMessage.addUserDefinedParameter("name", id.getLocalName());
		send(replyMessage);
	}
}
