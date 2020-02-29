package agents;

import java.util.ArrayList;

public class UserAccount {
   String name;
   int password;
   String community;
   ArrayList < String > contacts = new ArrayList < String >();
   
   UserAccount(String name, int password, String community){
	   this.name = name;
	   this.password = password;
	   this.community = community;
   }
   
   boolean addContact(String name){
	   for(String it : contacts){
		   if (it.equals(it)){
			   return false;
		   }
	   }
	   contacts.add(name);
	   return true;
   }
   
   public String getName() { return name; }
   public int getPassword() { return password; }
   public String getCommunity() { return community; }
   public ArrayList < String > getContacts() { return contacts; }
}
