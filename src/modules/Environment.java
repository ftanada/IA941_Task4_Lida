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
    private GridMap gridMap;
    private List<Thing> walls = new ArrayList<>();

    
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
        this.currentAction = "gotoDestination";
        this.gridMap = new GridMap(25, 25, 775, 25);
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
            creature = proxy.createCreature(80, 110, 0);
            creature.start();
            System.out.println("Starting the WS3D Resource Generator ... ");
            // FMT World.grow(1);
            
            // FMT 01/06/2017 Create Simulation Enviroment - walls
            // not works CommandUtility.sendNewBrick(4,119.0,5.0,142.0,199.0);
            // borders
            CommandUtility.sendNewBrick(4, 0, 595, 800, 600);
            /*CommandUtility.sendNewBrick(4, 795, 0, 800, 600);
            CommandUtility.sendNewBrick(4, 0, 0, 800, 5);
            CommandUtility.sendNewBrick(4, 0, 0, 5, 600);
            //maze
            // horizontal
            CommandUtility.sendNewBrick(2,13.0,224.0,87.0,234.0);   
            CommandUtility.sendNewBrick(2,107.0,306.0,228.0,334.0);   
            CommandUtility.sendNewBrick(2,244.0,528.0,620.0,542.0);
            CommandUtility.sendNewBrick(2,302.0,468.0,500.0,479.0);
            CommandUtility.sendNewBrick(2,500.0,300.0,710.0,310.0);
            // vertical
            CommandUtility.sendNewBrick(2,318.0,150.0,327.0,350.0);   
            CommandUtility.sendNewBrick(2,700.0,20.0,710.0,480.0);   */
            
            World.createBrick(2,10.0,580.0,100.00,590.0);
            World.createBrick(2,230.0,6.0,240.00,400.0);
            World.createBrick(2,370.0,200.0,380.00,594.00);
            World.createBrick(2,530.0,6.0,540.00,400.0);
            
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
    public void resetState() 
    {
        currentAction = "gotoDestination";
    }
    
    public void terminateState()
    {
       currentAction = "none";
       System.out.println("DemoLIDA has finished its mission.");
       System.exit(1);
    }
    
    @Override
    public Object getState(Map<String, ?> params) {
        Object requestedObject = null;
        String mode = (String) params.get("mode");
        //System.out.println("getState: "+mode);
        int[] vBoard;
        boolean flag = false;
        switch (mode) 
        {
            case "food":
                requestedObject = food;
                break;
            case "jewel":
                requestedObject = jewel;
                break;
            case "thingAhead":
                requestedObject = thingAhead;
                break;
            case "leafletJewel":
                requestedObject = leafletJewel;
                break;
            case "openRight":
            case "openLeft":
            case "openBottom":
            case "openTop":
            case "wall":
                requestedObject = wall;
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
                
        for (Thing thing : creature.getThingsInVision()) 
        {
            if (thing.getCategory() == Constants.categoryBRICK) 
            {
                if (walls.isEmpty() || walls.stream().noneMatch(wall -> wall.getName().equals(thing.getName()))) {
                    wall = thing;
                    walls.add(thing);
                    gridMap.markWall(thing); 
                }
            }
        }
        
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
    }
    
   
    @Override
    public void processAction(Object action) 
    {
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
                /*case "moveBottom":
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
                    break;   */
                case "moveBottom":
                case "moveRight":
                case "moveLeft":
                case "moveTop":
                case "gotoDestination":
                  creature.updateState();
                  double currX = creature.getPosition().getX();
                  double currY = creature.getPosition().getY();
                  if ((currX > 750) && (currY < 50)) terminateState();
                  gridMap.markStartPosition(currX, currY);
                  List<Coordinate> coordinates = gridMap.findPath();
                  if (coordinates != null && !coordinates.isEmpty()) try
                  {
                    creature.moveto(1.5, coordinates.get(0).getX(), coordinates.get(0).getY());
                  } catch (Exception e) 
                    {
                      e.printStackTrace();
                    }
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
