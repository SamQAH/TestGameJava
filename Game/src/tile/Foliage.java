package tile;

import java.awt.Graphics2D;

import entitiy.AnimationHandler;

public class Foliage {

    int xoffset;
    int yoffset;
    AnimationHandler ah;
    String id; // what it's called in the json
    int counter = 0;

    public Foliage(AnimationHandler ah , String id){
        this.ah = ah;
        xoffset = (int)(Math.random()*20-10);
        yoffset = (int)(Math.random()*20-10);
        this.id = id;
    }

    public void draw(Graphics2D g2, int screenx, int screeny){
        g2.drawImage(ah.getCurrentFrame(id, counter), screenx+xoffset,screeny+yoffset,64,64, null);//reference by number, change later
        counter++;
    }
}
