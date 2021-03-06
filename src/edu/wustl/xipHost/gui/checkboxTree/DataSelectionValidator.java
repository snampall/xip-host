/**
 * Copyright (c) 2011 Washington University in St. Louis. All Rights Reserved.
 */
package edu.wustl.xipHost.gui.checkboxTree;

import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.log4j.Logger;
import edu.wustl.xipHost.gui.ExceptionDialog;

/**
 * @author Jaroslaw Krych
 *
 */
public class DataSelectionValidator {
	final static Logger logger = Logger.getLogger(DataSelectionValidator.class);
	
	public static boolean isDataSelected(DefaultMutableTreeNode rootNode){
		//check if selectedDataSearchresult is not null and at least one PatientNode is selected
		boolean isDataSelected = false;
		if(rootNode != null){
			if(rootNode.getChildCount() != 0){
				DefaultMutableTreeNode locationNode = (DefaultMutableTreeNode) rootNode.getFirstChild();
				int numOfPatients = locationNode.getChildCount();
				if (numOfPatients == 0){
					logger.warn("No data is selected");
					new ExceptionDialog("Cannot launch selected application.", 
							"No dataset selected. Please query and select data nodes.",
							"Launch Application Dialog");
					return false;
				} else {
					for(int i = 0; i < numOfPatients; i++){
						PatientNode existingPatientNode = (PatientNode) locationNode.getChildAt(i);
						if(existingPatientNode.isSelected() == true){
							isDataSelected = true;
							return isDataSelected;
						} else {
							int numOfStudies = existingPatientNode.getChildCount();
							for(int j = 0; j < numOfStudies; j++){
								StudyNode existingStudyNode = (StudyNode)existingPatientNode.getChildAt(j);
								if(existingStudyNode.isSelected() == true){
									isDataSelected = true;
									return isDataSelected;
								} else {
									int numOfSeries = existingStudyNode.getChildCount();
									for(int k = 0; k < numOfSeries; k++){
										SeriesNode existingSeriesNode = (SeriesNode)existingStudyNode.getChildAt(k);
										if(existingSeriesNode.isSelected() == true){
											isDataSelected = true;
											return isDataSelected;
										} else {
											int numOfItems = existingSeriesNode.getChildCount();
											for(int m = 0; m < numOfItems; m++){
												ItemNode existingItemNode = (ItemNode)existingSeriesNode.getChildAt(m);
												if(existingItemNode.isSelected() == true){
													isDataSelected = true;
													return isDataSelected;
												}
											}
										}
									}
								}
							}
						}
					}
					if(isDataSelected == false){
						logger.warn("No data is selected");
						new ExceptionDialog("Cannot launch selected application.", 
								"No dataset selected. Please select data nodes.",
								"Launch Application Dialog");
						return false;
					}
				}
			} else {
				logger.warn("No data is selected");
				new ExceptionDialog("Cannot launch selected application.", 
						"No dataset selected. Please query and select data nodes.",
						"Launch Application Dialog");
				return false;
			}
		} else {
			logger.warn("No data is selected");
			new ExceptionDialog("Cannot launch selected application.", 
					"No dataset selected. Please query and select data nodes.",
					"Launch Application Dialog");
			return false;
		}
		return isDataSelected;
	}

}
