package agents;

import java.util.ArrayList;


public class Utils {
  static String getFileName(String filePath){
	  int i = filePath.length() - 1;
	  while(i > 0 && filePath.charAt(i) != '\\'){
		  i--;
	  }
	  String name = filePath.substring(i);
	  return name;
  }
}
