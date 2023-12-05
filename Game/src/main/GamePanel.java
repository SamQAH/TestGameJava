package main;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints.Key;

import javax.swing.JPanel;

import entitiy.Player;

public class GamePanel extends JPanel implements Runnable{
    
    // screen settings
    public final int ORIGINALTILESIZE = 32;
    final int SCALE = 2;

    public final int TILESIZE = ORIGINALTILESIZE * SCALE;

    final int MAXSCREENCOL = 24;
    final int MAXSCREENROW = 32;

    final int SCREENWIDTH = MAXSCREENCOL * TILESIZE;
    final int SCREENHEIGHT = MAXSCREENROW * TILESIZE;

    int FPS = 60;

    KeyHandler kh = new KeyHandler();

    Thread gameThread;

    Player player = new Player(this, kh);


    public GamePanel(){
        this.setPreferredSize(new Dimension(SCREENWIDTH, SCREENHEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(kh);
        this.setFocusable(true);
    }

    public void startGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {

        double frameInterval = 1E9/FPS; // in nanpseconds
        double frameFraction = 0; // fraction until next frame
        long lasttime = System.nanoTime();
        long currenttime;
        long deltaTime = 1; // can be used for true speed calculations

        while(gameThread != null){
            currenttime = System.nanoTime();
            frameFraction += (deltaTime = (currenttime - lasttime)) / frameInterval;
            lasttime = currenttime;

            if(frameFraction >= 1){
                update();
                repaint();
                frameFraction--;
            }
            




        }
        
    }

    // calls appopriate methods per frame
    public void update(){
        player.update();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        player.draw(g2);
        g2.dispose();
    }
}
