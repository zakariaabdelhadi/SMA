package agents;

public class AgentFileDescriptor {
 private String content;
 private boolean recommend;
 
 AgentFileDescriptor(String content, boolean recommend){
	 this.content = content;
	 this.recommend = recommend;
 }
 
 void setPath(String content) { this.content = content; }
 void setRecommend(boolean recommend) { this.recommend = recommend; }
 
 public boolean getRecommend() { return recommend; }
 public String getContent() { return content; }
}
