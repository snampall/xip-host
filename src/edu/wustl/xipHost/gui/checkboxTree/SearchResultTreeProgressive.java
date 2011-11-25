/**
 * Copyright (c) 2010 Washington University in St. Louis. All Rights Reserved.
 */
package edu.wustl.xipHost.gui.checkboxTree;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import edu.wustl.xipHost.dataModel.Item;
import edu.wustl.xipHost.dataModel.Patient;
import edu.wustl.xipHost.dataModel.SearchResult;
import edu.wustl.xipHost.dataModel.Series;
import edu.wustl.xipHost.dataModel.Study;
import edu.wustl.xipHost.gui.checkboxTree.SearchResultTree;
import edu.wustl.xipHost.gui.checkboxTree.SeriesNode;

/**
 * @author Jaroslaw Krych
 *
 */
public class SearchResultTreeProgressive extends SearchResultTree {

	public SearchResultTreeProgressive(){
		super();
	}
	
	public void updateNodes(SearchResult result) {					    			
		firePropertyChange(JTree.ROOT_VISIBLE_PROPERTY, !isRootVisible(), isRootVisible());
		if(result == null){			
			rootNode.removeAllChildren();
			treeModel.reload(rootNode);
			return;
		}
		DefaultMutableTreeNode locationNode;
		if(rootNode.getChildCount() == 0){
			locationNode = new DefaultMutableTreeNode(result.getDataSourceDescription());
			for(int i = 0; i < result.getPatients().size(); i++){
				final Patient patient = result.getPatients().get(i);
				PatientNode patientNode = new PatientNode(patient){
					public String toString(){															
						String patientDesc = patient.toString();
						if(patientDesc == null){
							patientDesc = "";
						}else{
							
						}	
						return patientDesc;						
					}
					public Patient getUserObject(){
						return patient;
					}					
				};
				patientNode.addNodeSelectionListener(this);
				locationNode.add(patientNode);
			}
		} else {
			locationNode = (DefaultMutableTreeNode) rootNode.getFirstChild();
			int numOfPatientNodes = locationNode.getChildCount();
			PatientNode patientNode = null;
			for(int i = 0; i < result.getPatients().size(); i++){
				final Patient patient = result.getPatients().get(i);
				for(int a = 0; a < numOfPatientNodes; a++){
					PatientNode existingPatientNode = (PatientNode) locationNode.getChildAt(a);					
					Patient existingPatient = (Patient) existingPatientNode.getUserObject();
					if(patient.equals(existingPatient)){
						patientNode = existingPatientNode;
						int numOfStudyNodes = patientNode.getChildCount();
						if(numOfStudyNodes == 0){
							for(int j = 0; j < patient.getStudies().size(); j++){
								final Study study = patient.getStudies().get(j);
								StudyNode studyNode = new StudyNode(study){
									public String toString(){															
										String studyDesc = study.toString();
										if(studyDesc == null){
											studyDesc = "";
										}else{
											
										}	
										return studyDesc;						
									}
									public Study getUserObject(){
										return study;
									}					
								};
								if(patientNode.isSelected()){
									studyNode.setSelected(true);
      		     					studyNode.getCheckBox().setSelected(true);
								}
								studyNode.addNodeSelectionListener(this);
								patientNode.add(studyNode);								
							}
						} else {
							StudyNode studyNode = null;
							for(int j = 0; j < patient.getStudies().size(); j++){
								final Study study = patient.getStudies().get(j);
								for(int b = 0; b < numOfStudyNodes; b++){
									StudyNode existingStudyNode = (StudyNode)patientNode.getChildAt(b);
									Study existingStudy = (Study) existingStudyNode.getUserObject();
									if(study.equals(existingStudy)){
										studyNode = existingStudyNode;
										int numOfSeriesNodes = studyNode.getChildCount();
										if(numOfSeriesNodes == 0){
											for(int k = 0; k < study.getSeries().size(); k++){
												final Series series = study.getSeries().get(k);
												SeriesNode seriesNode = new SeriesNode(series){
													public String toString(){						
														String seriesDesc = series.toString();
														if(seriesDesc == null){
															seriesDesc = "";
														}else{
															
														}	
														return seriesDesc;
													}
													public Series getUserObject(){
														return series;
													}
												};
												if(studyNode.isSelected()){
													seriesNode.setSelected(true);
				      		     					seriesNode.getCheckBox().setSelected(true);
												}
												seriesNode.addNodeSelectionListener(this);
												studyNode.add(seriesNode);
											}
										} else {
											SeriesNode seriesNode = null;
											for(int k = 0; k < study.getSeries().size(); k++){
												final Series series = study.getSeries().get(k);
												SeriesNode existingSeriesNode = (SeriesNode)studyNode.getChildAt(k);
												Series existingSeries = (Series)existingSeriesNode.getUserObject();
												if(series.equals(existingSeries)){
													seriesNode = existingSeriesNode;
													int numOfItemNodes = seriesNode.getChildCount();
													if(numOfItemNodes == 0){
														for(int m = 0; m < series.getItems().size(); m++){
															final Item item = series.getItems().get(m);
															ItemNode itemNode = new ItemNode(item);
															if(seriesNode.isSelected()){
																itemNode.setSelected(true);
							      		     					itemNode.getCheckBox().setSelected(true);
															}
															itemNode.addNodeSelectionListener(this);
															seriesNode.add(itemNode);
														}	
													} 
												} 
											}	
										}
									}
								}
							}
						}
					}
				}
			}
		}
		rootNode.add(locationNode);				
		treeModel.nodeChanged(rootNode);
		treeModel.reload(rootNode);
		expandAll();		
	}
}
