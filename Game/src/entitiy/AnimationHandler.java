package entitiy;

import java.awt.image.BufferedImage;

import org.json.JSONObject;

import java.util.HashMap;

/* stores a buffered image containing frames of animation for cyclic purposees,
 * gets the current frame of the animation from a String key to a json file
 * stores how many frames each animation key has been called with hashmap, does not reset counter
 * assumes each frame is aligned vertically and goes down by height
 */
public class AnimationHandler {
    BufferedImage spritesheet;


    HashMap<String,Integer> counter;

    JSONObject metadata;


    public AnimationHandler(BufferedImage ss, JSONObject jsonobj){
        spritesheet = ss;
        metadata = jsonobj;
        counter = new HashMap<>();
        for(String k : metadata.keySet()){
            counter.put(k,0);
        }
    }

    public BufferedImage getCurrentFrame(String key){
        JSONObject currentdata = metadata.getJSONObject(key);
        int numberOfFrames = currentdata.getInt("frames");
        int animationTime = currentdata.getInt("frame delay");
        int startx = currentdata.getInt("x");
        int starty = currentdata.getInt("y");
        int width = currentdata.getInt("width");
        int height = currentdata.getInt("height");

        int counternum = counter.get(key).intValue();
        counternum++;
        counter.replace(key, counternum);
        BufferedImage image = spritesheet.getSubimage(startx, starty + height * (counternum/animationTime % numberOfFrames), width, height);;
/* 
        switch(currentdata.getString("direction")){
            case "down":
                image = spritesheet.getSubimage(startx, starty + height * (counternum/animationTime % numberOfFrames), width, height);
                break;
            case "right":
                image = spritesheet.getSubimage(startx + width * (counternum/animationTime % numberOfFrames), starty, width, height);
                break;
            default:
                image = spritesheet.getSubimage(0,0,1,1);
                System.out.println("unsupported direction of spritesheet animation");
                break;
        }*/
        return image;
    }
}
