/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package detectors;

/**
 *
 * @author ftanada
 */

import java.util.HashMap;
import java.util.Map;

import edu.memphis.ccrg.lida.framework.shared.Linkable;
import edu.memphis.ccrg.lida.pam.PerceptualAssociativeMemory;
import edu.memphis.ccrg.lida.pam.tasks.BasicDetectionAlgorithm;
import edu.memphis.ccrg.lida.sensorymemory.SensoryMemory;

public class ObjectDetector extends BasicDetectionAlgorithm{
	
	private Map<String, Object> detectorParams = new HashMap<String, Object>();
	
	/**
	 * Default constructor. Associated {@link Linkable},
	 * {@link PerceptualAssociativeMemory} and {@link SensoryMemory} must be set
	 * using setters.
	 */
	public ObjectDetector() {
	}

	@Override
	public void init() {
		super.init();
		Integer position = (Integer)getParam("position", 0);
		detectorParams.put("position", position);
		String obj = (String)getParam("object", "wall");
		detectorParams.put("object", obj);
	}

	
	@Override
	public double detect() {
		if((Boolean) sensoryMemory.getSensoryContent("", detectorParams)){
			return 1.0;
		}
		return 0.0;
	}
}
