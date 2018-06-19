package com.example.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Random;

/*
 * 生成验证码图片
 */
public class ImageCode {
	// 图片宽度
	private int width = 100;
	// 图片高度
	private int height = 30;

	private Random random = new Random();

	// 生成随机颜色 fc-前景色 bc-后景色
	public Color getColor(int fc, int bc) {
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	// 生成干扰线
	public void drawLine(Graphics2D g, int num) {
		g.setColor(this.getColor(100, 200));
		for (int i = 0; i < num; i++) {
			int x1 = random.nextInt(width);
			int y1 = random.nextInt(height);
			int x2 = random.nextInt(10);
			int y2 = random.nextInt(10);
			g.drawLine(x1, y1, x2, y2);
		}
	}

	// 生成验证码
	public String drawString(Graphics2D g, int num) {
		StringBuffer sb = new StringBuffer();
		String str = "";
		int no = 0;
		for (int i = 0; i < num; i++) {
			switch (random.nextInt(3)) {
			case 1:
				no = random.nextInt(26) + 65; // A-Z
				str = String.valueOf((char) no);
				break;
			case 2:
				no = random.nextInt(26) + 97; // a-z
				str = String.valueOf((char) no);
				break;
			default:
				no = random.nextInt(10) + 48;
				str = String.valueOf((char) no);
				break;
			}
			Color color = new Color(20 + random.nextInt(20), 20 + random.nextInt(20), 20 + random.nextInt(20));
			g.setColor(color);
			// 想文字旋转一定的角度
			AffineTransform trans = new AffineTransform();
			trans.rotate(random.nextInt(45) * 3.14 / 180, 15 * i + 8, 7);
			// 缩放文字
			float scaleSize = random.nextFloat() + 0.8f;
			if (scaleSize > 1f)
				scaleSize = 1f;
			trans.scale(scaleSize, scaleSize);
			g.setTransform(trans);
			g.drawString(str, 15 * i + 18, 14);

			sb.append(str);
		}
		g.dispose();
		return sb.toString();
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}
	
}
