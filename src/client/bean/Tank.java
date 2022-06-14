package client.bean;

import client.client.TankClient;
import client.event.TankHitEvent;
import client.event.TankHitListener;
import client.protocol.MessageMsg;
import client.protocol.TankDeadMsg;
import client.protocol.TankMoveMsg;
import client.protocol.TankReduceBloodMsg;
import client.strategy.Fire;
import client.strategy.FireAction;
import client.strategy.NormalFireAction;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class Tank implements TankHitListener, Fire {
    private int id;
    private int imageX1,imageX2,imageY;
    public static final int XSPEED = 3;
    public static final int YSPEED = 3;
    private static Image RSCimg = Resource.getInstance().img;//资源图片儿
    private String name;
    private boolean good;//坦克阵营好坏
    private int x, y;
    private boolean live = true;
    private TankClient tc;
    private Dir dir = Dir.U;
    private Dir ptDir = Dir.U;
    private int blood = 100;
    private BloodBar bb = new BloodBar();
    private FireAction fireAction = new NormalFireAction();//可以开火
    private boolean flag = true;
    public static final int UP = 0;
	public static final int RIGHT = 1;
	public static final int DOWN = 2;
	public static final int LEFT = 3;
	
    private static Toolkit tk = Toolkit.getDefaultToolkit();
    private static Image[] imgs = null;
    private static Map<String, Image> map = new HashMap<>();
    static{
    	
        imgs = new Image[]{//加载两方阵营的图片
            tk.getImage(Tank.class.getClassLoader().getResource("client/images/tank/tD.png"))
        };
        map.put("tD", imgs[0]);
        
    }

    public static final int WIDTH =  imgs[0].getWidth(null);
    public static final int HEIGHT = imgs[0].getHeight(null);

    public Tank(int x, int y, boolean good, String name) {
        this.x = x;
        this.y = y;
        this.good = good;
        this.name = name;
        calImageXY();
    }

    public Tank(String name, int x, int y, boolean good, Dir dir, TankClient tc) {
        this(x, y, good, name);
        this.dir = dir;
        this.tc = tc;
        calImageXY();
    }

    /**
     * 根据坦克阵营画出图片
     * @param g
     */
    public void draw(Graphics g) {
        if(!live) {
            if(!good) {
                tc.getTanks().remove(this);
            }
            return;
        }
        calImageXY();
        if(flag) {
			g.drawImage(RSCimg, x, y, x+34, y+34,imageX1, imageY,imageX2,imageY+34,null );
		}
		else {
			g.drawImage(RSCimg, x, y, x+34, y+34, imageX2, imageY,imageX2+34,imageY+34,null );
		}
		flag = !flag;
        g.drawString(name, x, y - 20);
        bb.draw(g);//画出血条
        TankMoveMsg msg = new TankMoveMsg(this.id, x,y, dir, ptDir);
        
        this.tc.getNc().send(msg);
        move();
    }

    /**
     * 根据坦克的方向进行移动
     */
    public void move() {
		if(live == false)
			return;
		int direction=0;
		switch(dir) {
    	case U:
    		direction = UP;
    		break;
    	case R:
    		direction =RIGHT;
    		break;
    	case D:
    		direction = DOWN;
    		break;
    	case L:
    		direction = LEFT;
    		break;
    	}
			switch(direction) {
			
				case 0: y-=YSPEED;
						y = (y<34?34:y);
				break;
				case 1: x+=XSPEED;
						x = (x>(TankClient.GAME_WIDTH-34)?(TankClient.GAME_WIDTH-34):x);
				break;
				case 2: y+=YSPEED;
						y = (y>(TankClient.GAME_HEIGHT-34)?(TankClient.GAME_HEIGHT-34):y);
				
				break;
				case 3: x-=XSPEED;
						x = (x<0?0:x);
				break;
			}
		
		
	}

    /**
     * 监听键盘按下, 上下左右移动分别对应WSAD
     * @param e
     */
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
        case  KeyEvent.VK_W:
			setDir(dir.U);
			break;
		case  KeyEvent.VK_D:
			setDir(dir.R);
			break;
		case  KeyEvent.VK_S:
			setDir(dir.D);
			break;
		case  KeyEvent.VK_A:
			setDir(dir.L);
			break;
		case KeyEvent.VK_Q:
			//Quit();
			break;
		case KeyEvent.VK_SPACE://监听到空格键按下则开火
            fire();
            //System.out.println(dir);
            break;
		case KeyEvent.VK_ENTER:
			tc.inputMsg();
			
			break;
        }
    }

    /**
     * 根据4个方向的布尔值判断坦克的方向
     */

    public void sendMsg(String Msg) {
    	if(Msg == "")
			return;
		MessageMsg msg2 = new MessageMsg(this,tc.isPublic);
        this.getTc().getNc().send(msg2);
        tc.SetContentNull();
    }

    public void fire() {//发出一颗炮弹的方法
        fireAction.fireAction(this);
    }

    @Override
    public void actionToTankHitEvent(TankHitEvent tankHitEvent) {
        this.tc.getExplodes().add(new Explode(tankHitEvent.getSource().getX() - 20,
                tankHitEvent.getSource().getY() - 20, this.tc));//坦克自身产生一个爆炸
        if(this.blood == 20){//坦克每次扣20滴血, 如果只剩下20滴了, 那么就标记为死亡.
            this.live = false;
            TankDeadMsg msg = new TankDeadMsg(this.id);//向其他客户端转发坦克死亡的消息
            this.tc.getNc().send(msg);
            this.tc.getNc().sendClientDisconnectMsg();//和服务器断开连接
            this.tc.gameOver();
            return;
        }
        this.blood -= 20;//血量减少20并通知其他客户端本坦克血量减少20.
        TankReduceBloodMsg msg = new TankReduceBloodMsg(this.id, tankHitEvent.getSource());
        this.tc.getNc().send(msg);
    }

    /**
     * 血条
     */
    private class BloodBar {
        public void draw(Graphics g) {
            Color c = g.getColor();
            g.setColor(Color.BLACK);
            g.drawRect(x, y - 15, 30, 8);
            int w = (30 * blood) / 100 ;
            if(good)
            	g.setColor(Color.YELLOW);
            else
            	g.setColor(Color.GREEN);
            g.fillRect(x, y - 15, w, 8);
            g.setColor(c);
        }
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, imgs[0].getWidth(null), imgs[0].getHeight(null));
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public boolean isGood() {
        return good;
    }

    public void setGood(boolean good) {
        this.good = good;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Dir getDir() {
        return dir;
    }

    public void setDir(Dir dir) {
        this.dir = dir;
    }

    public Dir getPtDir() {
        return dir;
    }

    public void setPtDir(Dir ptDir) {
        this.ptDir = ptDir;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBlood() {
        return blood;
    }

    public void setBlood(int blood) {
        this.blood = blood;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TankClient getTc() {
        return tc;
    }

    public void setTc(TankClient tc) {
        this.tc = tc;
    }
    private void calImageXY() {
    	int faction=1;
    	int direction=0;
    	switch(dir) {
    	case U:
    		direction = UP;
    		break;
    	case R:
    		direction = RIGHT;
    		break;
    	case D:
    		direction = DOWN;
    		break;
    	case L:
    		direction = LEFT;
    		break;
    	}
    	if(good==true)
    		faction = 0;
		imageX1 =  direction*68;
		imageX2 = 34+direction*68;
		imageY = faction*34;
	}
   
    
}
