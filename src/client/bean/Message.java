package client.bean;

public class Message {

	boolean good;
	boolean isPublic;
	int id;
	String Msg="";
	public int TTL = 40;
	public Message(boolean good,boolean isPublic,int id,String Msg) {
		this.good = good;
		this.isPublic = isPublic;
		this.id = id;
		this.Msg = Msg;
	}
	public String getMsg() {
		return Msg;
	}
}
