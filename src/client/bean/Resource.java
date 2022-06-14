package client.bean;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Resource {
	protected Image img;
	private static Resource instance;
	private Resource() {
		File f = new File("robots_sprite.png");//建立文件对象
		try {//文件操作放在错误处理块中
			img = ImageIO.read(f);//从文件中读取图像
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static Resource getInstance() {
		if(instance == null) 
			instance = new Resource();
		return instance;
	}
	
	
}

