package modules;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;
import ws3dproxy.util.Constants;

public class Environment extends EnvironmentImpl {

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
    private GridMap gridMap;
    private List<Thing> walls = new ArrayList<>();
    private String sLastAction;
    
    public Environment() {
        this.ticksPerRun = DEFAULT_TICKS_PER_RUN;
        this.proxy = new WS3DProxy();
        this.creature = null;
        this.food = null;
        this.jewel = null;
        this.thingAhead = new ArrayList<>();
        this.leafletJewel = null;
        this.currentAction = "gotoDestination";
        this.gridMap = new GridMap(25, 25, 775, 25);
        this.sLastAction = "";
    }

    @Override
    public void init() {
        super.init();
        ticksPerRun = (Integer) getParam("environment.ticksPerRun", DEFAULT_TICKS_PER_RUN);
        taskSpawner.addTask(new BackgroundTask(ticksPerRun));
        
        try {
            System.out.println("Reseting the WS3D World ...");
            proxy.getWorld().reset();
            creature = proxy.createCreature(25, 25, 0);
            creature.start();
            System.out.println("Starting the WS3D Resource Generator ... ");
            //World.grow(1);
            Thread.sleep(4000);
            creature.updateState();
            System.out.println("DemoLIDA has started...");
            //World.createBrick(4,200.0,0.0,250.00,400.0);
            //World.createBrick(4,350.0,200.0,400.00,600.00);
            //World.createBrick(4,500.0,0.0,550.00,400.0);
            World.createBrick(4,230.0,0.0,240.00,400.0);
            World.createBrick(4,370.0,200.0,380.00,600.00);
            World.createBrick(4,530.0,0.0,540.00,400.0);
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

    @Override
    public Object getState(Map<String, ?> params) {
        Object requestedObject = null;
        String mode = (String) params.get("mode");
        switch (mode) {
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
            case "wall":
                requestedObject = wall;
                break;
            default:
                break;
        }
        return requestedObject;
    }

    
    public void updateEnvironment() {
        creature.updateState();
        food = null;
        jewel = null;
        leafletJewel = null;
        wall = null;
        thingAhead.clear();
    
        for (Thing thing : creature.getThingsInVision()) {
            if (thing.getCategory() == Constants.categoryBRICK) {
                if (walls.isEmpty() || walls.stream().noneMatch(wall -> wall.getName().equals(thing.getName()))) {
                    wall = thing;
                    walls.add(thing);
                    gridMap.markWall(thing); 
                }
            }
            
            if (creature.calculateDistanceTo(thing) <= Constants.OFFSET) {
                // Identifica o objeto proximo
                thingAhead.add(thing);
                break;
            } else if (thing.getCategory() == Constants.categoryJEWEL) {
                if (leafletJewel == null) {
                    // Identifica se a joia esta no leaflet
                    for(Leaflet leaflet: creature.getLeaflets()){
                        if (leaflet.ifInLeaflet(thing.getMaterial().getColorName()) &&
                                leaflet.getTotalNumberOfType(thing.getMaterial().getColorName()) > leaflet.getCollectedNumberOfType(thing.getMaterial().getColorName())){
                            leafletJewel = thing;
                            break;
                        }
                    }
                } else {
                    // Identifica a joia que nao esta no leaflet
                    jewel = thing;
                }
            } else if (food == null && creature.getFuel() <= 300.0
                        && (thing.getCategory() == Constants.categoryFOOD
                        || thing.getCategory() == Constants.categoryPFOOD
                        || thing.getCategory() == Constants.categoryNPFOOD)) {
                
                    // Identifica qualquer tipo de comida
                    food = thing;
            }
           
        }
    }
    
    
    
    @Override
    public void processAction(Object action) {
        String actionName = (String) action;
        currentAction = actionName.substring(actionName.indexOf(".") + 1);
    }

    public String getCurrentAction() {
        return currentAction;
    }
    
    private void performAction(String currentAction) {
        try {
            switch (currentAction) {
                case "gotoDestination":
                    creature.updateState();
                    gridMap.markStartPosition(creature.getPosition().getX(), creature.getPosition().getY());
                    List<Coordinate> coordinates = gridMap.findPath();
                    if (coordinates != null && !coordinates.isEmpty()) {
                        creature.moveto(0.5, coordinates.get(0).getX(), coordinates.get(0).getY());
                    }
                    break;   
                case "rotate":
                    creature.rotate(1.0);
                    //CommandUtility.sendSetTurn(creature.getIndex(), -1.0, -1.0, 3.0);
                    break;
                case "gotoFood":
                    if (food != null) 
                        creature.moveto(3.0, food.getX1(), food.getY1());
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
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getLastAction()
    {
      return(sLastAction);    
    }
    
    public void resetLastAction()
    {
      sLastAction = null;    
    }
}
