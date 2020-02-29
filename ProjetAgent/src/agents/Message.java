package agents;

import java.util.ArrayList;

public class Message {
	  static final String SUCCESS = "success";
	  static final String FAIL = "fail";
	  
	  static String createStringListMessage(ArrayList < String > content){
		  StringBuffer buffer = new StringBuffer();
		  for(String it : content){
			  buffer.append(it);
			  buffer.append(";");
		  }
		  return buffer.toString();
	  }
	  static ArrayList < String > parseStringList(String message){
		  ArrayList < String > contacts = new ArrayList < String >();
		  int b = 0;
		  int i = 0;
		  while (message.length() > i){
		  while(message.length() > i && message.charAt(i) != ';')i++;
		   contacts.add(message.substring(b, i));
			  i++;
			  b = i;
		  }  
		  return contacts;
	  }
}
