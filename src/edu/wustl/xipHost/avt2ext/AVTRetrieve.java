/**
 * Copyright (c) 2011 Washington University in St. Louis. All Rights Reserved.
 */
package edu.wustl.xipHost.avt2ext;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomOutputStream;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.nema.dicom.wg23.ObjectDescriptor;
import org.nema.dicom.wg23.ObjectLocator;
import org.nema.dicom.wg23.Uuid;
import com.siemens.scr.avt.ad.annotation.ImageAnnotation;
import com.siemens.scr.avt.ad.api.ADFacade;
import edu.wustl.xipHost.dataAccess.DataAccessListener;
import edu.wustl.xipHost.dataAccess.DataSource;
import edu.wustl.xipHost.dataAccess.Retrieve;
import edu.wustl.xipHost.dataAccess.RetrieveEvent;
import edu.wustl.xipHost.dataAccess.RetrieveListener;
import edu.wustl.xipHost.dataAccess.RetrieveTarget;
import edu.wustl.xipHost.dicom.DicomUtil;
import edu.wustl.xipHost.iterator.TargetElement;

/**
 * @author Jaroslaw Krych
 *
 */
public class AVTRetrieve implements Retrieve {
	final static Logger logger = Logger.getLogger(AVTRetrieve.class);
	ADFacade adService = AVTFactory.getADServiceInstance();
	Map<Integer, Object> dicomCriteria;
	Map<String, Object> aimCriteria;
	List<ObjectDescriptor> objectDescriptors;
	File importDir;
	RetrieveTarget retrieveTarget;
	DataSource dataSource;
	
	public AVTRetrieve(){}
	
	/**
	 * 
	 */
	public AVTRetrieve(Map<Integer, Object> dicomCriteria, Map<String, Object> aimCriteria, File importDir, RetrieveTarget retrieveTarget, DataSource dataSource) {
		this.dicomCriteria = dicomCriteria;
		this.aimCriteria = aimCriteria;
		this.importDir = importDir;
		this.retrieveTarget = retrieveTarget;
		this.dataSource = dataSource;
	}
	
	@Override
	public void setCriteria(Map<Integer, Object> dicomCriteria, Map<String, Object> aimCriteria) {
		this.dicomCriteria = dicomCriteria;
		this.aimCriteria = aimCriteria;
	}
	
	@Override
	public void setCriteria(Object criteria) {
			
	}
	
	@Override
	public void setObjectDescriptors(List<ObjectDescriptor> objectDescriptors) {
		this.objectDescriptors = objectDescriptors;		
	}

	@Override
	public void setImportDir(File importDir) {
		this.importDir = importDir;		
	}

	@Override
	public void setRetrieveTarget(RetrieveTarget retrieveTarget) {
		this.retrieveTarget = retrieveTarget;		
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Override
	public void setRetrieve(TargetElement targetElement, RetrieveTarget retrieveTarget) {
		//TODO to be removed
	}
	
	@Override
	public void run() {
		try {
			logger.info("Executing AVT retrieve.");
			Map<String, ObjectLocator> objectLocs = retrieve(dicomCriteria, aimCriteria, importDir, retrieveTarget);
			fireResultsAvailable(objectLocs);
		} catch (IOException e) {
			logger.error(e, e);
			return;
		}
	}
	
	SAXBuilder builder = new SAXBuilder();
	Document document;
	XMLOutputter outToXMLFile = new XMLOutputter();
	Map<String, ObjectLocator> retrieve(Map<Integer, Object> dicomCriteria, Map<String, Object> aimCriteria, File importDir, RetrieveTarget retrieveTarget) throws IOException {		
		logger.debug("DICOM retrieve criteria: ");
		Iterator<Integer> keySet = dicomCriteria.keySet().iterator();
		while(keySet.hasNext()){
			Integer key = keySet.next();
			Object value = dicomCriteria.get(key);
			logger.debug("Tag: " + DicomUtil.toDicomHex(key) + " Value: " + value.toString());
		}
		
		Map<String, ObjectLocator> objectLocators = new HashMap<String, ObjectLocator>();		
		File dirPath = importDir.getAbsoluteFile();				
		if(retrieveTarget == RetrieveTarget.DICOM_AND_AIM){
			//Retrieve DICOM						
			//If oneSeries contains subset of items, narrow dicomCriteria to individual SOPInstanceUIDs
			//Then retrieve data item by item			
			List<DicomObject> retrievedDICOM = adService.retrieveDicomObjs(dicomCriteria, aimCriteria);											
			if(retrievedDICOM.size() == objectDescriptors.size()){
				int i = 0;
				for(i = 0; i < retrievedDICOM.size(); i++){
					DicomObject dicom = retrievedDICOM.get(i);							
					String filePrefix = dicom.getString(Tag.SOPInstanceUID);
					try {
						File file = new File(importDir.getAbsolutePath() + File.separatorChar + filePrefix);
						if(!file.exists()){
							file.createNewFile();
						}
						FileOutputStream fos = new FileOutputStream(file);
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						DicomOutputStream dout = new DicomOutputStream(bos);
						dout.writeDicomFile(dicom);
						dout.close();
						ObjectLocator objLoc = new ObjectLocator();				
						Uuid itemUUID = objectDescriptors.get(i).getUuid();
						objLoc.setUuid(itemUUID);				
						objLoc.setUri(file.getAbsolutePath()); 
						objectLocators.put(itemUUID.getUuid(), objLoc);
					} catch (IOException e) {
						logger.error(e, e);
						return null;
					}									
				}			
				//Retrieve AIM		
				List<String> annotationUIDs = adService.findAnnotations(dicomCriteria, aimCriteria);
				Set<String> uniqueAnnotUIDs = new HashSet<String>(annotationUIDs);
				Iterator<String> iter = uniqueAnnotUIDs.iterator();
				while(iter.hasNext()){
					String uid = iter.next();
					ImageAnnotation loadedAnnot = adService.getAnnotation(uid);			
					String strXML = loadedAnnot.getAIM();
					byte[] source = strXML.getBytes();
					InputStream is = new ByteArrayInputStream(source);
					try {
						document = builder.build(is);
					} catch (JDOMException e) {
						logger.error(e, e);
						return null;
					}	
					//Ensure dirPath is correctly assign. There are references below of this variable
					File outFile = new File(dirPath + File.separator + uid);
					FileOutputStream outStream = new FileOutputStream(outFile);			
					outToXMLFile.output(document, outStream);
			    	outStream.flush();
			    	outStream.close();
			    	//retrievedFiles.add(outFile);
			    	ObjectLocator objLoc = new ObjectLocator();				
			    	Uuid itemUUID = objectDescriptors.get(i).getUuid();
					objLoc.setUuid(itemUUID);				
					objLoc.setUri(outFile.getAbsolutePath()); 
					objectLocators.put(itemUUID.getUuid(), objLoc);
					i++;
			    	//Retrieve DICOM SEG
			    	//temporarily voided. AVTQuery needs to be modified to query for DICOM SEG objects
			    	//
			    	Set<String> dicomSegSOPInstanceUIDs = new HashSet<String>();
			    	List<DicomObject> segObjects = adService.retrieveSegmentationObjects(uid);
			    	for(int j = 0; j < segObjects.size(); j++){
			    		DicomObject dicom = segObjects.get(j);
			    		String sopInstanceUID = dicom.getString(Tag.SOPInstanceUID);
			    		//Check if DICOM SEG was not serialized in reference to another AIM
			    		if(!dicomSegSOPInstanceUIDs.contains(sopInstanceUID)){
			    			dicomSegSOPInstanceUIDs.add(sopInstanceUID);
			    			DicomObject dicomSeg = adService.getDicomObject(sopInstanceUID);
			    			String message = "DICOM SEG " + sopInstanceUID + " cannot be loaded from file system!";
			    			if(dicomSeg == null){			    				
			    				throw new FileNotFoundException(message);
			    			} else {					 
			    				//TODO DICOM SEG tmp file not found e.g. DICOM SEG belongs to not specified Study for which TargetIteratorRunner was not requested					 					    			
		    					File outDicomSegFile = new File(dirPath + File.separator + sopInstanceUID);
	    						FileOutputStream fos = new FileOutputStream(outDicomSegFile);
	    						BufferedOutputStream bos = new BufferedOutputStream(fos);
	    						DicomOutputStream dout = new DicomOutputStream(bos);
	    						dout.writeDicomFile(dicomSeg);
	    						dout.close();
	    						//retrievedFiles.add(outDicomSegFile);
	    						ObjectLocator dicomSegObjLoc = new ObjectLocator();					    						
	    						Uuid dicomSegItemUUID = objectDescriptors.get(i).getUuid();	    						
								dicomSegObjLoc.setUuid(dicomSegItemUUID);				
								dicomSegObjLoc.setUri(outDicomSegFile.getAbsolutePath()); 
								objectLocators.put(dicomSegItemUUID.getUuid(), dicomSegObjLoc);
								i++;
			    			}
			    		}
			    	}	  
				}
			} else {
				logger.warn("Number of retrieved objects does not equals to number of ObjectDescriptors!");
			}
		}
		return objectLocators;
	}		
	
	@Override
	public void addDataAccessListener(DataAccessListener l) {
		//TODO to be removed
		
	}

	@Override
	public Map<String, ObjectLocator> getObjectLocators() {
		//TODO to be removed
		return null;
	}
	
	void fireResultsAvailable(Map<String, ObjectLocator> objectLocators){
		RetrieveEvent event = new RetrieveEvent(objectLocators);         		        
		listener.retrieveResultsAvailable(event);
	}


	RetrieveListener listener;
	@Override
	public void addRetrieveListener(RetrieveListener l) {
		listener = l;
	}
}