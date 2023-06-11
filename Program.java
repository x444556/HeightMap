package net.ddns.x444556;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;
import javax.swing.JFrame;

public class Program extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;
	private Thread thread;
	private boolean running;
	private BufferedImage image;
	public int[] pixels;
	public boolean doSimulate = false;
	public int windowX = 800, windowY = 800;
	public int imageX = 100, imageY = 100;
	public double targetFPS = 50;
	public double[][] heightMap;
	public double maxDiff = 5.0;
	public double valueAtMousePos = 0.0;
	public double highestVal=0;
	
	public Program() {
		thread = new Thread(this);
		image = new BufferedImage(imageX, imageY, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		
		heightMap = new double[imageY][imageX];
		
		setSize(windowX, windowY);
		setResizable(false);
		setTitle("HeightMapGen");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBackground(Color.black);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	private synchronized void start() {
		running = true;
		thread.start();
	}
	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, windowX, windowY, null);
		bs.show();
	}
	private Color getHeatMapColor(double value)
	{
		double red, green, blue;
		double [][] color = { {0,0,0}, {0,0,1}, {0,1,1}, {0,1,0}, {1,1,0}, {1,0,0}, {1,1,1} };
		// A static array of n colors:  (black, blue, cyan,   green,  yellow,  red) using {r,g,b} for each.
		  
		int idx1;        // |-- Our desired color will be between these two indexes in "color".
		int idx2;        // |
		double fractBetween = 0;  // Fraction between "idx1" and "idx2" where our value is.
		  
		if(value <= 0)      {  idx1 = idx2 = 0;            }    // accounts for an input <=0
		else if(value >= 1)  {  idx1 = idx2 = 5; }    // accounts for an input >=0
		else
		{
			value = value * (5);        // Will multiply value by 3.
			idx1  = (int) Math.floor(value);                  // Our desired color will be after this index.
			idx2  = idx1+1;                        // ... and before this index (inclusive).
			fractBetween = value - idx1;    // Distance between the two indexes (0-1).
		}
		
		red   = (color[idx2][0] - color[idx1][0])*fractBetween + color[idx1][0];
		green = (color[idx2][1] - color[idx1][1])*fractBetween + color[idx1][1];
		blue  = (color[idx2][2] - color[idx1][2])*fractBetween + color[idx1][2];
		
		return new Color((float)red, (float)green, (float)blue);
	}
	private Color getHeatMapColorBlackAndWhite(double value) {
		float v = (float) ((value + maxDiff) / (maxDiff * 2));
		Color c = new Color(v, v, v);
		return c;
	}
	private void simToImg() {
		for(int y=0; y<imageY; y++) {
			for(int x=0; x<imageX; x++) {
				System.out.println(heightMap[y][x]);
				pixels[y * imageX + x] = getHeatMapColorBlackAndWhite(heightMap[y][x]).getRGB();
			}
		}
	}
	private void genHeightMap(int seed) {
		Random rnd = new Random(seed);
		double[][] tempHM = new double[imageY][imageX];
		for(int y=0; y<imageY; y++) {
			for(int x=0; x<imageX; x++) {
				double c = (rnd.nextDouble() * maxDiff * 2 - maxDiff);
				tempHM[y][x] = c;
			}
		}
		for(int y=0; y<imageY; y++) {
			for(int x=0; x<imageX; x++) {
				double total = tempHM[y][x];
				int count = 1;
				if(y > 0) {
					total += tempHM[y - 1][x];
					count++;
					if(x > 0) {
						total += tempHM[y - 1][x - 1];
						count++;
					}
					if(x < imageX - 1) {
						total += tempHM[y - 1][x + 1];
						count++;
					}
				}
				if(y < imageY - 1) {
					total += tempHM[y + 1][x];
					count++;
					if(x > 0) {
						total += tempHM[y + 1][x - 1];
						count++;
					}
					if(x < imageX - 1) {
						total += tempHM[y + 1][x + 1];
						count++;
					}
				}
				if(x > 0) {
					total += tempHM[y][x - 1];
					count++;
				}
				if(x < imageX - 1) {
					total += tempHM[y][x + 1];
					count++;
				}
				heightMap[y][x] = total / count;
			}
		}
	}
	public void run() {
		requestFocus();
		genHeightMap((int) (Math.random()*Integer.MAX_VALUE));
		render();
		simToImg();
		render();
	}
	
	
	public static void main(String [] args) {
		Program sim = new Program();
		sim.start();
	}
}
