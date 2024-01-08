package Utility;

public class CollisionBox{
  private static final double margin = 16; // true margin / 2
  int width, height, globalx, globaly, widthPartisions, heightPartision;
  int[][] mesh;
  public CollisionBox(int width, int height, int x, int y){
    this.width = width;
    this.height = height;
    globalx = x;
    globaly = y;
    setMesh();
  }

  public void setMesh(){
    /*  0--->  coords around the perimeter
     *  T   |
     *  |   v
     */ <----
    widthPartisions = Math.ceil(width / margin); // should be always >= 1
    heightPartisions = Math.ceil(heigh / margin);
    mesh = new int[2 * (widthPartisions + heightPartisions)][2];
    int[] currentCoords = new int[]{globalx, globaly};
    int counter = 0;
    for(int i = 0; i < widthPartision; i++){
      mesh[counter] = currentCoords.clone();
      currentCoords[0] += margin;
      counter ++;
    }
    currentCoords[0] = width + globalx;
    for(int i = 0; i < heightPartision; i++){
      mesh[counter] = currentCoords.clone();
      currentCoords[1] += margin;
      counter ++;
    }
    currentCoords[1] = height + globaly;
    for(int i = 0; i < widthPartision; i++){
      mesh[counter] = currentCoords.clone();
      currentCoords[0] -= margin;
      counter ++;
    }
    currentCoords[0] = globalx;
    for(int i = 0; i < heightPartision; i++){
      mesh[counter] = currentCoords.clone();
      currentCoords[1] -= margin;
      counter ++;
    }
    
  }

  public void move(int x, int y){
    globalx += x;
    globaly += y;
  }

  //returns [up, right, down, left]
  public boolean[] checkCollide(CollisionBox cb){
    for(int i = 0; i < mesh.length; i++){
      if(cb.checkCollide(mesh[i][0], mesh[i][1])){
        //
        if(0 <= i && i <= widthPartision+1 && widthPartision + heightParsition +1 <= i && i <= 2*widthPartision + heightParsition +1){
//TODO fix
        }
      }
    }
  }
  public boolean checkCollide(int x, int y){
    if(globalx <= x && x <= globalx + length && globaly <= y && y <= globaly + height){
      return true;
    }
    return false;
  }
}

