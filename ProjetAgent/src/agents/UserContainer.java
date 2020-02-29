package agents;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.gui.GuiEvent;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import javafx.application.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.event.*;
import javafx.collections.*;
public class UserContainer extends Application{
 static private final int WINDOW_WIDTH = 550;
 static private final int WINDOW_HEIGHT = 600;
 private User user;
 private Thread thread;
 static private String arg;
 private ObservableList < String > observableFileList = FXCollections.observableArrayList();
 ListView < String > fileListView = new ListView(observableFileList);
 
 private ObservableList < String > observableContactList = FXCollections.observableArrayList();
 ListView < String > contactListView = new ListView(observableContactList);
 
 private ObservableList < String > observableRequestList = FXCollections.observableArrayList();
 ListView < String > requestListView = new ListView(observableRequestList);
 
 
 static Stage primaryStage;
 static Scene mainScene;
 static BorderPane loginPane;
 static BorderPane signUpPane;
 static BorderPane mainPane;
 TextField loginNameTextField;
 TextField loginPasswordTextField;
 TextField signupNameTextField;
 TextField signupPasswordTextField;
 TextField requestUserTF;
 TextField requestFileNameTF;
 
 ComboBox signupCommunityComboBox;
 
 public void setUser(User user) { this.user = user; }
 public static void main(String args[]){
	 if (args.length > 0)
	 arg = args[0];
	 else arg = null;
	 launch(UserContainer.class);
 }
 public void startContainer(){
	 if (arg == null) return;
	 try {
		 Runtime runtime = Runtime.instance();
		 Properties properties = new ExtendedProperties();
		 properties.setProperty(Profile.MAIN_HOST, "localhost");
		 Profile profile = new ProfileImpl(properties);
		 AgentContainer container = runtime.createAgentContainer(profile);
		 AgentController agentController = container.createNewAgent(arg, "agents.User", new Object[]{this});
		 agentController.start();
	} catch (ControllerException e) { 
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 }
 
 
 public void updateFileList(Hashtable < String, AgentFileDescriptor > files){
	 observableFileList.clear();
	 for(Map.Entry < String, AgentFileDescriptor > it : files.entrySet()){
		 observableFileList.add(it.getKey());
	 }
 }
 public void updateContactList(ArrayList < String > contacts){
	 observableContactList.clear();
	 for(String it : contacts){
		 observableContactList.add(it);
	 }
 }
 public void addContact(String contact){
	 observableContactList.add(contact);
 }
 public void addFileRequest(String senderName, String fileName){
	 observableRequestList.add(senderName + " vous demande le fichier : " + "'" + fileName + "'");
 }
 public void start(Stage p) throws Exception{
	 thread = Thread.currentThread();
	 startContainer();
	 primaryStage = p;
	 primaryStage.setTitle(arg);
	 primaryStage.setOnCloseRequest(event -> {
		 user.doDelete();
		});
	 buildMainScene(primaryStage);
	 buildLoginScene(primaryStage);
	 buildSignupScene(primaryStage);
	 primaryStage.setScene(new Scene(signUpPane, WINDOW_WIDTH, WINDOW_HEIGHT));
	 primaryStage.show();
 }
 public void setLoginScene(){
	 primaryStage.getScene().setRoot(loginPane);
 }
 public void setSignupScene(){
	 primaryStage.getScene().setRoot(signUpPane);
 }
 public void setMainScene(){
	 primaryStage.getScene().setRoot(mainPane);
 }
 private void buildLoginScene(Stage primaryStage){
	 loginPane = new BorderPane();	
	 GridPane gridPane = new GridPane();
	 gridPane.setHgap(10);
	 VBox vbox = new VBox();
	 vbox.setPadding(new Insets(10));
	 vbox.setSpacing(25.0);
	 Label nameLabel = new Label("name");
	 loginNameTextField = new TextField();
	 Label passwordLabel = new Label("password");
	 loginPasswordTextField = new TextField();
	 HBox buttonHBox = new HBox();
	 buttonHBox.setPadding(new Insets(10));
	 buttonHBox.setSpacing(25.0); 
	 Button loginButton = new Button("login");
	 Button signupButton = new Button("signup");
	 buttonHBox.getChildren().add(loginButton);
	 buttonHBox.getChildren().add(signupButton);
	 loginButton.setOnAction(new EventHandler < ActionEvent >(){
		 @Override
		 public void handle(ActionEvent event){
			 try {
				GuiEvent guiEvent = new GuiEvent(this, User.EVENT_TYPE_LOGIN);
				 guiEvent.addParameter(loginNameTextField.getText());
				 guiEvent.addParameter(loginPasswordTextField.getText());
				 user.onGuiEvent(guiEvent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	 });
	 signupButton.setOnAction(new EventHandler < ActionEvent >(){
		 @Override
		 public void handle(ActionEvent event){
			 try {
				setSignupScene();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	 });
	 gridPane.add(nameLabel, 0, 0);
	 gridPane.add(loginNameTextField, 1, 0);
	 gridPane.add(passwordLabel, 0, 1);
	 gridPane.add(loginPasswordTextField, 1, 1);
	 gridPane.add(buttonHBox, 1, 2);
	 
	 vbox.getChildren().add(gridPane);
	 loginPane.setCenter(vbox);
 }
 private void buildMainScene(Stage primaryStage){
	 mainPane = new BorderPane();	 
	 HBox hbox = new HBox();
	 hbox.setPadding(new Insets(10));
	 hbox.setSpacing(10.0);
	 Label addContactLabel = new Label("contact");
	 TextField addContactTextField = new TextField();
	 Button addContactButton = new Button("add");
	 addContactButton.setOnAction(new EventHandler < ActionEvent >(){
		 @Override
		 public void handle(ActionEvent event){
			 try {
				String contactName = addContactTextField.getText();
				 if (contactName != null){
				   GuiEvent guiEvent = new GuiEvent(this, User.EVENT_TYPE_ADD_CONTACT);
				   guiEvent.addParameter(addContactTextField.getText());
				   user.onGuiEvent(guiEvent);
				 }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	 });
	 hbox.getChildren().add(addContactLabel);
	 hbox.getChildren().add(addContactTextField);
	 hbox.getChildren().add(addContactButton);
	 HBox requestFileHBox = new HBox();
	 requestFileHBox.setPadding(new Insets(10));
	 requestFileHBox.setSpacing(10.0);
	 Label requestUserLabel = new Label("user");
	 requestUserTF = new TextField();
	 Label reauestFileNameLabel = new Label("fichier");
	 requestFileNameTF = new TextField();
	 Button sendRequestFileButton = new Button("demande fichier");
	 sendRequestFileButton.setOnAction(new EventHandler < ActionEvent >(){
		 @Override
		 public void handle(ActionEvent event){
			 GuiEvent guiEvent = new GuiEvent(this, User.EVENT_TYPE_REQUEST_FILE);
			 guiEvent.addParameter(requestUserTF.getText());
			 guiEvent.addParameter(requestFileNameTF.getText());
			 user.onGuiEvent(guiEvent);
		 }
	 });
	 requestFileHBox.getChildren().add(requestUserLabel);
	 requestFileHBox.getChildren().add(requestUserTF);
	 requestFileHBox.getChildren().add(reauestFileNameLabel);
	 requestFileHBox.getChildren().add(requestFileNameTF);
	 requestFileHBox.getChildren().add(sendRequestFileButton);
	// borderPane.setTop(hbox);
	 
	 VBox vBox = new VBox();
	 GridPane gridPane = new GridPane();
	 Button addButton = new Button("addFile");
	 addButton.setOnAction(new EventHandler < ActionEvent >(){
		 @Override
		 public void handle(ActionEvent event){
			 try {
				FileChooser fileChooser = new FileChooser();
				 fileChooser.setTitle("Open Resource File");
				 File file = fileChooser.showOpenDialog(primaryStage);
				 if (file == null) return;
				 String content = null;
				 try {
					 FileReader reader = new FileReader(file);
				     char[] chars = new char[(int) file.length()];
					 reader.read(chars);
				     content = new String(chars);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 if (content == null) return;
				 GuiEvent guiEvent = new GuiEvent(this, User.EVENT_TYPE_ADD_FILE);
				 guiEvent.addParameter(Utils.getFileName(file.getAbsolutePath()));
				 guiEvent.addParameter(content);
				 user.onGuiEvent(guiEvent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		     
		 }
	 });
	 Button sendButton = new Button("sendFile");
	 sendButton.setOnAction(new EventHandler < ActionEvent >(){
		 @Override
		 public void handle(ActionEvent event){
		     try {
				String target = contactListView.getSelectionModel().getSelectedItem();
				 String name = fileListView.getSelectionModel().getSelectedItem();
				 if (target == null || name == null) return;
				 GuiEvent guiEvent = new GuiEvent(this, User.EVENT_TYPE_SEND_FILE);
				 guiEvent.addParameter(target);
				 guiEvent.addParameter(name);
				 user.onGuiEvent(guiEvent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		     
		 }
	 });
	 Button disconnectButton = new Button("disconnect");
	 disconnectButton.setOnAction(new EventHandler < ActionEvent >(){
		 @Override
		 public void handle(ActionEvent event){
			 try {
				setLoginScene();
				 user.sendDisconnectMessage();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	 });
	 gridPane.add(new Label("fichiers"), 0, 0);
	 gridPane.add(fileListView, 0, 1);
	 gridPane.add(addButton, 1, 1);
	 gridPane.add(new Label("contactes"), 0, 2);
	 gridPane.add(contactListView, 0, 3);
	 gridPane.add(sendButton, 1, 3);
	 gridPane.add(new Label("les demandes"), 0, 4);
	 gridPane.add(requestListView, 0, 5);
	 vBox.setPadding(new Insets(10));
	 vBox.setSpacing(10.0); 
	 vBox.getChildren().add(disconnectButton);
	 vBox.getChildren().add(hbox);
	 vBox.getChildren().add(requestFileHBox);
	 vBox.getChildren().add(gridPane);
	 
	 mainPane.setCenter(vBox);
 }
 private void buildSignupScene(Stage primaryStage){
	 signUpPane = new BorderPane();	
	 GridPane gridPane = new GridPane();
	 gridPane.setHgap(10);
	 VBox vbox = new VBox();
	 vbox.setPadding(new Insets(10));
	 vbox.setSpacing(25.0);
	 Label nameLabel = new Label("name");
	 signupNameTextField = new TextField();
	 Label passwordLabel = new Label("password");
	 signupPasswordTextField = new TextField();
	 Label communityLabel = new Label("community");
	 ObservableList < String > options = 
		FXCollections.observableArrayList(
				Community.SCIENCE.toString(), Community.MATH.toString(),
				Community.ART.toString(), Community.LITERATURE.toString(),
				Community.AGRICULTURE.toString(), Community.BUSNESS.toString(),
				Community.ASTRONOMY.toString()
	 );			
	 signupCommunityComboBox = new ComboBox(options);
	 HBox buttonHBox = new HBox();
	 buttonHBox.setPadding(new Insets(10));
	 buttonHBox.setSpacing(25.0); 
	 Button signupButton = new Button("signup");
	 Button loginButton = new Button("login");
	 buttonHBox.getChildren().add(signupButton);
	 buttonHBox.getChildren().add(loginButton);
	 signupButton.setOnAction(new EventHandler < ActionEvent >(){
		 @Override
		 public void handle(ActionEvent event){
			 try {
				if (signupCommunityComboBox.getSelectionModel().getSelectedItem().toString().length() == 0) return;
				 GuiEvent guiEvent = new GuiEvent(this, User.EVENT_TYPE_SIGNUP);
				 guiEvent.addParameter(signupNameTextField.getText());
				 guiEvent.addParameter(signupPasswordTextField.getText());
				 guiEvent.addParameter(signupCommunityComboBox.getSelectionModel().getSelectedItem());
				 user.onGuiEvent(guiEvent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	 });
	 loginButton.setOnAction(new EventHandler < ActionEvent >(){
		 @Override
		 public void handle(ActionEvent event){
			 try {
				setLoginScene();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	 });
	 gridPane.add(nameLabel, 0, 0);
	 gridPane.add(signupNameTextField, 1, 0);
	 gridPane.add(passwordLabel, 0, 1);
	 gridPane.add(signupPasswordTextField, 1, 1);
	 gridPane.add(communityLabel, 0, 2);
	 gridPane.add(signupCommunityComboBox, 1, 2);
	 gridPane.add(buttonHBox, 1, 3);
	 
	 vbox.getChildren().add(gridPane);
	 signUpPane.setCenter(vbox);
 }
}
