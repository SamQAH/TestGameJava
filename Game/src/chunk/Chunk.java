package chunk;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import Utility.Noise2D;
import main.GamePanel;
import tile.FoliageManager;
import tile.TileSet;
import tile.TileManager;


/*
 * Each chunk is responsible for:
 * Generating itself from a seed value
 * Storing its information to a file

 */
public class Chunk{
    GamePanel gp;
    int[] id; // [x,y] cooridinate with starting screen being 0 , 0 and each subsequent chunk id incrementing by 1, corresponding to chunkSize number of chunks
    public static final int[] chunkSize = new int[] {32,32}; // number of tiles horizontaly and verticaly, NEVER CHANGE, I CONFUSED X AND Y FREQUENTLY 
    String fileName; // stored in /src/chunk/chunk_data/??.txt
    TileManager tm;
    FoliageManager fm;
    File file;

    int[][] tileData;
    int[][] foliageData;

    private static double octave = 0.5;
    private static int frequency = 2;
    private static int detail = 5;
    private static double noiseScale = 0.005;

    // private static HashMap<Integer,CardinalDirections> TileBlendingRules;
    // static { TileBlendingRules = new HashMap<>();
    //         TileBlendingRules.put()
    //         }

    public static double getTileValue(int[] coordGlobal){
        double value = 0;
        for(int i = 0; i<detail; i++){
            value += Noise2D.getValue(coordGlobal[0]*(int)Math.pow(frequency,i)*noiseScale, coordGlobal[1]*(int)Math.pow(frequency,i)*noiseScale)*Math.pow(octave,i);
        }
        return value;
    }

    public static int getTileID(int[] coordGlobal){
        double value = getTileValue(coordGlobal);
        if(value > 0){
            return 0;
        }else if(value > -0.2){
            return 9;
        }else{
            return 1;
        }
    }

    public int[] convertGlobal(int[] coords){
        int[] temp = new int[2];
        temp[0] = coords[0] + id[0] * chunkSize[0];
        temp[1] = coords[1] + id[1] * chunkSize[1];
        return temp;
    }

    public int[] convertGlobal(int row, int col){
        int[] temp = new int[2];
        temp[0] = row + id[0] * chunkSize[0];
        temp[1] = col + id[1] * chunkSize[1];
        return temp;
    }

    public static TileSet getBasicBlendedTile(int[] coordGlobal){
        int row = coordGlobal[0];
        int col = coordGlobal[1];

        CardinalDirections cd = new CardinalDirections(getTileID(new int[]{row,col-1}), getTileID(new int[]{row+1,col}), getTileID(new int[]{row,col+1}), getTileID(new int[]{row-1,col}));
        return OceanBlendRules.findBlendedTile(cd);
    }


    public Chunk(GamePanel gp, int[] coord, int seed){
        this.gp = gp;
        id = coord;
        fileName = "src/chunk/chunk_data/chunk_"+id[0]+"_"+id[1]+".txt";
        file = new File(fileName);

        // if(!file.exists()){
        //     this.generateChunk(seed);
        // } TODO remove when stop changing generate chunk function
        this.generateChunk(seed);
        this.blendTiles();
        this.writeData();
        this.parseData();
        

        fm = new FoliageManager(gp, foliageData);
        tm = new TileManager(gp, tileData);

    }
    
    public void generateChunk(int seed){
        /*
         * calculate the chunk type:
         * plains
         * ocean
         * beach
         * ocean beach vertical blend
         * 
         * write tiles
         * write foliage
         */
        
        Random random = new Random(seed);
        int[] currentGlobalCoord = new int[2];
        currentGlobalCoord[0] = id[0] * Chunk.chunkSize[0];
        currentGlobalCoord[1] = id[1] * Chunk.chunkSize[1];
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

            writeTileString(random, bufferedWriter,currentGlobalCoord);
            bufferedWriter.write("section break\n");
            writeFoliage(random, bufferedWriter);
            bufferedWriter.close();

        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("generated chunk");


    }

    private void writeFoliage(Random random, BufferedWriter bufferedWriter){
        foliageData = new int[chunkSize[0]][chunkSize[1]];
        int row = 0;
        int col = 0;
        String data = "";
        while(col<chunkSize[1]){
            int num;
            switch(tileData[col][row]){
                case 0:
                num = random.nextInt(20);
                break;
                default:
                num = -1;
                break;
            }
            foliageData[col][row] = num;
            data += num + " ";
            row++;
            if(row == chunkSize[0]){
                row = 0;
                col++;
                data += "\n";
            }
        }
        try{
            bufferedWriter.write(data);
        }catch(Exception e){
            e.printStackTrace();
        }



    }

    private void writeTileString(Random random, BufferedWriter bufferedWriter, int[] globalCoords){
        tileData = new int[chunkSize[0]][chunkSize[1]];
        String data = "";
        int row = 0;
        int col = 0;
        while(col<chunkSize[1]){
            int tile = getTileID(globalCoords);
            data += tile+" ";
            tileData[col][row] = tile;
            row++;
            globalCoords[0]++;
            if(row == chunkSize[0]){
                row = 0;
                col++;
                globalCoords[0] -= chunkSize[0];
                globalCoords[1]++;
                data += "\n";
            }
        }
        try{
            bufferedWriter.write(data);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void blendTiles(){
        /*find all water tiles and check the four cardinal directions
         *repeat but look for alreay replaced tiles
         */
        int row = 0;
        int col = 0;
        HashMap<int[], Integer> requestedChanges = new HashMap<>();
        while(col < chunkSize[1]){
            /* only for ocean tiles so far
             * basic blending, only look at basic tiles like grass, ocean, sand
             * if on the edge of the current chunk, get tile from gettile()
             * else get values from tiledata
             * 
             * advanced blending, looks at adjacent tiles' sides through info from TileSet, blend already blended tiles
             * if there is 2+ blended tiles around an unblended tile
             * Find important tiles around requested changes
             */
            if(tileData[col][row] == 1){
                CardinalDirections cd;
                if(row == 0 || row == chunkSize[0]-1 || col == 0 || col == chunkSize[1]-1){
                    cd = new CardinalDirections(getTileID(convertGlobal(row,col-1)), getTileID(convertGlobal(row+1,col)), getTileID(convertGlobal(row,col+1)), getTileID(convertGlobal(row-1,col)));
                }else{
                    cd = new CardinalDirections(tileData[col-1][row], tileData[col][row+1], tileData[col+1][row], tileData[col][row-1]);

                }
                int index = OceanBlendRules.findBlendedTile(cd).index;
                if(index != 1){
                    requestedChanges.put(new int[]{row,col},index);
                }
            }
            

            row++;
            if(row == chunkSize[0]){
                row = 0;
                col++;
            }
        }
        //TODO switch to Utility coordinateset
        Set<int[]> coordSet = requestedChanges.keySet();
        ArrayList<int[]> overLap = new ArrayList<>();
        ArrayList<int[]> currentAdjacent = new ArrayList<>();
        for(int[] coord : coordSet){
            tileData[coord[1]][coord[0]] = requestedChanges.get(coord);
            currentAdjacent.add(new int[]{coord[0]+1,coord[1]});
            currentAdjacent.add(new int[]{coord[0]-1,coord[1]});
            currentAdjacent.add(new int[]{coord[0],coord[1]+1});
            currentAdjacent.add(new int[]{coord[0],coord[1]-1});
        }
        // remove the coords that are already in coordSet
        for(int i = 0; i < currentAdjacent.size(); i++){
            for(int[] coord : coordSet){
                if(Arrays.equals(currentAdjacent.get(i),coord)){
                    currentAdjacent.remove(i);
                    break;
                }
            }
        }

        // finds duplicates and puts them in overlap
        ArrayList<int[]> temp = new ArrayList<>();
        for(int[] coord : currentAdjacent){
            if(temp.contains(coord) && !overLap.contains(coord)){
                overLap.add(coord);
            }else{
                temp.add(coord);
            }
        }
        HashMap<int[],Integer> newrequest = new HashMap<>();
        HashMap<Integer,Integer> adj;
        for(int[] coord : overLap){
            adj = new HashMap<>();
            for(int[] coords : coordSet){
                if(coord[1]-1 == coords[1]){
                    adj.put(0,requestedChanges.get(coords));
                }else if(coord[0]+1 == coords[0]){
                    adj.put(1,requestedChanges.get(coords));
                }else if(coord[1]+1 == coords[1]){
                    adj.put(2,requestedChanges.get(coords));
                }else if(coord[0]-1 == coords[0]){
                    adj.put(3,requestedChanges.get(coords));
                }
            }
            newrequest.put(coord,TileSet.findBySide(adj).index);
        }

        for(int[] coord : newrequest.keySet()){
            tileData[coord[1]][coord[0]] = newrequest.get(coord);
        }

        
        
    }



    public void parseData(){
        tileData = new int[chunkSize[0]][chunkSize[1]];
        foliageData = new int[chunkSize[0]][chunkSize[1]];
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            int counter = 0; //section counter
            int colCounter = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.equals("section break")){
                    counter++;
                    colCounter = 0;
                    continue;
                }
                int rowCounter = 0;
                int[] tempRow = new int[chunkSize[0]];
                for(String s : line.strip().split(" ")){
                    tempRow[rowCounter] = Integer.parseInt(s);
                    rowCounter++;
                }
                switch(counter){
                    case 0:
                    tileData[colCounter] = tempRow;
                    break;
                    case 1:
                    foliageData[colCounter] = tempRow;
                    break;
                    default:
                    break;
                }
                colCounter++;
                

            }
            bufferedReader.close();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public int[][] getTileData(){
        return tileData;
    }

    public int[][] getFoliageData(){
        return foliageData;
    }

    public void draw(Graphics2D g2){
        tm.draw(g2, id, gp.frameCounter);
        fm.draw(g2, id);
    }

    public void writeData(){
        String data = "";
        int row = 0;
        int col = 0;
        while(col<chunkSize[1]){
            data += tileData[col][row]+" ";
            row++;
            if(row == chunkSize[0]){
                row = 0;
                col++;
                data += "\n";
            }
        }
        data += "section break\n";
        row = 0;
        col = 0;
        while(col<chunkSize[1]){
            data += foliageData[col][row]+" ";
            row++;
            if(row == chunkSize[0]){
                row = 0;
                col++;
                data += "\n";
            }
        }

        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(data);
            bufferedWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}

class CardinalDirections{
    int up, down, left, right;

    public CardinalDirections(int up, int right, int down, int left){
        this.up = up;
        this.right = right;
        this.down = down;
        this.left = left;
    }

    public boolean equals(CardinalDirections cd){
        if(cd.up == this.up && cd.right == this.right && cd.down == this.down && cd.left == this.left){
            return true;
        }
        return false;
    }

    public boolean equals(int up, int right, int down, int left){
        if(up == this.up && right == this.right && down == this.down && left == this.left){
            return true;
        }
        return false;
    }

}

enum OceanBlendRules{
    blendup (TileSet.oceanblendup, 9,1,1,1),
    blendright (TileSet.oceanblendright, 1,9,1,1),
    blenddown (TileSet.oceanblenddown, 1,1,9,1),
    blendleft (TileSet.oceanblendleft, 1,1,1,9),
    blendinsideupright (TileSet.oceanblendinsideupright, 9,9,1,1),
    blendinsideupleft (TileSet.oceanblendinsideupleft, 9,1,1,9),
    blendinsidedownleft (TileSet.oceanblendinsidedownleft, 1,1,9,9),
    blendinsidedownright (TileSet.oceanblendinsidedownright, 1,9,9,1),
    blendinside (TileSet.oceanblendinside, 9,9,9,9);

    public final CardinalDirections ruleSet;
    public final TileSet label;

    private OceanBlendRules(TileSet l, int up, int right, int down, int left){
        ruleSet = new CardinalDirections(up, right, down, left);
        label = l;
    }

    public static TileSet findBlendedTile(CardinalDirections cd){
        //default returns ocean
        for(OceanBlendRules obr : OceanBlendRules.values()){
            if(cd.equals(obr.ruleSet)){
                return obr.label;
            }
        }
        return TileSet.openocean;
    }


}