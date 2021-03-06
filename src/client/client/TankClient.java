package client.client;

import client.bean.BattleMap;
import client.bean.Dir;
import client.bean.Explode;
import client.bean.Message;
import client.bean.Missile;
import client.bean.Tank;
import client.protocol.MissileDeadMsg;
import client.protocol.TankDeadMsg;
import server.TankServer;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class TankClient extends Frame {
    public static final int GAME_WIDTH = 800;
    public static final int GAME_HEIGHT = 610;
    private Image offScreenImage = null;
    public boolean isPublic = true;
    
    public boolean showDialog = false;
    public BattleMap mymap = BattleMap.getInstance();
    private Tank myTank;//客户端的坦克
    private NetClient nc = new NetClient(this);
    private ConDialog dialog = new ConDialog();
    private GameOverDialog gameOverDialog = new GameOverDialog();
    private UDPPortWrongDialog udpPortWrongDialog = new UDPPortWrongDialog();
    private ServerNotStartDialog serverNotStartDialog = new ServerNotStartDialog();
    private TextField MsgContent = new TextField("", 8);
    
    private List<Missile> missiles = new ArrayList<>();//存储游戏中的子弹集合
    private List<Explode> explodes = new ArrayList<>();//爆炸集合
    private List<Tank> tanks = new ArrayList<>();//坦克集合
    private ArrayList<Message> Messages = new ArrayList<Message>();//消息集合
    private JFrame textFrame = new JFrame("文本输入窗体");
    private JLabel label = new JLabel(isPublic?"[公开] :":"[队伍] :");
    @Override
    public void paint(Graphics g) {
        //g.drawString("missiles count:" + missiles.size(), 10, 50);
        //g.drawString("explodes count:" + explodes.size(), 10, 70);
        //g.drawString("tanks    count:" + tanks.size(), 10, 90);
    	
    	for(int i = 0; i<Messages.size();i++) {
    		Message m = Messages.get(i);
    		if(m.TTL==0) {
    			Messages.remove(i);
    			break;
    		}
    		
    		g.drawString(m.getMsg(),300,400-i*10);
    		
    		
    		m.TTL--;//time to live
    	}
        for(int i = 0; i < missiles.size(); i++) {//客户端绘画子弹爆炸动画、绘画坦克、绘画其他坦克都在客户端的paint实现。
            Missile m = missiles.get(i);
            if(m.hitTank(myTank)){
//                TankDeadMsg msg = new TankDeadMsg(myTank.getId());
//                nc.send(msg);
                MissileDeadMsg mmsg = new MissileDeadMsg(m.getTankId(), m.getId());
                nc.send(mmsg);
//                nc.sendClientDisconnectMsg();
//                gameOverDialog.setVisible(true);
            }
            m.draw(g);
        }
        for(int i = 0; i < explodes.size(); i++) {
            Explode e = explodes.get(i);
            e.draw(g);
        }
        for(int i = 0; i < tanks.size(); i++) {
            Tank t = tanks.get(i);
            t.draw(g);
        }
        
        if(null != myTank){
            myTank.draw(g);
        }
        
    }

    @Override
    public void update(Graphics g) {
        if(offScreenImage == null) {
            offScreenImage = this.createImage(800, 600);
        }
        Graphics gOffScreen = offScreenImage.getGraphics();
        Color c = gOffScreen.getColor();
        gOffScreen.setColor(Color.GRAY);
        gOffScreen.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        gOffScreen.setColor(c);
        paint(gOffScreen);
        mymap.draw(gOffScreen);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    public void launchFrame() {
        this.setLocation(0, 0);
        this.setSize(GAME_WIDTH, GAME_HEIGHT);
        this.setTitle("TankClient");
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                nc.sendClientDisconnectMsg();//关闭窗口前要向服务器发出注销消息.
                System.exit(0);
            }
        });
        this.setResizable(false);
        this.setBackground(Color.LIGHT_GRAY);

        this.addKeyListener(new KeyMonitor());

        this.setVisible(true);
        textFrame.setLayout(new FlowLayout());
        textFrame.add(label);
        textFrame.add(MsgContent);
        label.setSize(50,100);
        textFrame.setLocation(200, 600);
        textFrame.setSize(400,100);
        MsgContent.addKeyListener(new java.awt.event.KeyAdapter() {
        	public void keyPressed(KeyEvent e) {
        		if(e.getKeyCode()==KeyEvent.VK_ENTER){
        			textFrame.setVisible(false);
        			myTank.sendMsg(getContent());
        		}
        		else if(e.getKeyCode()==KeyEvent.VK_CONTROL) {
        			isPublic=!isPublic;
        			label.setText(isPublic?"[公开] :":"[队伍] :");
        		}
        		}
        		});
        new Thread(new PaintThread()).start();

        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        TankClient tc = new TankClient();
        
        tc.launchFrame();
    }

    /**
     * 重画线程
     */
    class PaintThread implements Runnable {
        public void run() {
            while(true) {
                repaint();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class KeyMonitor extends KeyAdapter {

        

        @Override
        public void keyPressed(KeyEvent e) {
            myTank.keyPressed(e);
        }
    }

    /**
     * 游戏开始前连接到服务器的对话框
     */
    class ConDialog extends Dialog{
        Button b = new Button("connect to server");
        TextField tfIP = new TextField("127.0.0.1", 15);//服务器的IP地址
        TextField tfTankName = new TextField("myTank", 8);
        
        public ConDialog() {
            super(TankClient.this, true);
            this.setLayout(new FlowLayout());
            this.add(new Label("server IP:"));
            this.add(tfIP);
            this.add(new Label("tank name:"));
            this.add(tfTankName);
            this.add(b);
            this.setLocation(500, 400);
            this.pack();
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setVisible(false);
                    System.exit(0);
                }
            });
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String IP = tfIP.getText().trim();
                    String tankName = tfTankName.getText().trim();
                    myTank = new Tank(tankName, 50 + (int)(Math.random() * (GAME_WIDTH - 100)),
                            50 + (int)(Math.random() * (GAME_HEIGHT - 100)), true, Dir.U, TankClient.this);
                    nc.connect(IP);
                    setVisible(false);
                }
            });
        }
    }

    /**
     * 坦克死亡后退出的对话框
     */
    class GameOverDialog extends Dialog{
        Button b = new Button("exit");
        public GameOverDialog() {
            super(TankClient.this, true);
            this.setLayout(new FlowLayout());
            this.add(new Label("Game Over~"));
            this.add(b);
            this.setLocation(500, 400);
            this.pack();
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
        }
    }

    /**
     * UDP端口分配失败后的对话框
     */
    class UDPPortWrongDialog extends Dialog{
        Button b = new Button("ok");
        public UDPPortWrongDialog() {
            super(TankClient.this, true);
            this.setLayout(new FlowLayout());
            this.add(new Label("something wrong, please connect again"));
            this.add(b);
            this.setLocation(500, 400);
            this.pack();
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
        }
    }

    /**
     * 连接服务器失败后的对话框
     */
    class ServerNotStartDialog extends Dialog{
        Button b = new Button("ok");
        public ServerNotStartDialog() {
            super(TankClient.this, true);
            this.setLayout(new FlowLayout());
            this.add(new Label("The server has not been opened yet..."));
            this.add(b);
            this.setLocation(500, 400);
            this.pack();
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
        }
    }

    public void gameOver(){
        this.gameOverDialog.setVisible(true);
    }

    public void inputMsg() {
    	textFrame.setVisible(!showDialog);
    }
    
    public List<Missile> getMissiles() {
        return missiles;
    }

    public void setMissiles(List<Missile> missiles) {
        this.missiles = missiles;
    }

    public List<Explode> getExplodes() {
        return explodes;
    }

    public void setExplodes(List<Explode> explodes) {
        this.explodes = explodes;
    }

    public List<Tank> getTanks() {
        return tanks;
    }

    public void setTanks(List<Tank> tanks) {
        this.tanks = tanks;
    }

    public Tank getMyTank() {
        return myTank;
    }

    public void setMyTank(Tank myTank) {
        this.myTank = myTank;
    }

    public NetClient getNc() {
        return nc;
    }

    public void setNc(NetClient nc) {
        this.nc = nc;
    }

    public UDPPortWrongDialog getUdpPortWrongDialog() {
        return udpPortWrongDialog;
    }

    public ServerNotStartDialog getServerNotStartDialog() {
        return serverNotStartDialog;
    }

	public ArrayList<Message> getMessages() {
		// TODO Auto-generated method stub
		return Messages;
	}
	
	public String getContent() {
		
		String temp=(isPublic?"[所有人] ":"[队伍] ");
		temp = temp+myTank.getName()+":"+MsgContent.getText();
		return temp;
	}
	public void SetContentNull() {
		MsgContent.setText("");
	}
		
	
}