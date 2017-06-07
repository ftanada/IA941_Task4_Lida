package modules;

import edu.memphis.ccrg.lida.sensorymemory.SensoryMemoryImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ws3dproxy.model.Thing;

public class SensoryMemory extends SensoryMemoryImpl 
{
    private Map<String, Object> sensorParam;
    private Thing food;
    private Thing jewel;
    private Thing wall;
    private List<Thing> thingAhead;
    private Thing leafletJewel;
    private double energy;
    private Thing genericThing;
    private boolean openBottom;
    private boolean openLeft;
    private boolean openRight;
    private boolean openTop;

    public SensoryMemory() {
        this.sensorParam = new HashMap<>();
        this.food = null;
        this.jewel = null;
        this.wall = null;
        this.thingAhead = new ArrayList<>();
        this.leafletJewel = null;
        this.genericThing = null;
        openBottom = false;
        openLeft = false;
        openRight = false;
        openTop = false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void runSensors() {
        sensorParam.clear();
        /*sensorParam.put("mode", "f");
        food = (Thing) environment.getState(sensorParam);
        sensorParam.clear();
        sensorParam.put("mode", "jewel");
        jewel = (Thing) environment.getState(sensorParam);
        sensorParam.clear();
        sensorParam.put("mode", "thingAhead");
        thingAhead = (List<Thing>) environment.getState(sensorParam);
        sensorParam.clear();
        sensorParam.put("mode", "leafletJewel");
        leafletJewel = (Thing) environment.getState(sensorParam);
        // FMT 01/06/2017
        sensorParam.clear();
        sensorParam.put("mode", "wall");
        wall = (Thing) environment.getState(sensorParam);
        sensorParam.put("mode", "openBottom");
        openBottom = (boolean) environment.getState(sensorParam);
        sensorParam.put("mode", "openRight");
        openRight = (boolean) environment.getState(sensorParam);
        sensorParam.clear();
        sensorParam.put("mode", "openLeft");
        openLeft = (boolean) environment.getState(sensorParam);
        sensorParam.clear();
        sensorParam.put("mode", "openTop");
        openTop = (boolean) environment.getState(sensorParam);*/
        sensorParam.put("mode", "wall");
        wall = (Thing) environment.getState(sensorParam);        
    }

    @Override
    public Object getSensoryContent(String modality, Map<String, Object> params) {
        Object requestedObject = null;
        String mode = (String) params.get("mode");
        // FMT System.out.println("getSensoryContent: "+mode);
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
            case "wall":
                requestedObject = wall;
                break;
            case "energy":
                requestedObject = energy;
                break;
            case "object":
                requestedObject = genericThing;
                break;
            case "openBottom":
                requestedObject = openBottom;
                break;
            case "openLeft":
                requestedObject = openLeft;
                break;
            case "openRight":
                requestedObject = openRight;
                break;
            case "openTop":
                requestedObject = openTop;
                break;
            default:
                break;
        }
        return requestedObject;
    }

    @Override
    public Object getModuleContent(Object... os) {
        return null;
    }

    @Override
    public void decayModule(long ticks) {
    }
}
