/**
 * Copyright (c) 2010 Washington University in St. Louis. All Rights Reserved.
 */
package edu.wustl.xipHost.dataAccess;

import java.sql.Timestamp;
import java.util.Iterator;
import org.apache.log4j.Logger;
import edu.wustl.xipHost.dataModel.Patient;
import edu.wustl.xipHost.dataModel.SearchResult;
import edu.wustl.xipHost.dataModel.Series;
import edu.wustl.xipHost.dataModel.Study;

/**
 * @author Jaroslaw Krych
 *
 */
public class Util {
	final static Logger logger = Logger.getLogger(Util.class);
	
	public static void searchResultToLog(SearchResult result){
		if(logger.isDebugEnabled()){
			 Iterator<Patient> patients = result.getPatients().iterator();
			 while(patients.hasNext()){
				 Patient patient = patients.next();
				 Timestamp patientLastUpdated = patient.getLastUpdated();
				 String strPatientLastUpdated = null;
				 if(patientLastUpdated != null){
					 strPatientLastUpdated = patientLastUpdated.toString();
				 }
				 logger.debug(patient.toString() + " Last updated: " + strPatientLastUpdated);
				 Iterator<Study> studies = patient.getStudies().iterator();
				 while(studies.hasNext()){
					 Study study = studies.next();
					 Timestamp studyLastUpdated = study.getLastUpdated();
					 String strStudyLastUpdated = null;
					 if(studyLastUpdated != null){
						 strStudyLastUpdated = studyLastUpdated.toString();
					 }
					 logger.debug(study.toString() + " Last updated: " + strStudyLastUpdated);
					 Iterator<Series> series = study.getSeries().iterator();
					 while(series.hasNext()){
						 Series oneSeries = series.next();
						 Timestamp seriesLastUpdated = oneSeries.getLastUpdated();
						 String strSeriesLastUpdated = null;
						 if(seriesLastUpdated != null){
							 strSeriesLastUpdated = seriesLastUpdated.toString();
						 }
						 logger.debug(oneSeries.toString() + " Last updated: " + strSeriesLastUpdated);
					 }
				 }
			 }
		}
	}
}
