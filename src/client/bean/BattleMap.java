package client.bean;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;





public class BattleMap {
	private int[][] brickBlocks = {{2,5,0},{2,6,0},{3,5,0},{4,5,0},{5,5,0},{6,5,0},{7,5,0},{7,6,0},{9,6,0},{9,7,0},{9,8,0},{15,5,0},{16,5,0},{14,5,0},{17,5,0},{18,5,0},{19,5,0}};
	private int[][] waterBlocks = {{0,1},{0,2},{1,1},{1,2},{12,7},{12,8},{12,9},{13,7},{13,8},{13,9}};
	private int[][] grassBlocks = {{13,1},{14,1},{1,10},{2,10},{3,10},{4,10},{1,11},{2,11},{3,11},{4,11},{12,6},{13,6},{12,5},{13,5}};
	private int[][] stoneBlocks = {{0,7},{0,8},{0,9}};

	private int waterState = 0;
	private Image img;
	private static BattleMap instance;
	public BattleMap() {
		img = Resource.getInstance().img;
	}
	public void draw(Graphics gOffScreen) {
		
		gOffScreen.drawImage(img, 250, 400-34, 284, 400, 646, 34*5, 680, 34*6, null);
		gOffScreen.drawImage(img, 100, 50, 134, 84, 306, 238, 340, 272, null);
		gOffScreen.drawImage(img, 250, 50, 284, 84, 306, 238, 340, 272, null);
		gOffScreen.drawImage(img, 400, 50, 434, 84, 306, 238, 340, 272, null);
		
		
		for(int i = 0;i<brickBlocks.length;i++) {
			if(brickBlocks[i][2]==0){
				gOffScreen.drawImage(img,	brickBlocks[i][0]*34,	brickBlocks[i][1]*34,	brickBlocks[i][0]*34+34,	brickBlocks[i][1]*34+34,613, 171, 645, 203,null);
				gOffScreen.drawImage(img,	brickBlocks[i][0]*34+17,brickBlocks[i][1]*34,	brickBlocks[i][0]*34+51,	brickBlocks[i][1]*34+34,613, 171, 645, 203,null);
				gOffScreen.drawImage(img,	brickBlocks[i][0]*34,	brickBlocks[i][1]*34+17,brickBlocks[i][0]*34+34,	brickBlocks[i][1]*34+51,613, 171, 645, 203,null);
				gOffScreen.drawImage(img,	brickBlocks[i][0]*34+17,brickBlocks[i][1]*34+17,brickBlocks[i][0]*34+51,	brickBlocks[i][1]*34+51,613, 171, 645, 203,null);
			}
		}
		for(int i = 0;i<stoneBlocks.length;i++) {
			gOffScreen.drawImage(img,	stoneBlocks[i][0]*34,	stoneBlocks[i][1]*34,	stoneBlocks[i][0]*34+34,	stoneBlocks[i][1]*34+34,613, 171, 645, 203,null);
			gOffScreen.drawImage(img,	stoneBlocks[i][0]*34+17,stoneBlocks[i][1]*34,	stoneBlocks[i][0]*34+51,	stoneBlocks[i][1]*34+34,613, 171, 645, 203,null);
			gOffScreen.drawImage(img,	stoneBlocks[i][0]*34,	stoneBlocks[i][1]*34+17,stoneBlocks[i][0]*34+34,	stoneBlocks[i][1]*34+51,613, 171, 645, 203,null);
			gOffScreen.drawImage(img,	stoneBlocks[i][0]*34+17,stoneBlocks[i][1]*34+17,stoneBlocks[i][0]*34+51,	stoneBlocks[i][1]*34+51,613, 171, 645, 203,null);
		}
		for(int i = 0;i<waterBlocks.length;i++) {
			if(waterState%3==2) {
				gOffScreen.drawImage(img,	waterBlocks[i][0]*34,	waterBlocks[i][1]*34,	waterBlocks[i][0]*34+34,	waterBlocks[i][1]*34+34,1, 239, 33, 271,null);
			}
			else{
				gOffScreen.drawImage(img,	waterBlocks[i][0]*34,	waterBlocks[i][1]*34,	waterBlocks[i][0]*34+34,	waterBlocks[i][1]*34+34,35, 239, 68, 271,null);
			}
		}
		for(int i = 0;i<grassBlocks.length;i++) {
			gOffScreen.drawImage(img,	grassBlocks[i][0]*34,	grassBlocks[i][1]*34,	grassBlocks[i][0]*34+34,	grassBlocks[i][1]*34+34,137, 239, 169, 271,null);
		}
		waterState = (waterState+1)%3;
//
	}
	
	public static BattleMap getInstance() {
		if(instance==null) {
			instance = new BattleMap();
		}
		return instance;
	}
	
}
