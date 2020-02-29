package agents;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;

public class MainContainer{
 public static void main(String args[]){
	 try {
		 Runtime runtime = Runtime.instance();
		 Properties properties = new ExtendedProperties();
		 properties.setProperty(Profile.GUI, "true");
		 Profile profile = new ProfileImpl(properties);
		 AgentContainer mainContainer = runtime.createMainContainer(profile);
		 AgentController agentController = mainContainer.createNewAgent("serveur", "agents.Serveur", new Object[]{});
		 agentController.start();
	} catch (ControllerException e) {
		e.printStackTrace();
	}
 }
}
