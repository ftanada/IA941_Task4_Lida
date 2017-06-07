package modules;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import ws3dproxy.CommandUtility;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;
import ws3dproxy.model.WorldPoint;
import ws3dproxy.util.Constants;

public class Environment extends EnvironmentImpl 
{
   private static final Logger logger = Logger.getLogger(Environment.class.getCanonicalName());
    
    private static final int DEFAULT_TICKS_PER_RUN = 100;
    private int ticksPerRun;
    private WS3DProxy proxy;
    private Creature creature;
    private Thing food;
    private Thing jewel;
    private List<Thing> thingAhead;
    private Thing leafletJewel;
    private Thing wall;
    private String currentAction;   
    private double energy;
    private Thing genericThing;
    
    // FMT 2017
    private int dCellSize = 30;
    private int iSafeSpace = 10;
    private double dBoardXSize = 800;
    private double dBoardYSize = 600;
    private String sBoardMap;
    private String sLastAction;
    private int iXCells = (int)Math.ceil(dBoardXSize/ dCellSize);
    private int iYCells = (int)Math.ceil(dBoardYSize/dCellSize);
    private int targetXCell = iXCells -2;
    private int targetYCell = iYCells -2;
    private boolean[][] envGrid = new boolean[iXCells][iYCells];
    private List<int[]> possibleCells = new ArrayList<>();
    public List<int[]> pathToTarget = new ArrayList<>();
    
    public Environment() 
    {
        this.ticksPerRun = DEFAULT_TICKS_PER_RUN;
        this.proxy = new WS3DProxy();
        this.creature = null;
        this.food = null;
        this.jewel = null;
        this.thingAhead = new ArrayList<>();
        this.leafletJewel = null;
        this.wall = null;
        this.genericThing = null;
        this.currentAction = "moveRight";
        this.sLastAction = "";
    }

    @Override
    public void init() {
        super.init();
        ticksPerRun = (Integer) getParam("environment.ticksPerRun", DEFAULT_TICKS_PER_RUN);
        taskSpawner.addTask(new BackgroundTask(ticksPerRun));
        
        try 
        {
            System.out.println("Reseting the WS3D World ...");
            proxy.getWorld().reset();
            creature = proxy.createCreature(80, 80, 0);
            creature.start();
            System.out.println("Starting the WS3D Resource Generator ... ");
            // FMT World.grow(1);
            
            // FMT 01/06/2017 Create Simulation Enviroment - walls
            // not works CommandUtility.sendNewBrick(4,119.0,5.0,142.0,199.0);
            // borders
            CommandUtility.sendNewBrick(2, 795, 0, 800, 600);
            CommandUtility.sendNewBrick(2, 0, 0, 800, 5);
            CommandUtility.sendNewBrick(2, 0, 595, 800, 600);
            CommandUtility.sendNewBrick(2, 0, 0, 5, 600);
            //maze
            // horizontal
            CommandUtility.sendNewBrick(4,13.0,224.0,87.0,234.0);   
            CommandUtility.sendNewBrick(4,107.0,306.0,228.0,334.0);   
            CommandUtility.sendNewBrick(4,244.0,528.0,620.0,542.0);
            CommandUtility.sendNewBrick(4,302.0,468.0,500.0,479.0);
            CommandUtility.sendNewBrick(4,500.0,300.0,710.0,310.0);
            // vertical
            CommandUtility.sendNewBrick(4,318.0,150.0,327.0,350.0);   
            CommandUtility.sendNewBrick(4,700.0,20.0,710.0,480.0);   
            
            // FMT initial move
            creature.start();
            creature.moveto(3.0, 450, 450); 
            creature.move(0, 0, 0);
            
            Thread.sleep(4000);
            creature.updateState();
            System.out.println("DemoLIDA has started...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BackgroundTask extends FrameworkTaskImpl {

        public BackgroundTask(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            updateEnvironment();
            performAction(currentAction);
        }
    }

    @Override
    public void resetState() {
        currentAction = "gotoDestination";
    }
    
    // FMT Checks if a INT[] is inside a LIST<INT[]>
    public boolean checkListContains(List<int[]> inputList, int[] cell)
    {
        boolean resp = false;
        for (int[] tcell: inputList)
        {
            if (tcell[0]==cell[0] && tcell[1]==cell[1])
                resp = true;
        }
        return(resp);
    }
    
    // Checks if a Thing is inside the cell grid x,y
    private boolean isInsideGrid(Thing thing, int x, int y){
        int xmin = dCellSize* x - dCellSize;
        int xmax = dCellSize* x;
        int ymin = dCellSize* y - dCellSize;
        int ymax = dCellSize* y;
        int y1 = (int)thing.getY1()-iSafeSpace;
        int y2 = (int)thing.getY2()+iSafeSpace;
        int x1 = (int)thing.getX1()-iSafeSpace;
        int x2 = (int)thing.getX2()+iSafeSpace;
        if (((x1>=xmin && x1<=xmax)
                && (y1>=ymin && y1<=ymax))
                || ((x2>=xmin && x2<=xmax)
                && (y2>=ymin && y2<=ymax))
                ||((x1>=xmin && x1<=xmax)
                && (y2>=ymin && y2<=ymax))
                || ((x2>=xmin && x2<=xmax)
                && (y1>=ymin && y1<=ymax))){
            return true;
        }
        else if (((xmin>=x1 && xmin<=x2)
                && (ymin>=y1 && ymin<=y2))
                || ((xmax>=x1 && xmax<=x2)
                && (ymax>=y1 && ymax<=y2))
                ||((xmin>=x1 && xmin<=x2)
                && (ymax>=y1 && ymax<=y2))
                || ((xmax>=x1 && xmax<=x2)
                && (ymin>=y1 && ymin<=y2))){
            return true;
                    }
        else if (((((x1<=xmin && x2>=xmax)))
                && ((y1>=ymin && y1<=ymax) || (y2>=ymin && y2<=ymax)))
                ||(((y1<=ymin && y2>=ymax))
                && ((x1>=xmin && x1<=xmax) || (x2>=xmin && x2<=xmax)))){
            return true;
        }
        return false;
    }
        
    //Add seen bricks to memory
    private void recordBricks(List<Thing> things)
    {
        int xCels = iXCells;
        int yCels = iYCells;
        for (int i = 1; i <= yCels; i++) 
        {
            for (int j = 1; j <= xCels; j++) 
            {
                for (Thing thing : things) 
                {
                    if (isInsideGrid(thing, j, i))
                    {
                        envGrid[j-1][i-1]=true;
                    }
                }
            }
        }
    }
    
    //Clear bricks from memory
    private void clearBricks()
    {
        int xCels = iXCells;
        int yCels = iYCells;
        for (int i = 1; i <= yCels; i++) 
        {
            for (int j = 1; j <= xCels; j++) 
            {
                envGrid[j-1][i-1]= false;
            }
        }
    }
    
    //returns a string that visually represents the grids which are or not ocupied in the enviroment
    private String mapStringer(){
        String response = "";
        int xCels = iXCells;
        int yCels = iYCells;
        String pos;
        
        for (int i = 1; i <= yCels; i++) {
            for (int j = 1; j <= xCels; j++) {
                pos = "\u2591";
                
                if (envGrid[j-1][i-1]==true)
                    pos = "\u2588";
                for (int[] caminho : pathToTarget) {
                    if (caminho[0]==j && caminho[1]==i){
                        pos = "\u256C";
                    }
                }
                response += pos;
                
            }
            response += System.getProperty("line.separator");
        }
        return response;
    }
        //RECURSEVELY search for the Target cell
    private List<int[]> recursivePath(List<int[]> cells){
        List<int[]> localCells = new ArrayList<>();
        List<int[]> aux = new ArrayList<>();
        
        String pathString = "";
        for (int[] cell : cells){
        pathString+=("("+cell[0]+","+cell[1]+")");
        }
        //System.out.println(pathString+System.getProperty("line.separator"));
        
        //registra esse level da busca
        for (int[] cell:cells)
        {
            for (int[] tcell : sideFreeCell(cell))
            {
                if (!checkListContains(possibleCells,tcell))
                {
                    //adiciona a lista de células já procuradas
                    possibleCells.add(tcell);
                    aux.add(tcell);                    
                }
            }
        }
        
        // checa se achou target
        for (int[] cell:cells){
            for (int[] tcell : sideFreeCell(cell))
            {
                if (checkListContains(aux,tcell))
                {
                    if (tcell[0]==targetXCell && tcell[1]==targetYCell)
                    {
                        localCells.add(tcell);
                        localCells.add(cell);
                        return localCells;
                    }
                }
            }
        }
        try
        {
          localCells =recursivePath(aux);
        }
        catch (Exception name) 
        {
           System.out.println(name.getLocalizedMessage());
        }
        int[] lastCell=localCells.get(localCells.size() - 1);
        for (int[] cell:cells)
        {
            for (int[] tcell : sideFreeCell(cell))
            {
                if (tcell[0]==lastCell[0] && tcell[1]==lastCell[1])
                {
                    localCells.add(cell);
                    return localCells;
                }
            }
        }
        return  null;
    }
       
    //Checks for a list of FREE CELLS adjacent to a desired cell
    private List<int[]> sideFreeCell(int[] currentCell){
        List<int[]> freeCells = new ArrayList<>();
        try
        {        
        if (currentCell[0]+1 <= iXCells)
            if(envGrid[currentCell[0]][currentCell[1]-1]==false)
                freeCells.add(new int[]{currentCell[0]+1,currentCell[1]});
        if (currentCell[0]-1 >0)
            if(envGrid[currentCell[0]-2][currentCell[1]-1]==false)
                freeCells.add(new int[]{currentCell[0]-1,currentCell[1]});
        if (currentCell[1]+1 <= iYCells)
            if(envGrid[currentCell[0]-1][currentCell[1]]==false)
                freeCells.add(new int[]{currentCell[0],currentCell[1]+1});
        if (currentCell[1]-1 > 0)
            if(envGrid[currentCell[0]-1][currentCell[1]-2]==false)
                freeCells.add(new int[]{currentCell[0],currentCell[1]-1});        
        }
        catch (Exception e)
        {
            System.out.println("Error on sidefreeCell:" +e.getMessage());
        }
        return freeCells; 
    }
    
    //Returns the cells list to the target
    private List<int[]> searchPath(){
        List<int[]> thisPathToTarget = new ArrayList<>();
        int cxpos = (int)Math.ceil(creature.getPosition().getX()/dCellSize);
        int cypos = (int)Math.ceil(creature.getPosition().getY()/dCellSize);
        
        possibleCells.clear();
        possibleCells.add(new int[]{cxpos,cypos});
        List<int[]> creaPos = new ArrayList();
        creaPos.add(new int[]{cxpos,cypos});
        thisPathToTarget = recursivePath(creaPos);
        thisPathToTarget.remove(thisPathToTarget.size() - 1);
        //String pathString = "";
        //for (int[] cell : thisPathToTarget){
        //pathString+=("("+cell[0]+","+cell[1]+")"+System.getProperty("line.separator"));
        //}
        //System.out.println(pathString);
        
        return thisPathToTarget;
    }
    
    // FMT returns the current creature cell x,y
    public int[] returnCurrentCreatureCell()
    {
        int xPos = (int)Math.ceil(creature.getPosition().getX()/ dCellSize);
        int yPos = (int)Math.ceil(creature.getPosition().getY()/ dCellSize);
        return new int[]{xPos, yPos};
    }

    @Override
    public Object getState(Map<String, ?> params) {
        Object requestedObject = null;
        String mode = (String) params.get("mode");
        //System.out.println("getState: "+mode);
        int[] vBoard;
        boolean flag = false;
        switch (mode) {
            case "openRight":
                vBoard = returnCurrentCreatureCell();
                if (vBoard[0]+1 <= iXCells)
                    if (checkListContains(pathToTarget, new int[]{vBoard[0]+1, vBoard[1]}))
                        flag = true;
                break;
            case "openLeft":
                vBoard = returnCurrentCreatureCell();
                if (vBoard[0]-1 > 0)
                    if(checkListContains(pathToTarget, new int[]{vBoard[0]-1, vBoard[1]}))
                        flag = true;
                break;
            case "openBottom":
                vBoard = returnCurrentCreatureCell();
                if (vBoard[1]+1 <= iYCells)
                    if(checkListContains(pathToTarget, new int[]{vBoard[0], vBoard[1]+1}))
                        flag = true;
                break;
            case "openTop":
                vBoard = returnCurrentCreatureCell();
                if (vBoard[1]-1 > 0)
                    if(checkListContains(pathToTarget, new int[]{vBoard[0], vBoard[1]-1}))
                        flag = true;
                break;
            default:
                break;
        }
        return(flag);
    }

    public boolean isInsideCell(Thing tThing, int x, int y)
    {
       Boolean bRet = false;
       double x1 = tThing.getX1();
       double x2 = tThing.getX2();
       double y1 = tThing.getY1();
       double y2 = tThing.getY2();
       if ((x > x1) && (x < x2) && (y > y1) && (y < y2))
         bRet = true;
       return(bRet);
    }
    /**
     *
     * @param tThings
     * @return
     * Function to map the board - blank spaces vs cells with things
     */
    public String boardMapper(List<Thing> tThings)
    {
      String sMap;
      String sAux;
      
      sMap = "";
      int xPos = (int) Math.ceil(dBoardXSize/dCellSize);
      int yPos = (int) Math.ceil(dBoardXSize/dCellSize);
      for (int i = 1; i <= yPos; i++)
      {
          for (int j = 1; j <= xPos; j++)
          {
            sAux = "-";
            for (Thing thing: tThings)
            {
                if (isInsideCell(thing, j, i))
                    sAux = "X";
            }
            sMap += sAux;
          }
      }
      return(sMap);
    }
    
    public void updateEnvironment() 
    {
        List<Thing> lThings;
        creature.updateState();
        food = null;
        jewel = null;
        leafletJewel = null;
        genericThing = null;
        wall = null;
        thingAhead.clear();
                
        energy = creature.getFuel();
        // FMT System.out.println("updateEnvironment: "+energy);
        lThings = creature.getThingsInVision();
        sBoardMap = this.boardMapper(lThings);
        /*for (Thing thing : lThings) 
        {
            if (creature.calculateDistanceTo(thing) <= Constants.OFFSET) {
                // Identifica o objeto proximo
                thingAhead.add(thing);
                genericThing = thing;
                System.out.println("updateEnvironment: found genericThing");
                break;
            } else if (thing.getCategory() == Constants.categoryJEWEL) 
              {
                if (leafletJewel == null) 
                {
                    // Identifica se a joia esta no leaflet
                    for (Leaflet leaflet: creature.getLeaflets())                   
                    {
                        if (leaflet.ifInLeaflet(thing.getMaterial().getColorName()) &&
                                leaflet.getTotalNumberOfType(thing.getMaterial().getColorName()) > leaflet.getCollectedNumberOfType(thing.getMaterial().getColorName()))
                        {
                            leafletJewel = thing;
                            System.out.println("updateEnvironment: found leafletJewel");
                            break;
                        }
                    }
                } else 
                  {
                    // Identifica a joia que nao esta no leaflet
                    jewel = thing;
                    System.out.println("updateEnvironment: found leafletJewel");
                  }
            } else if (food == null && creature.getFuel() <= 300.0
                        && (thing.getCategory() == Constants.categoryFOOD
                        || thing.getCategory() == Constants.categoryPFOOD
                        || thing.getCategory() == Constants.categoryNPFOOD)) 
                    {                
                      // Identifica qualquer tipo de comida
                      food = thing;
                      System.out.println("updateEnvironment: found food)");
                    } else if (wall == null && (thing.getCategory() == Constants.categoryBRICK))
                           {
                              wall = thing;
                              // FMT System.out.println("updateEnvironment: found wall)");
                           }                               
        } */
        // new code
        recordBricks(creature.getThingsInVision());
        pathToTarget = searchPath();
    }
    
   
    @Override
    public void processAction(Object action) {
        String actionName = (String) action;
        currentAction = actionName.substring(actionName.indexOf(".") + 1);
    }

    public String getLastAction()
    {
      return(sLastAction);    
    }
    
    public void resetLastAction()
    {
      sLastAction = null;    
    }
    
    private void performAction(String currentAction) 
    {
        WorldPoint currPoint;
        sLastAction = currentAction;
        try {
            System.out.println("performAction: "+currentAction);
            switch (currentAction) {
                case "rotate":
                    creature.rotate(1.0);
                    //CommandUtility.sendSetTurn(creature.getIndex(), -1.0, -1.0, 3.0);
                    break;
                case "gotoFood":
                    if (food != null) 
                        creature.moveto(1.5, food.getX1(), food.getY1());
                        //CommandUtility.sendGoTo(creature.getIndex(), 3.0, 3.0, food.getX1(), food.getY1());
                    break;
                case "gotoJewel":
                    if (leafletJewel != null)
                        creature.moveto(3.0, leafletJewel.getX1(), leafletJewel.getY1());
                        //CommandUtility.sendGoTo(creature.getIndex(), 3.0, 3.0, leafletJewel.getX1(), leafletJewel.getY1());
                    break;                    
                case "get":
                    creature.move(0.0, 0.0, 0.0);
                    //CommandUtility.sendSetTurn(creature.getIndex(), 0.0, 0.0, 0.0);
                    if (thingAhead != null) {
                        for (Thing thing : thingAhead) {
                            if (thing.getCategory() == Constants.categoryJEWEL) {
                                creature.putInSack(thing.getName());
                            } else if (thing.getCategory() == Constants.categoryFOOD || thing.getCategory() == Constants.categoryNPFOOD || thing.getCategory() == Constants.categoryPFOOD) {
                                creature.eatIt(thing.getName());
                            }
                        }
                    }
                    this.resetState();
                    break;
                case "gotoDestination":
                    creature.moveto(1.5, 450.0, 450.0);
                        //CommandUtility.sendGoTo(creature.getIndex(), 3.0, 3.0, leafletJewel.getX1(), leafletJewel.getY1());
                    break;                    
                case "moveAgent":
                    currPoint = creature.getPosition();
                    double angle = currPoint.getAngle();
                    creature.moveto(3.0, currPoint.getX()+dCellSize, currPoint.getY()+dCellSize);
                    break;                    
                case "turnLeft":
                    creature.rotate(1.5);
                    break;
                case "turnRight":
                    creature.rotate(1.5);
                    break;
                case "moveBottom":
                    creature.moveto(1.5, creature.getPosition().getX(), creature.getPosition().getY()+5);                     
                    break;
                case "moveRight":
                    creature.moveto(1.5, creature.getPosition().getX()+5, creature.getPosition().getY());
                    break;
                case "moveLeft":
                    creature.moveto(1.5, creature.getPosition().getX()-5, creature.getPosition().getY());
                    break;
                case "moveTop":
                    creature.moveto(1.5, creature.getPosition().getX(), creature.getPosition().getY()-5);
                    //CommandUtility.sendGoTo(creature.getIndex(), 3.0, 3.0, creature.getPosition().getX(), creature.getPosition().getY()-5);
                    break;                    
                default:
                    creature.moveto(1.5, 450.0, 450.0);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
