/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.utexas.cs.nn.gridTorus;

import edu.utexas.cs.nn.parameters.Parameters;
import edu.utexas.cs.nn.util.datastructures.ArrayUtil;

/**
 * Description of some key game components such as converging and prey being eaten
 * @author Jacob Schrum
 */
public class TorusPredPreyGame {
    
    public static final int AGENT_TYPE_PRED = 0;
    public static final int AGENT_TYPE_PREY = 1;
    
    private final TorusWorld world;
    private final TorusAgent[] preds;
    private final TorusAgent[] preys;
    
    private boolean gameOver;
    private int time;
    private int timeLimit;

    /**
     * Constructor for the game board
     * @param xDim dimensions for the x axis
     * @param yDim dimensions for the y axis
     * @param numPred number of predators
     * @param numPrey number of prey
     */
    public TorusPredPreyGame(int xDim, int yDim, int numPred, int numPrey){
        gameOver = false;
        time = 0;
        timeLimit = Parameters.parameters.integerParameter("torusTimeLimit");
        
        world = new TorusWorld(xDim, yDim);
        preds = new TorusAgent[numPred];
        preys = new TorusAgent[numPrey];
        // Place predators
        for(int i = 0; i < numPred; i++) {
            int[] pos = world.randomCell();
            preds[i] = new TorusAgent(world, pos[0], pos[1], AGENT_TYPE_PRED);
        }
        // Place prey where predators aren't
        for(int i = 0; i < numPrey; i++) {
            int[] pos = world.randomUnoccupiedCell(preds);
            preys[i] = new TorusAgent(world, pos[0], pos[1], AGENT_TYPE_PREY);
        }
    }
    
    public TorusWorld getWorld(){
        return world;
    }
    
    public TorusAgent[][] getAgents(){
        return new TorusAgent[][]{preds,preys};
    }

    public TorusAgent[] getPredators(){
        return preds;
    }
    
    public TorusAgent[] getPrey(){
        return preys;
    }
    
    /**
     * converges all predators and prey towards the other
     * @param predMoves a grid of the possible predator moves
     * @param preyMoves a grid of the possible prey moves
     */
    public void advance(int[][] predMoves, int[][] preyMoves) {
        moveAll(predMoves, preds);
        moveAll(preyMoves, preys);
        eat(preds, preys);
        time++;
        gameOver = ArrayUtil.countOccurrences(null, preys) == preys.length ||
                time >= timeLimit;
    }
    
    public int getTime(){
        return time;
    }
    
    /**
     * moves all the agents along the x and y coordinates
     * @param moves a grid of all possible moves for an agent
     * @param agents the array of all the agents
     */
    private static void moveAll(int[][] moves, TorusAgent[] agents) {
        assert moves.length == agents.length : "Moves and Agents don't match up: " + moves.length +" != "+ agents.length;
        for(int i = 0; i < agents.length; i++) {
            if(agents[i] != null) {
                agents[i].move(moves[i][0], moves[i][1]);
            }
        }
    }
    
    /**
     * If any predator and prey are in the same location, the prey is eaten
     * @param preds list of predators
     * @param preys list of the prey
     */
    private static void eat(TorusAgent[] preds, TorusAgent[] preys) {
        for(int i = 0; i < preys.length; i++) {
            if(preys[i] != null && preys[i].isCoLocated(preds)){ // Prey is eaten
            	//The prey at this location is currently being digested, so is now null
                preys[i] = null;
            }
        }
    }

    boolean gameOver() {
        return gameOver;
    }
}
