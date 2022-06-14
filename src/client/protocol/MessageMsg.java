package client.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import client.bean.Dir;
import client.bean.Message;
import client.bean.Missile;
import client.bean.Tank;
import client.client.TankClient;

public class MessageMsg implements Msg{
	private int msgType = Msg.MESSAGE_MSG;
    private Tank tank;
    private TankClient tc;
    private boolean isPublic = false;
    public MessageMsg(Tank tank,boolean isPublic){
    	this.tank = tank;
    	this.isPublic = isPublic;
    }
	public MessageMsg(TankClient tc) {
		// TODO Auto-generated constructor stub
		this.tc = tc;
	}
	@Override
	public void send(DatagramSocket ds, String IP, int UDP_Port) {
		// TODO Auto-generated method stub
		ByteArrayOutputStream baos = new ByteArrayOutputStream(150);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(msgType);
            dos.writeInt(tank.getId());
            dos.writeBoolean(tank.isGood());
            dos.writeBoolean(isPublic);
            dos.writeUTF(tank.getTc().getContent());

        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] buf = baos.toByteArray();
        try{
            DatagramPacket dp = new DatagramPacket(buf, buf.length, new InetSocketAddress(IP, UDP_Port));
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@Override
	public void parse(DataInputStream dis) {
		// TODO Auto-generated method stub
		try{
            int tankId = dis.readInt();
            boolean isGood = dis.readBoolean();
            boolean isPublic = dis.readBoolean();
            if(isPublic||isGood==tc.getMyTank().isGood()) {
            	String MsgContent = dis.readUTF();
                Message Msg = new Message(isGood, isPublic, tankId, MsgContent);
                tc.getMessages().add(Msg);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
