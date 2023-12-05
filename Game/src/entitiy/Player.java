package entitiy;

import main.KeyHandler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import main.GamePanel;


public class Player extends Entity {
    GamePanel gp;
    KeyHandler kh;
    AnimationHandler ah;
    SpriteHandler sh;
    
    String lastdirection = "down";



    public Player(GamePanel gp, KeyHandler kh){
        this.gp = gp;
        this.kh = kh;
        setDefaultValues();
        getPlayerImage();
    }
    
    // the player's atarting position and movement behaviours
    public void setDefaultValues(){
        x = 100;
        y = 100;
        speed = 5;
        direction = "up";

    }

    //determine the state of the player by key inputs
    public void update(){
        if(kh.uppressed){
            y -= speed;
            direction = lastdirection = "up";
        }
        if(kh.downpressed){
            y += speed;
            direction = lastdirection = "down";
        }
        if(kh.leftpressed){
            x -= speed;
            direction = lastdirection = "left";
        }
        if(kh.rightpressed){
            x += speed;
            direction = lastdirection = "right";
        }
        if(!(kh.uppressed || kh.downpressed || kh.leftpressed || kh.rightpressed)){
            direction = "still";
        }

        
    }

    //display the player on graphics 2d
    public void draw(Graphics2D g2){


        BufferedImage image = null;

        switch (direction) {
            case "up":
                image = ah.getCurrentFrame("up");
                break;
            case "down":
                image = ah.getCurrentFrame("down");
                break;
            case "left":
                image = ah.getCurrentFrame("left");
                break;
            case "right":
                image = ah.getCurrentFrame("right");
                break;
            case "still":
                image = sh.getImage(lastdirection);
                break;
            default:
                g2.setColor(Color.WHITE);
                g2.fillRect(x, y, gp.TILESIZE,gp.TILESIZE);
                break;
        }

        g2.drawImage(image,x,y,gp.TILESIZE,gp.TILESIZE,null);
    }

    // initalize player displays, walking animation, still sprites
    public void getPlayerImage(){
        try{
            //walking animation
            BufferedImage ss = ImageIO.read(getClass().getResourceAsStream("/sprites/player/playerwalk.png"));
            String location = new String("src\\sprites\\player\\player.json");
            File file = new File(location);
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            JSONObject playerdata = new JSONObject(content);
            ah = new AnimationHandler(ss, playerdata);

            //standing still sprite
            BufferedImage stillss = ImageIO.read(getClass().getResourceAsStream("/sprites/player/playerstill.png"));
            location = new String("src\\sprites\\player\\playerstill.json");
            file = new File(location);
            content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            JSONObject playerstilldata = new JSONObject(content);
            sh = new SpriteHandler(stillss, playerstilldata);

            System.out.println("initalized images");

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
