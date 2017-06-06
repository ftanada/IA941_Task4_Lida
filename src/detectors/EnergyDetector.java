/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
/**
 *
 * @author ftanada
 */
package detectors;

import java.util.HashMap;
import java.util.Map;

import edu.memphis.ccrg.lida.pam.PamLinkable;
import edu.memphis.ccrg.lida.pam.tasks.DetectionAlgorithm;
import edu.memphis.ccrg.lida.pam.tasks.MultipleDetectionAlgorithm;

public class EnergyDetector extends MultipleDetectionAlgorithm implements DetectionAlgorithm {

	private PamLinkable highEnergy;
	private PamLinkable lowEnergy;
	private final String modality = "";
	private Map<String, Object> detectorParams = new HashMap<String, Object>();
	
	@Override
	public void init(){
		super.init();
		highEnergy = pamNodeMap.get("highEnergy");
		lowEnergy = pamNodeMap.get("lowEnergy");
		detectorParams.put("mode","energy");
	}
	
	@Override
	public void detectLinkables() {
		double energyValue = (Double)sensoryMemory.getSensoryContent(modality, detectorParams);

		if (energyValue > 0.33){
			pam.receiveExcitation(highEnergy, 1.0);
		}else {
			pam.receiveExcitation(lowEnergy, 1.0);
		}
		
	}
}

    
