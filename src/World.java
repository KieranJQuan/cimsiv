/**
 * CHANGELOG
 * -Implemented world generation
 * -Mating under work, fixes appreciated, bugs aplenty
 * Added Hunger mechanic, 0 hunger will kill you
 * Forests now grant 4 hunger
 * Mountains slow movement and consume 1 extra hunger
 * Plains are, well plain (yea fiteme kieran)
 * Implemented fixes discussed on friday during class
 * Grouping mechanic for Humans, they will converge on specified point, with collision detection, so no overlap
 * Jan 14, 2018
 * Made demon class movement, see below for details
 * Added follow method (modified and put in organism)
 * For convenience, change lifesimulation window time to run faster (delay 100 instead of 1000)
 */

//todo
//interactions for eating, make food dissappear
//interactions with each other
//mating with 2

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

// World class
// -----------
// includes three grids (a grid of Animals on top of a grid of Plants on top of a grid of Regions)
// can be updated by calling step, which will iterate through all three grids and call their step method
// position is sent into method so that each tile of the grid can check nearby Organisms or Environments
class World {
    // WorldDisplay class
    // ------------------
    // exists within an instance of a World class, and is a JPanel to represent the three grids
    // shows Animals in the front on top of Plants on top of Regions

    static Random ran = new Random();

    class WorldDisplay extends JPanel {
        WorldDisplay(int width, int height) {
            setPreferredSize(new Dimension(width, height));
        }

        public void paintComponent(Graphics g) {
            int width = getWidth();
            int height = getHeight();
            paintGrid(g, back, width, height);
            paintGrid(g, plants, width, height);
            paintGrid(g, orgs, width, height);
        }

        private void paintGrid(Graphics g, Tile[][] tiles, int width, int height) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (tiles[i][j] != null) {
                        g.drawImage(tiles[i][j].sprite, i * width / size, j * height / size, width * tiles[i][j].imageSize / size, height * tiles[i][j].imageSize / size, null);
                    }
                }
            }
        }
    }

    // Tile class
    // ----------
    // abstract class extended by all things, used for images I guess
    // although Plants and Animals differ from Regions, they both need to be displayed, so an image is loaded regardless
    private abstract class Tile {
        private BufferedImage sprite;
        protected int imageSize;

        protected Tile() {
            imageSize = 1;
            try {
                sprite = ImageIO.read(new File(this.getClass().getSimpleName() + ".png"));
            } catch (IOException iOExc) {
                sprite = null;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private int size; // size variable only refers to WORLD
    private Region[][] back;  //background tiles, EG: Mountain, sand
    private Plant[][] plants;  // plants, EG bushes, trees,includes rocks
    private Organism[][] orgs; // moving organisms, demons, knights
    private ArrayList<Organism> orgList = new ArrayList<Organism>();
    private ArrayList<Plant> plantList = new ArrayList<Plant>();
    int turn = 0; // keeps track of turn number, starts at 09, useful for keeping track of speed using % operator

    public World(int s) {
        size = s;
        back = new Region[size][size];
        plants = new Plant[size][size];
        orgs = new Organism[size][size];
        //put stuff into the arrays
        //-------------------------
        //temp test

        //world generation

        //Create broad biomes in 3x3 grid
        int roll;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Region region;
                roll = (int) (Math.random() * 100);
                if (i == 0) {
                    if (j == 0) {
                        if (roll < 40) {
                            region = new Plain();
                        } else if (roll < 75) {
                            region = new Forest();
                        } else {
                            region = new Mountain();
                        }
                    } else {
                        if (roll < 50) {
                            region = back[i][j - 1];
                        } else if (roll < 70) {
                            region = new Plain();
                        } else if (roll < 88) {
                            region = new Forest();
                        } else {
                            region = new Mountain();
                        }
                    }
                } else {
                    if (j == 0) {
                        if (roll < 50) {
                            region = back[i - 1][j];
                        } else if (roll < 70) {
                            region = new Plain();
                        } else if (roll < 88) {
                            region = new Forest();
                        } else {
                            region = new Mountain();
                        }
                    } else {
                        if (roll < 40) {
                            region = back[i][j - 1];
                        } else if (roll < 80) {
                            region = back[i - 1][j];
                        } else if (roll < 88) {
                            region = new Plain();
                        } else if (roll < 95) {
                            region = new Forest();
                        } else {
                            region = new Mountain();
                        }
                    }
                }
                back[i][j] = region;
            }
        }


    }

    //step, turn ends, everyone will update call move
    protected void step() {
        turn++; // increment turn, new turn starts every step ;
        // System.out.println(orgList.size());
        for (int i = 0; i < orgList.size(); i++) {

            if (turn % orgList.get(i).speed == 0) {// check if its speed allows it to move this turn
                orgs[orgList.get(i).posX][orgList.get(i).posY] = null; // delete from original
                int oldSize = orgList.size();
                orgList.get(i).move(); // move
                if (oldSize != orgList.size() + 1) {
                    orgs[orgList.get(i).posX][orgList.get(i).posY] = orgList.get(i); // re add to new spot
                }
                //interactions and codestuffs can come in here

            }
        }

        //plants
        int numPlants = plantList.size();
        for (int i = 0; i < numPlants; i++) {
            plantList.get(i).step();
            if (plantList.size() != numPlants) {
                i += plantList.size() - numPlants;
                numPlants = plantList.size();
            }
        }

    }

    void freeRemove(int y, int x) {
        if (x > -1 && x < size && y > -1 && y < size) {
            if (orgs[x][y] != null) {
                orgList.remove(orgList.indexOf(orgs[x][y]));
                orgs[x][y] = null;
            }
            if (plants[x][y] != null) {
                plantList.remove(plantList.indexOf(plants[x][y]));
                plants[x][y] = null;
            }
        }
    }

    void freeAdd(String name, int y, int x) {
        if (x > -1 && x < size && y > -1 && y < size) {
            Object obj;
            if (orgs[x][y] == null) {
                if (name.equals("Human")) {
                    addOrg(new Human(), x, y);
                } else if (name.equals("Demon")) {
                    addOrg(new Demon(), x, y);
                } else if (name.equals("Fox")) {
                    addOrg(new Fox(), x, y);
                } else if (name.equals("Knight")) {
                    addOrg(new Knight(), x, y);
                } else if (name.equals("Mage")) {
                    addOrg(new Mage(), x, y);
                } else if (name.equals("Wolf")) {
                    addOrg(new Wolf(), x, y);
                }
            }
            if (plants[x][y] == null) {
                if (name.equals("Corn")) {
                    addPlant(new Corn(), x, y);
                } else if (name.equals("Dandelion")) {
                    addPlant(new Dandelion(), x, y);
                } else if (name.equals("MapleTree")) {
                    addPlant(new MapleTree(), x, y);
                } else if (name.equals("PineTree")) {
                    addPlant(new PineTree(), x, y);
                } else if (name.equals("Rose")) {
                    addPlant(new Rose(), x, y);
                }
            }
        }
    }

    void orgReset() {
        orgs = new Organism[size][size];
        orgList = new ArrayList<Organism>();
    }

    void plantReset() {
        plants = new Plant[size][size];
        plantList = new ArrayList<Plant>();
    }

    public int getSize() {
        return size;
    }

    //easy way to add an organism
    protected void addOrg(Organism o, int x, int y) {

        o.posX = x;
        o.posY = y;
        orgList.add(o);
        orgs[x][y] = o;
    }

    //addPlants
    private void addPlant(Plant p, int x, int y) {
        p.posX = x;
        p.posY = y;
        plantList.add(0, p);
        plants[x][y] = p;
    }

    protected void print() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (orgs[j][i] == null) {
                    System.out.print(0);
                } else {
                    System.out.print(1);
                }
            }
            System.out.println();
        }
    }


    // 3 big Abstracts, extends tile for image loading

    //animals
    private abstract class Organism extends Tile {
        int posX, posY; //position
        int health, str; // stats
        int hunger, speed; //mo stats
        boolean mating; //sex

        //determine starting location
        protected Organism(int x, int y) {
            posX = x;
            posY = y;
        }

        protected Organism() {
            posX = 0;
            posY = 0;
        }

        //what an organism can do
        protected void move() {
            int dir = ran.nextInt(4); // determines direction
            if (dir == 0 && posX +1  < orgs.length)//movement
                posX++;
            if (dir == 1 && posX > 0)
                posX--;
            if (dir == 2 && posY +1 < orgs.length)
                posY++;
            if (dir == 3 && posY > 0)
                posY--;
            hunger--;//loses a hunger after moving, interactions with hunger dealt with in array, World Class
            death();
        }

        protected void follow(int x, int y) {//moves towards this point

            if (posX > x && posX - 1 >= 0) {

                if (orgs[posX - 1][posY] == null) {
                    posX--;
                }
            }
            if (posX < x && posX + 1 < orgs.length) {
                if (orgs[posX + 1][posY] == null) {
                    posX++;
                }
            }
            if (posY > y && posY - 1 >= 0) {
                if (orgs[posX][posY - 1] == null) {
                    posY--;
                }
            }
            if (posY < y && posY + 1 < orgs.length) {
                if (orgs[posX][posY + 1] == null) {
                    posY++;
                }
            }

        }

        //movement methods

        protected boolean canMoveRight() {
            if (posX + 1 < orgs.length) {
                if (orgs[posX + 1][posY] == null) {
                    return true;
                }
            }

            return false;
        }

        protected boolean canMoveDown() {
            if (posY + 1 < orgs.length) {
                if (orgs[posX][posY + 1] == null) {
                    return true;
                }
            }

            return false;
        }

        protected boolean canMoveLeft() {
            if (0 < posX) {
                if (orgs[posX - 1][posY] == null) {
                    return true;
                }
            }

            return false;
        }

        protected boolean canMoveUp() {
            if (0 < posY) {
                if (orgs[posX][posY - 1] == null) {
                    return true;
                }
            }

            return false;
        }

        protected boolean canMoveUpRight() {
            if (posX + 1 < orgs.length && 0 < posY) {
                if (orgs[posX + 1][posY - 1] == null) {
                    return true;
                }
            }

            return false;
        }

        protected boolean canMoveUpLeft() {
            if (0 < posX && 0 < posY) {
                if (orgs[posX - 1][posY - 1] == null) {
                    return true;
                }
            }

            return false;
        }

        protected boolean canMoveDownRight() {
            if (posX + 1 < orgs.length && posY + 1 < orgs.length) {
                if (orgs[posX + 1][posY + 1] == null) {
                    return true;
                }
            }

            return false;
        }

        protected boolean canMoveDownLeft() {
            if (0 < posX && posY + 1 < orgs.length) {
                if (orgs[posX - 1][posY + 1] == null) {
                    return true;
                }
            }

            return false;
        }

        protected void eat(int val) { // val is food value, EG berry food value is 2, val == 2
            //refill hunger, eat food
            hunger += val;
        }

        protected void mate(Organism o) {//create a new Organism, implementation in specific classes

            if(mating) {

                if (posX + 1 < orgs.length) {
                    if (orgs[posX + 1][posY] == null) {
                        addOrg(o, posX + 1, posY);
                    }
                    mating = false;
                    //else unlucky
                } else {
                    if (orgs[posX - 1][posY] == null) {
                        addOrg(o, posX - 1, posY);
                    }
                    mating = false;
                    //else unlucky
                }
            }
        }
        protected boolean death() {   // just checks if they died
            if ( hunger <= 0||health<=0) {
                orgs[posX][posY] = null;
                orgList.remove(orgList.indexOf(this));
                return true;
            }
            return false;
        }

        //check if there's organisms around
        protected boolean prox(Organism o) {
            boolean check = false;
            if (posX + 1 < orgs.length) {
                if (orgs[posX + 1][posY] != null)
                    if (orgs[posX + 1][posY].getClass() == o.getClass()) {
                        check = true;
                    }
            }
            if (posX - 1 >= 0) {
                if (orgs[posX - 1][posY] != null)
                    if (orgs[posX - 1][posY].getClass() == o.getClass()) {
                        check = true;
                    }
            }
            if (posY + 1 < orgs.length) {
                if (orgs[posX][posY + 1] != null)
                    if (orgs[posX][posY + 1].getClass() == o.getClass()) {
                        check = true;
                    }
            }
            if (posY - 1 >= 0) {
                if (orgs[posX][posY - 1] != null)
                    if (orgs[posX][posY - 1].getClass() == o.getClass()) {
                        check = true;
                    }
            }
            return check;
        }


    }

    //plants
    private abstract class Plant extends Tile {
        protected int posX, posY;
        protected int maturity, lifeSpan;
        protected int forestRate, plainRate, mountainRate;

        protected Plant() {
            maturity = 0;
        }

        protected void step() {
            maturity++;
            reproduce();
            death();
        }

        protected abstract void reproduce();

        protected void death() {
            int randomRoll = (int) (Math.random() * 100);
            if (back[posX][posY] instanceof Forest && randomRoll >= forestRate) {
                die();
            } else if (back[posX][posY] instanceof Plain && randomRoll >= plainRate) {
                die();
            } else if (back[posX][posY] instanceof Mountain && randomRoll >= mountainRate) {
                die();
            } else if (maturity >= lifeSpan) {
                die();
            }
        }

        protected void die() {
            plants[posX][posY] = null;
            plantList.remove(plantList.indexOf(this));
        }
    }

    //tiles
    private abstract class Region extends Tile {
    }

    //Nuances and smol classes

    /**TEST CLASSES **/

    private class Human extends Organism {
        //constructor
        protected Human(int x, int y) {
            super(x, y);
            health = 3; //cam take 3 damage
            str = 1;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 10; // counts down to 0, then dies
            speed = 1; // speed, every 1 turn will move
            mating = true; //not yet
        }

        protected Human() {
            super(0,0);
            health = 3; //cam take 3 damage
            str = 1;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 10; // counts down to 0, then dies
            speed = 1; // speed, every 1 turn will move
            mating = true; //not yet
        }

        protected void move() {
            super.move();

            //next tile will feel the effects
            if(back[posX][posY] instanceof Mountain){ // longer trek time, more hunger loss
                speed =3;
                hunger--;
            }else if(back[posX][posY] instanceof Plain){  // pretty plain hehe
                speed =1;
            }else{ // rejuvenate at forest
                speed = 1;

            }

            if(plants[posX][posY] instanceof Rose){
                mating = true;
                mate(new Human());
            }
            if(plants[posX][posY] instanceof Corn){
                eat(10);
            }
            if(plants[posX][posY] instanceof Dandelion){
                eat(1);
                if ((int)(Math.random()*2)==0){
                    mating = true;
                }
            }

            //random chance of mating
            // if((int)(Math.random()*4)==0)

            if(hunger<=5){//only mates when ready, for some reason thats when humans hungry
                mate(new Human());
            }




        }




    }
    //nu imag fel bad mun


    //TILE-AGE
    private class Forest extends Region {
    }

    private class Plain extends Region {
    }

    private class Mountain extends Region {
    }

    //REAL ONES
    //porganisms
    //REAL ONES
    private class Demon extends Organism {

        // Variables

        // ritual count will make all demons move to a point every once in a while
        private int ritual = 0;
        // path will choose the path the demon takes after the ritual or when it is spawned
        private int path = ran.nextInt(4);

        // Constructor

        protected Demon(int x, int y) {
            super(x, y);
            health = 100; //cam take 3 damage
            str = 25;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 1000; // counts down to 0, then dies
            speed = 1; // speed, every 1 turn will move
            mating = true; //not yet
        }

        protected Demon() {
            super();
            health = 100; //cam take 3 damage
            str = 4;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 1000; // counts down to 0, then dies
            speed = 1; // speed, every 1 turn will move
            mating = true; //not yet
        }


        // Methods

        protected void move() {
            // Add to ritual
            ritual++;

            // Ritual pattern
            if (ritual >= 100) {
                // Join together at center
                follow(size / 2, size / 2);

                // Reset ritual count when finished
                if (ritual == 120) {
                    ritual = 0;
                }

            }

            // Non-Ritual pattern
            else {

                // If the demon is on the border, move clockwise
                if (posX == 0 || posX == size - 1 || posY == 0 || posY == size - 1) {
                    moveClockwise();
                }

                // If the demon is not on the border, move until the demon reaches the border
                else {
                    // If a demon hits an object, recalculate path
                    if (path == 0)
                        if (canMoveRight()) {
                            posX++;
                        } else {
                            path = ran.nextInt(4);
                        }
                    if (path == 1)
                        if (canMoveLeft()) {
                            posX--;
                        } else {
                            path = ran.nextInt(4);
                        }
                    if (path == 2)
                        if (canMoveDown()) {
                            posY++;
                        } else {
                            path = ran.nextInt(4);
                        }
                    if (path == 3)
                        if (canMoveUp()) {
                            posY--;
                        } else {
                            path = ran.nextInt(4);
                        }


                }
            }
            //uses some hunger
            this.hunger--;
            death();

        }

        private void moveClockwise() {
            // If the demon is on the left side
            if (posX == 0) {
                if (posY == 0) {
                    if (canMoveRight()) {
                        posX++;
                    }
                } else {
                    if (canMoveUp()) {
                        posY--;
                    }
                }
            }
            // If the demon is on the top side
            else if (posY == 0) {
                if (posX == size - 1) {
                    if (canMoveDown()) {
                        posY++;
                    }
                } else {
                    if (canMoveRight()) {
                        posX++;
                    }
                }
            }
            // If the demon is on the right side
            else if (posX == size - 1) {
                if (posY == size - 1) {
                    if (canMoveLeft()) {
                        posX--;
                    }
                } else {
                    if (canMoveDown()) {
                        posY++;
                    }
                }
            }
            // If the demon is on the bottom side
            else if (posY == size - 1) {
                if (canMoveLeft()) {
                    posX--;
                }
            }

        }


        protected void mate() {

        }
        //override for kills
        protected void follow(int x, int y){
            super.follow(x,y);

            if(posX+1<orgs.length)
                if(orgs[posX+1][posY]!=null&& !(orgs[posX+1][posY]instanceof Demon)){

                    orgs[posX+1][posY].health-=this.str;
                    orgs[posX+1][posY].death();
                    this.hunger+=100;
                    str=str*2;

                }
            if(posX-1>=0)
                if(orgs[posX-1][posY]!=null&&!(orgs[posX-1][posY]instanceof Demon)){

                    orgs[posX-1][posY].health-=this.str;
                    orgs[posX-1][posY].death();
                    this.hunger+=100;
                    str=str*2;

                }
            if(posY+1<orgs.length)
                if(orgs[posX][posY+1]!=null&&!(orgs[posX][posY+1]instanceof Demon)){

                    orgs[posX][posY+1].health-=this.str;
                    orgs[posX][posY+1].death();
                    this.hunger+=100;
                    str=str*2;

                }
            if(posY-1>=0)
                if(orgs[posX][posY-1]!=null&&!(orgs[posX][posY-1]instanceof Demon)){

                    orgs[posX][posY-1].health-=1000;
                    orgs[posX][posY-1].death();
                    this.hunger+=100;
                    str=str*2;

                }
        }
    }

    private class Knight extends Organism {

        // Variables

        // path will choose the path the demon takes after the ritual or when it is spawned
        private int venture = ran.nextInt(4);
        // detour will make knight have a 10% chance to randomly change direction
        private int detour = ran.nextInt(10);

        /*
         * Venture is different than normal, because the knight will always move in a diagonal pattern
         * 0: up-right
         * 1: down-right
         * 2: down-left
         * 3: up-left
         */

        // Constructor

        protected Knight(int x, int y) {
            super(x, y);
            health = 10; //cam take 3 damage
            str = 2;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 10; // counts down to 0, then dies
            speed = 1; // speed, every 1 turn will move
            mating = true; //not yet
        }

        protected Knight() {
            super();
            health = 10; //cam take 3 damage
            str = 2;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 100; // counts down to 0, then dies
            speed = 1; // speed, every 1 turn will move
            mating = true; //not yet
        }

        //remember to call eat when you meat a berry bush in move
        //knights can attack and hunt any organism, except for knights

        protected void attack(Organism o) {
            if (!(o instanceof Knight)) {
                o.health -= str + 3; // boost in attacking
                this.health -= o.str;
            }
        }

        protected void move() {
            // Determines movement when against border
            if (posX == 0 || posX == size - 1 || posY == 0 || posY == size - 1) {



                // If the knight is on a corner

                // Top-left
                if (posX == 0 && posY == 0) {
                    if (canMoveDownRight()) {
                        venture = 1;
                        posX++;
                        posY++;
                    }
                }
                // Bottom-left
                else if (posX == 0 && posY == size - 1) {
                    if (canMoveUpRight()) {
                        venture = 0;
                        posX++;
                        posY--;
                    }
                }
                // Top-right
                else if (posX == size - 1 && posY == 0) {
                    if (canMoveDownLeft()) {
                        venture = 2;
                        posX--;
                        posY++;
                    }
                }
                // Bottom-right
                else if (posX == size - 1 && posY == size - 1) {
                    if (canMoveUpLeft()) {
                        venture = 3;
                        posX--;
                        posY--;
                    }
                }

                // If the knight is on a side

                // Top side
                else if (posY == 0) {
                    if (venture == 3) {
                        if (canMoveDownLeft()) {
                            venture = 2;
                            posX--;
                            posY++;
                        } else {
                            venture = 0;
                        }
                    } else if (venture == 0) {
                        if (canMoveDownRight()) {
                            venture = 1;
                            posX++;
                            posY++;
                        } else {
                            venture = 3;
                        }
                    }
                    else if (venture == 1){
                        if (canMoveDownRight()){
                            posX++;
                            posY++;
                        }
                    }
                    else if (venture == 2){
                        if (canMoveDownLeft()){
                            posX--;
                            posY++;
                        }
                    }
                }
                // Right side
                else if (posX == size - 1) {
                    if (venture == 0) {
                        if (canMoveUpLeft()) {
                            venture = 3;
                            posX--;
                            posY--;
                        } else {
                            venture = 1;
                        }
                    } else if (venture == 1) {
                        if (canMoveDownLeft()) {
                            venture = 2;
                            posX--;
                            posY++;
                        } else {
                            venture = 0;
                        }
                    }
                    else if (venture == 2){
                        if (canMoveDownLeft()){
                            posX--;
                            posY++;
                        }
                    }
                    else if (venture == 3){
                        if (canMoveUpLeft()){
                            posX--;
                            posY--;
                        }
                    }
                }
                // Bottom side
                else if (posY == size - 1) {
                    if (venture == 1) {
                        if (canMoveUpRight()) {
                            venture = 0;
                            posX++;
                            posY--;
                        } else {
                            venture = 2;
                        }
                    } else if (venture == 2) {
                        if (canMoveUpLeft()) {
                            venture = 3;
                            posX--;
                            posY--;
                        } else {
                            venture = 1;
                        }
                    }
                    else if (venture == 3){
                        if (canMoveUpLeft()){
                            posX--;
                            posY--;
                        }
                    }
                    else if (venture == 0){
                        if (canMoveUpRight()){
                            posX++;
                            posY--;
                        }
                    }
                }
                // Left side
                else if (posX == 0) {
                    if (venture == 2) {
                        if (canMoveDownRight()) {
                            venture = 1;
                            posX++;
                            posY++;
                        } else {
                            venture = 3;
                        }
                    } else if (venture == 3) {
                        if (canMoveUpRight()) {
                            venture = 0;
                            posX++;
                            posY--;
                        } else {
                            venture = 2;
                        }
                    }
                    else if (venture == 0){
                        if (canMoveUpRight()){
                            posX++;
                            posY--;
                        }
                    }
                    else if (venture == 1){
                        if (canMoveDownRight()){
                            posX++;
                            posY++;
                        }
                    }
                }
            }

            // Determines movement when not in border

            else {

                // small chance to randomly switch movement
                detour = ran.nextInt(15);
                if (detour == 0) {
                    venture = ran.nextInt(4);
                }

                // Move up-right
                if (venture == 0) {
                    // Check if space is empty
                    if (canMoveUpRight()) {
                        posX++;
                        posY--;
                    } else {
                        venture = ran.nextInt(4);
                    }
                }
                // Move down-right
                if (venture == 1) {
                    // Check if space is empty
                    if (canMoveDownRight()) {
                        posX++;
                        posY++;
                    } else {
                        venture = ran.nextInt(4);
                    }
                }
                // Move down-left
                if (venture == 2) {
                    // Check if space is empty
                    if (canMoveDownLeft()) {
                        posX--;
                        posY++;
                    } else {
                        venture = ran.nextInt(4);
                    }
                }
                // Move up-left
                if (venture == 3) {
                    // Check if space is empty
                    if (canMoveUpLeft()) {
                        posX--;
                        posY--;
                    } else {
                        venture = ran.nextInt(4);
                    }
                }
            }

            //checks for food knights can eat dandelions too
            if(plants[posX][posY] instanceof Corn || plants[posX][posY] instanceof Dandelion){
                hunger+=10;
            }

            //uses hunger, checks death
            hunger--;
            death();
            //knights plant roses
                if((int)(Math.random()*25)== 0){
                    if(plants[posX][posY]==null){
                        addPlant(new Rose(),posX,posY);
                    }
                }
            //create new knight
            if(hunger>110){
                mate(new Knight());
            }



        }

        protected void mate() {

        }
    }

    private class Mage extends Organism {

        // Teleport variable that makes mage randomly spawn in a new location
        private int tele = 0;
        // Teleport points
        private int xT = posX;
        private int yT = posY;
        // Possible movement points
        int x = posX;
        int y = posY;
        // Count to make sure the mage keeps moving
        int count = 0;

        protected Mage(int x, int y) {
            super(x, y);
            health = 10; //can take 3 damage
            str = 10;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 100; // counts down to 0, then dies
            speed = 2; // speed, every 1 turn will move
            mating = true; //not yet
        }

        protected Mage() {
            super();
            health = 10; //can take 3 damage
            str = 10;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 100; // counts down to 0, then dies
            speed = 2; // speed, every 1 turn will move
            mating = true; //not yet
        }

        protected void move() {
            if(plants[posX][posY] instanceof Corn){
                hunger+=20;
            }
            tele++;

            // When the wizard decides to teleport
            if (tele == 50) {
                teleport();
                if(canMoveDown()){
                    if(plants[posX][posY+1] == null){
                        addPlant(new Corn(),posX,posY+1);
                    }
                }
                if(canMoveUp()){
                    if(plants[posX][posY-1] == null){
                        addPlant(new Corn(),posX,posY-1);
                    }
                }
                if(canMoveRight()){
                    if(plants[posX+1][posY] == null){
                        addPlant(new Corn(),posX+1,posY);
                    }
                }
                if(canMoveLeft()){
                    if(plants[posX-1][posY] == null){
                        addPlant(new Corn(),posX-1,posY);
                    }
                }
                tele = 0;
            }

            // When the wizard goes on its regular movement
            else {
                explore();
            }
            hunger--;
            death();
            //mate if on rose
            if(plants[posX][posY] instanceof Rose) {
                mate(new Mage(posX, posY));
            }
        }

        protected void teleport() {
            // Create two numbers to determine the new location of wizard
            int xNew = ran.nextInt(25);
            int yNew = ran.nextInt(25);

            while (orgs[xNew][yNew] != null) {
                xNew = ran.nextInt(25);
                yNew = ran.nextInt(25);
            }

            // Adjust the numbers for position and teleport position
            posX = xNew;
            posY = yNew;
            xT = xNew;
            yT = yNew;

            // Recreate the location to find
            x = ran.nextInt(5) + xT - 2;
            y = ran.nextInt(5) + yT - 2;
            while (x < 0 || x > size - 1 || y < 0 || y > size - 1) {
                x = ran.nextInt(5) + xT - 2;
                y = ran.nextInt(5) + yT - 2;
            }
        }

        protected void explore() {
            // Wizards will never go too far away from their teleport point

            // Decides where the wizard wants to go
            // If the determined x or y is off the grid, recalculate
            while (x < 0 || x > size - 1 || y < 0 || y > size - 1) {
                x = ran.nextInt(5) + xT - 2;
                y = ran.nextInt(5) + yT - 2;
            }

            // Find a new spot to explore if found or can't find the spot
            count++;
            if ((posX == x && posY == y) || count == 5) {
                x = ran.nextInt(5) + xT - 2;
                y = ran.nextInt(5) + yT - 2;
                while (x < 0 || x > size - 1 || y < 0 || y > size - 1) {
                    x = ran.nextInt(5) + xT - 2;
                    y = ran.nextInt(5) + yT - 2;
                }
                count = 0;
            }
            // Otherwise, go to the spot
            else {
                follow(x, y);
            }
        }


    }

    private class Wolf extends Organism {

        // Coordinates to find an organism
        private int x = ran.nextInt(23) + 1;
        private int y = ran.nextInt(23) + 1;
        // Count to wait at spot
        private int count = 0;

        protected Wolf(int x, int y) {
            super(x, y);
            health = 7; //cam take 3 damage
            str = 10;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 100; // counts down to 0, then dies
            speed = 3; // speed, every 1 turn will move
            mating = true; //not yet
        }

        protected Wolf() {
            super();
            health = 8; //cam take 3 damage
            str = 10;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 125; // counts down to 0, then dies
            speed = 5; // speed, every 1 turn will move
            mating = true; //not yet
        }

        protected void move() {
            int folX = 0, folY =0;
            boolean foxFound = false;
            for(int i = 0 ; i<orgs.length; i++){
                for(int j = 0; j<orgs.length; j++){
                    if(orgs[j][i] instanceof Fox) {
                        folX = j;
                        folY = i;
                        foxFound = true;
                    }
                }
            }
            if(foxFound) {
                follow(folX, folY);
                death();
            }else{
                super.move();
            }
            //uses food, checks if starves
            hunger--;

            if(hunger>=130){
                mate(new Wolf());
            }

            if(posX+1<orgs.length)
                if(orgs[posX+1][posY] instanceof Fox){
                    this.eat((Fox)orgs[posX+1][posY]);

                }
            if(posX-1>=0)
                if(orgs[posX-1][posY] instanceof Fox){
                    this.eat((Fox)orgs[posX-1][posY]);
                }
            if(posY+1<orgs.length)
                if(orgs[posX][posY+1] instanceof Fox ){
                    this.eat((Fox)orgs[posX][posY+1]);
                }
            if(posY-1>=0)
                if(orgs[posX][posY-1] instanceof Fox){
                    this.eat((Fox)orgs[posX][posY-1]);
                }


        }


        //call this if it interacts with a fox
        protected void eat(Fox fox) {
            fox.health -= str; // attack it
           // this.health -= 1; // wolf resistance advantage
            if (fox.health <= 0) {
                hunger += fox.imageSize * 25;
            }
            this.str++;
            fox.death(); // checks if fox dies, if it does then it will remocve it
            //this.death();//check if fox manages to kill wolf
        }


    }

    private class Fox extends Organism {

        // Coordinates to find an organism
        private int x = ran.nextInt(23) + 1;
        private int y = ran.nextInt(23) + 1;
        // Count to wait at spot
        private int count = 0;

        protected Fox(int x, int y) {
            super(x, y);
            health = 5; //cam take 3 damage
            str = 5;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 100; // counts down to 0, then dies
            speed = 1; // speed, every 1 turn will move
            mating = true; //not yet
        }

        protected Fox() {
            super();
            health = 5; //cam take 3 damage
            str = 5;  // dweals one damage on attack yea dats rite "dweals" fiteme
            imageSize = 1; // 1x1 creature
            hunger = 100; // counts down to 0, then dies
            speed = 1; // speed, every 1 turn will move
            mating = true; //not yet
        }

        protected void move(){
            if((int)(Math.random()*1000)==0) {
                mating = true;
            }
            super.move();//random movement

            if(plants[posX][posY] instanceof PineTree||back[posX][posY] instanceof Forest){
              eat(4);
            }
            if(plants[posX][posY] instanceof MapleTree&&hunger>=120){
                mate(new Fox()); //mate in different forest
            }

        }
        //call this if it finds a berry bush in  move
        //eat(bush.imageSize);

        protected void mate() {

        }
    }

    //REAL PLANTS

    private class Corn extends Plant {
        private Corn() {
            lifeSpan = 30;
            forestRate = 80;
            plainRate = 100;
            mountainRate = 20;
        }

        protected void step() {
            if (Math.random() < 0.2) {
                maturity++;
            }
            super.step();
        }

        protected void reproduce() {
            if (maturity % 14 == 0) {
                int tryPosition = (int) (Math.random() * 4);
                boolean hasReproduced = false;
                for (int i = tryPosition; i < tryPosition + 4 && !hasReproduced; i++) {
                    switch (i % 4) {
                        case 0:
                            if (posX > 0 && plants[posX - 1][posY] == null) {
                                addPlant(new Corn(), posX - 1, posY);
                                hasReproduced = true;
                            }
                            break;
                        case 1:
                            if (posX < size - 1 && plants[posX + 1][posY] == null) {
                                addPlant(new Corn(), posX + 1, posY);
                                hasReproduced = true;
                            }
                            break;
                        case 2:
                            if (posY > 0 && plants[posX][posY - 1] == null) {
                                addPlant(new Corn(), posX, posY - 1);
                                hasReproduced = true;
                            }
                            break;
                        case 3:
                            if (posY < size - 1 && plants[posX][posY + 1] == null) {
                                addPlant(new Corn(), posX, posY + 1);
                                hasReproduced = true;
                            }
                            break;
                    }
                }
            }
        }
    }

    private class Dandelion extends Plant {
        private Dandelion() {
            lifeSpan = 2;
            forestRate = 100;
            plainRate = 100;
            mountainRate = 100;
        }

        protected void reproduce() {
            if (Math.random() < 0.55) {
                int tryPosition = (int) (Math.random() * 4);
                boolean hasReproduced = false;
                for (int i = tryPosition; i < tryPosition + 4 && !hasReproduced; i++) {
                    switch (i % 4) {
                        case 0:
                            if (posX > 0 && plants[posX - 1][posY] == null) {
                                addPlant(new Dandelion(), posX - 1, posY);
                                hasReproduced = true;
                            }
                            break;
                        case 1:
                            if (posX < size - 1 && plants[posX + 1][posY] == null) {
                                addPlant(new Dandelion(), posX + 1, posY);
                                hasReproduced = true;
                            }
                            break;
                        case 2:
                            if (posY > 0 && plants[posX][posY - 1] == null) {
                                addPlant(new Dandelion(), posX, posY - 1);
                                hasReproduced = true;
                            }
                            break;
                        case 3:
                            if (posY < size - 1 && plants[posX][posY + 1] == null) {
                                addPlant(new Dandelion(), posX, posY + 1);
                                hasReproduced = true;
                            }
                            break;
                    }
                }
            }
        }
    }

    private abstract class Tree extends Plant {
        protected int reproFreq;

        protected void step() {
            if (Math.random() < 0.1) {
                maturity++;
            }
            super.step();
        }

        protected void reproduce() throws IndexOutOfBoundsException {
            if (maturity % reproFreq == 0) {
                int tryPosition = (int) (Math.random() * 8);
                boolean hasReproduced = false;
                for (int i = tryPosition; i < tryPosition + 8 && !hasReproduced; i++) {
                    switch (i % 8) {
                        case 0:
                            if (posX > 0 && posY > 0 && !(plants[posX - 1][posY - 1] instanceof Tree)) {
                                if (plants[posX - 1][posY - 1] != null) {
                                    plantList.remove(plantList.indexOf(plants[posX - 1][posY - 1]));
                                }
                                addPlant(child(), posX - 1, posY - 1);
                                hasReproduced = true;
                            }
                            break;
                        case 1:
                            if (posX > 0 && !(plants[posX - 1][posY] instanceof Tree)) {
                                if (plants[posX - 1][posY] != null) {
                                    plantList.remove(plantList.indexOf(plants[posX - 1][posY]));
                                }
                                addPlant(child(), posX - 1, posY);
                                hasReproduced = true;
                            }
                            break;
                        case 2:
                            if (posX > 0 && posY < size - 1 && !(plants[posX - 1][posY + 1] instanceof Tree)) {
                                if (plants[posX - 1][posY + 1] != null) {
                                    plantList.remove(plantList.indexOf(plants[posX - 1][posY + 1]));
                                }
                                addPlant(child(), posX - 1, posY + 1);
                                hasReproduced = true;
                            }
                            break;
                        case 3:
                            if (posY < size - 1 && !(plants[posX][posY + 1] instanceof Tree)) {
                                if (plants[posX][posY + 1] != null) {
                                    plantList.remove(plantList.indexOf(plants[posX][posY + 1]));
                                }
                                addPlant(child(), posX, posY + 1);
                                hasReproduced = true;
                            }
                            break;
                        case 4:
                            if (posX < size - 1 && posY < size - 1 && !(plants[posX + 1][posY + 1] instanceof Tree)) {
                                if (plants[posX + 1][posY + 1] != null) {
                                    plantList.remove(plantList.indexOf(plants[posX + 1][posY + 1]));
                                }
                                addPlant(child(), posX + 1, posY + 1);
                                hasReproduced = true;
                            }
                            break;
                        case 5:
                            if (posX < size - 1 && !(plants[posX + 1][posY] instanceof Tree)) {
                                if (plants[posX + 1][posY] != null) {
                                    plantList.remove(plantList.indexOf(plants[posX + 1][posY]));
                                }
                                addPlant(child(), posX + 1, posY);
                                hasReproduced = true;
                            }
                            break;
                        case 6:
                            if (posX < size - 1 && posY > 0 && !(plants[posX + 1][posY - 1] instanceof Tree)) {
                                if (plants[posX + 1][posY - 1] != null) {
                                    plantList.remove(plantList.indexOf(plants[posX + 1][posY - 1]));
                                }
                                addPlant(child(), posX + 1, posY - 1);
                                hasReproduced = true;
                            }
                            break;
                        case 7:
                            if (posY > 0 && !(plants[posX][posY - 1] instanceof Tree)) {
                                if (plants[posX][posY - 1] != null) {
                                    plantList.remove(plantList.indexOf(plants[posX][posY - 1]));
                                }
                                addPlant(child(), posX, posY - 1);
                                hasReproduced = true;
                            }
                            break;
                    }
                }
            }
        }

        protected abstract Tree child();
    }

    private class PineTree extends Tree {
        private PineTree() {
            lifeSpan = 200;
            reproFreq = 99;
            forestRate = 100;
            plainRate = 95;
            mountainRate = 100;
        }

        protected Tree child() {
            return new PineTree();
        }
    }

    private class MapleTree extends Tree {
        private MapleTree() {
            lifeSpan = 100;
            reproFreq = 49;
            forestRate = 100;
            plainRate = 95;
            mountainRate = 98;
        }

        protected Tree child() {
            return new MapleTree();
        }
    }


    private class Rose extends Plant {
        private Rose() {
            lifeSpan = 14;
            forestRate = 100;
            plainRate = 90;
            mountainRate = 90;
        }

        protected void reproduce() {
            if (Math.random() < 0.09) {
                int tryPosition = (int) (Math.random() * 4);
                boolean hasReproduced = false;
                for (int i = tryPosition; i < tryPosition + 4 && !hasReproduced; i++) {
                    switch (i % 4) {
                        case 0:
                            if (posX > 0 && plants[posX - 1][posY] == null) {
                                addPlant(new Rose(), posX - 1, posY);
                                hasReproduced = true;
                            }
                            break;
                        case 1:
                            if (posX < size - 1 && plants[posX + 1][posY] == null) {
                                addPlant(new Rose(), posX + 1, posY);
                                hasReproduced = true;
                            }
                            break;
                        case 2:
                            if (posY > 0 && plants[posX][posY - 1] == null) {
                                addPlant(new Rose(), posX, posY - 1);
                                hasReproduced = true;
                            }
                            break;
                        case 3:
                            if (posY < size - 1 && plants[posX][posY + 1] == null) {
                                addPlant(new Rose(), posX, posY + 1);
                                hasReproduced = true;
                            }
                            break;
                    }
                }
            }
        }
    }

}



