/**
 * Copyright (c) 2012 Washington University in St. Louis. All Rights Reserved.
 */
package edu.wustl.xipHost.application;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.ws.Endpoint;
import org.apache.log4j.Logger;
import org.dcm4che2.data.Tag;
import org.jdom.Document;
import org.nema.dicom.wg23.ArrayOfString;
import org.nema.dicom.wg23.ArrayOfUUID;
import org.nema.dicom.wg23.AvailableData;
import org.nema.dicom.wg23.Host;
import org.nema.dicom.wg23.ModelSetDescriptor;
import org.nema.dicom.wg23.ObjectDescriptor;
import org.nema.dicom.wg23.ObjectLocator;
import org.nema.dicom.wg23.QueryResult;
import org.nema.dicom.wg23.Rectangle;
import org.nema.dicom.wg23.State;
import org.nema.dicom.wg23.Uuid;
import edu.wustl.xipHost.dataAccess.DataSource;
import edu.wustl.xipHost.dataAccess.Query;
import edu.wustl.xipHost.dataAccess.QueryEvent;
import edu.wustl.xipHost.dataAccess.QueryListener;
import edu.wustl.xipHost.dataAccess.Retrieve;
import edu.wustl.xipHost.dataAccess.RetrieveEvent;
import edu.wustl.xipHost.dataAccess.RetrieveFactory;
import edu.wustl.xipHost.dataAccess.RetrieveListener;
import edu.wustl.xipHost.dataAccess.RetrieveTarget;
import edu.wustl.xipHost.iterator.Criteria;
import edu.wustl.xipHost.iterator.IteratorUtil;
import edu.wustl.xipHost.iterator.IterationTarget;
import edu.wustl.xipHost.iterator.IteratorElementEvent;
import edu.wustl.xipHost.iterator.IteratorEvent;
import edu.wustl.xipHost.iterator.NotificationRunner;
import edu.wustl.xipHost.iterator.TargetElement;
import edu.wustl.xipHost.iterator.TargetIteratorRunner;
import edu.wustl.xipHost.iterator.TargetIteratorListener;
import edu.wustl.xipHost.localFileSystem.LocalFileSystemRetrieve;
import edu.wustl.xipHost.dataModel.AIMItem;
import edu.wustl.xipHost.dataModel.ImageItem;
import edu.wustl.xipHost.dataModel.Item;
import edu.wustl.xipHost.dataModel.Patient;
import edu.wustl.xipHost.dataModel.SearchResult;
import edu.wustl.xipHost.dataModel.Series;
import edu.wustl.xipHost.dataModel.Study;
import edu.wustl.xipHost.dicom.DicomUtil;
import edu.wustl.xipHost.gui.HostMainWindow;
import edu.wustl.xipHost.hostControl.Util;
import edu.wustl.xipHost.hostControl.XindiceManager;
import edu.wustl.xipHost.hostControl.XindiceManagerFactory;
import edu.wustl.xipHost.wg23.ClientToApplication;
import edu.wustl.xipHost.wg23.HostImpl;
import edu.wustl.xipHost.wg23.NativeModelListener;
import edu.wustl.xipHost.wg23.NativeModelRunner;
import edu.wustl.xipHost.wg23.StateExecutor;
import edu.wustl.xipHost.wg23.WG23DataModel;

public class Application implements NativeModelListener, TargetIteratorListener, QueryListener, RetrieveListener {	
	final static Logger logger = Logger.getLogger(Application.class);
	UUID id;
	String name;
	String exePath;
	String vendor;
	String version;
	File iconFile;
	String iconFilePath;
	String type;
	boolean requiresGUI;
	String wg23DataModelType;
	int concurrentInstances;
	IterationTarget iterationTarget;
	int numStateNotificationThreads = 2;
	boolean isValid;
	ExecutorService exeService = Executors.newFixedThreadPool(numStateNotificationThreads);	
	
	public Application(String name, String exePath, String vendor, String version, String iconFile,
			String type, boolean requiresGUI, String wg23DataModelType, int concurrentInstances, IterationTarget iterationTarget){
		id = UUID.randomUUID();
		this.name = name;
		this.exePath = exePath;
		this.vendor = vendor;
		this.version = version;
		iconFilePath = iconFile;
		if(iconFile != null){
			File file = new File(iconFile);
			if(file.exists()){
				this.iconFile = file;
			}else{
				this.iconFile = null;
				if(!iconFilePath.isEmpty()){
					logger.warn("Application: " + getName() + " icon file path: " + iconFilePath + " is not valid.");
				}
			}
		}
		this.type = type;
		this.requiresGUI = requiresGUI;
		this.wg23DataModelType = wg23DataModelType;
		this.concurrentInstances = concurrentInstances;
		this.iterationTarget = iterationTarget;
		if(name == null || exePath == null || type == null || wg23DataModelType == null || iterationTarget == null){
			isValid = false;
		} else if(name.isEmpty() || name.trim().length() == 0 || exePath.trim().isEmpty() ||
				type.isEmpty() || wg23DataModelType.isEmpty() || concurrentInstances == 0){
			isValid = false;
		} else {
			File exeFile = new File(exePath);
			if(exeFile.exists()){
				isValid = true;
			} else {
				isValid = false;
				logger.warn("Application: " + getName() + " exePath: " + exePath + " is not valid.");
			}
		}
	}
		
	public UUID getID(){
		return id;
	}		
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
		if(name == null || name.isEmpty() || name.trim().length() == 0) {
			isValid = false;
		}
	}
	public String getExePath(){
		return exePath;
	}
	
	public void setExePath(String path){
		exePath = path;
		if(exePath == null) {
			isValid = false;
			return;
		}
		File exeFile = new File(exePath);
		if(exeFile.exists()){
			isValid = true;
		} else {
			isValid = false;
			logger.warn("Application: " + getName() + " exePath: " + exePath + " is not valid.");
		}
	}
	public String getVendor(){
		return vendor;
	}
	public void setVendor(String vendor){
		this.vendor = vendor;
	}
	public String getVersion(){
		return version;
	}
	public void setVersion(String version){
		this.version = version;	
	}
		
	public String getIconPath(){
		return iconFilePath;
	}
	public void setIconPath(String iconFilePath){
		this.iconFilePath = iconFilePath;
		if(iconFilePath != null){
			File file = new File(iconFilePath);
			if(file.exists()){
				this.iconFile = file;
			}else{
				this.iconFile = null;
				if(!this.iconFilePath.isEmpty()){
					logger.warn("Application: " + getName() + " icon file path: " + this.iconFilePath + " is not valid.");
				}
			}
		}
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		if(type == null || type.isEmpty()) {
			isValid = false;
		}
	}

	public boolean requiresGUI() {
		return requiresGUI;
	}

	public void setRequiresGUI(boolean requiresGUI) {
		this.requiresGUI = requiresGUI;
	}

	public String getWG23DataModelType() {
		return wg23DataModelType;
	}

	public void setWG23DataModelType(String wg23DataModelType) {
		this.wg23DataModelType = wg23DataModelType;
		if(wg23DataModelType == null || wg23DataModelType.isEmpty()) {
			isValid = false;
		}
	}

	public int getConcurrentInstances() {
		return concurrentInstances;
	}

	public void setConcurrentInstances(int concurrentInstances) {
		this.concurrentInstances = concurrentInstances;
		if( concurrentInstances == 0) {
			isValid = false;
		}
	}

	public IterationTarget getIterationTarget() {
		return iterationTarget;
	}

	public void setIterationTarget(IterationTarget iterationTarget) {
		this.iterationTarget = iterationTarget;
		if(iterationTarget == null) {
			isValid = false;
		}
	}
	
	public boolean isValid(){
		return isValid;
	}
	
	
	ClientToApplication clientToApplication;
	public void startClientToApplication(){
		clientToApplication = new ClientToApplication(getApplicationServiceURL());
	}
	public ClientToApplication getClientToApplication(){
		return clientToApplication;
	}
	
	
	//Implementation HostImpl is used to be able to add WG23Listener
	//It is eventually casted to Host type
	Host host;
			
	//All loaded application by default will be saved again.
	//New instances of an application will be saved only when the save check box is selected
	Boolean doSave = true;
	public void setDoSave(boolean doSave){
		this.doSave = doSave;
	}
	public boolean getDoSave(){
		return doSave;
	}
	
	Endpoint hostEndpoint;
	URL hostServiceURL;
	URL appServiceURL;
	Thread threadNotification;
	public void launch(URL hostServiceURL, URL appServiceURL){						
		this.hostServiceURL = hostServiceURL;
		this.appServiceURL = appServiceURL;				
		setApplicationOutputDir(ApplicationManagerFactory.getInstance().getOutputDir());		
		setApplicationPreferredSize(HostMainWindow.getApplicationPreferredSize());
		//prepare native models
		//createNativeModels(getWG23DataModel());		
		//diploy host service				
		host = new HostImpl(this);	
		hostEndpoint = Endpoint.publish(hostServiceURL.toString(), host);
		if(getExePath().endsWith(".exe") || getExePath().endsWith(".bat")){
			try {																							
				Runtime.getRuntime().exec("cmd /c start /min " + getExePath() + " " + "--hostURL" + " " + hostServiceURL.toURI().toURL().toExternalForm() + " " + "--applicationURL" + " " + appServiceURL.toURI().toURL().toExternalForm());		         
			} catch (IOException e) {			
				logger.error(e, e);				
			} catch (URISyntaxException e) {
				logger.error(e, e);	
			}			
		} else if (getExePath().endsWith(".sh")){
			//Mac OS X compatible
			//To be able to run Runtime.exec() on the Mac OS X parameters must be passed via String[] instead of one String
			// sh files must have a executable mode and reside in XIPApp/bin directory
			try {												
				/*String[] cmdarray = {getExePath().getAbsolutePath(), "--hostURL", hostServiceURL.toURI().toURL().toExternalForm(),
						"--applicationURL", appServiceURL.toURI().toURL().toExternalForm()};*/
				String[] cmdarray = {"/usr/X11/bin/xterm", "-e", getExePath(), "--hostURL", hostServiceURL.toURI().toURL().toExternalForm(),
						"--applicationURL", appServiceURL.toURI().toURL().toExternalForm(), " ; le_exec"};
				logger.debug("Launching hosted application. Application name: " + getName() + " --hostURL " + hostServiceURL.toURI().toURL().toExternalForm() + "--applicationURL" + " " + appServiceURL.toURI().toURL().toExternalForm());
				Runtime.getRuntime().exec(cmdarray) ;
			} catch (IOException e) {			
				logger.error(e, e);	
			} catch (URISyntaxException e) {
				logger.error(e, e);	
			}		
		} else {
			try {
				Runtime.getRuntime().exec(getExePath() + " " + "--hostURL" + " " + hostServiceURL.toURI().toURL().toExternalForm() + " " + "--applicationURL" + " " + appServiceURL.toURI().toURL().toExternalForm());					
			} catch (IOException e) {
				logger.error(e, e);	
			} catch (URISyntaxException e) {
				logger.error(e, e);	
			}		
		}
		TargetIteratorRunner targetIter = new TargetIteratorRunner(selectedDataSearchResult, getIterationTarget(), query, this);
		try {
			Thread t = new Thread(targetIter);
			t.start();
		} catch(Exception e) {
			logger.error(e, e);
		}
	}	
	
	public Endpoint getHostEndpoint(){
		return hostEndpoint;
	}
		
	File appOutputDir;
	public void setApplicationOutputDir(File outDir){				
		try {
			appOutputDir = Util.create("xipOUT_" + getName() + "_", "", outDir);
		} catch (IOException e) {
			logger.error(e, e);
		}		
	}
	public File getApplicationOutputDir() {		
		return appOutputDir;
	}

	File appTmpDir;
	public void setApplicationTmpDir(File tmpDir){
		appTmpDir = tmpDir;
	}
	public File getApplicationTmpDir() {
		return appTmpDir;		
	}

	
	
	java.awt.Rectangle preferredSize;
	public void setApplicationPreferredSize(java.awt.Rectangle preferredSize){
		this.preferredSize = preferredSize;
	}
	
	
	State priorState = null;
	State state = null;
	boolean firstLaunch = true;
	int numberOfSentNotifications = 0;
	public void setState(State state){
		priorState = this.state;
		this.state = state;
		logger.debug("\"" + getName() + "\"" + " state changed to: " + this.state);
		if (state.equals(State.IDLE)){
			if(firstLaunch){
				startClientToApplication();
				notifyAddSideTab();
				firstLaunch = false;
				StateExecutor stateExecutor = new StateExecutor(this);
				stateExecutor.setState(State.INPROGRESS);
				exeService.execute(stateExecutor);
			} else {
				//Check if prior State was CANSELED, If yes proceed with change state to EXIT. If not to INPROGRESS.
				if(priorState.equals(State.CANCELED)){
					StateExecutor stateExecutor = new StateExecutor(this);
					stateExecutor.setState(State.EXIT);
					exeService.execute(stateExecutor);
					return;
				}
				synchronized(this){
					if(iter != null){
						boolean doInprogress = true;
						synchronized(targetElements){
							if(numberOfSentNotifications == targetElements.size()){
								doInprogress = false;
							}
						}
						if(doInprogress == false){
							StateExecutor stateExecutor = new StateExecutor(this);
							stateExecutor.setState(State.EXIT);
							exeService.execute(stateExecutor);
						} else {
							StateExecutor stateExecutor = new StateExecutor(this);
							stateExecutor.setState(State.INPROGRESS);
							exeService.execute(stateExecutor);
						}			
					} else {
						StateExecutor stateExecutor = new StateExecutor(this);
						stateExecutor.setState(State.INPROGRESS);
						exeService.execute(stateExecutor);
					}
				}
			}
		} else if(state.equals(State.INPROGRESS)){
			synchronized(targetElements){
				while(targetElements.size() == 0 || targetElements.size() <= numberOfSentNotifications){
					try {
						targetElements.wait();
					} catch (InterruptedException e) {
						logger.error(e, e);
					}
				}
				TargetElement element = targetElements.get(numberOfSentNotifications);
				AvailableData availableData = IteratorUtil.getAvailableData(element, retrieveTarget);
				NotificationRunner runner = new NotificationRunner(this);
				runner.setAvailableData(availableData);
				threadNotification = new Thread(runner);
				threadNotification.start();
				numberOfSentNotifications++;
			}
		} else if (state.equals(State.CANCELED)){
			StateExecutor stateExecutor = new StateExecutor(this);
			stateExecutor.setState(State.IDLE);
			exeService.execute(stateExecutor);
		} else if (state.equals(State.EXIT)){
			//Application runShutDownSequence goes through ApplicationTerminator and Application Scheduler
			//ApplicationScheduler time is set to zero but other value could be used when shutdown delay is needed.
			ApplicationTerminator terminator = new ApplicationTerminator(this);
			Thread t = new Thread(terminator);
			t.start();	
			//reset application parameters for subsequent launch
			firstLaunch = true;
			retrievedTargetElements.clear();
			targetElements.clear();
			iter = null;
			numberOfSentNotifications = 0;
			retrievedTargetElements.clear();
		}
	}
	
	public State getState(){
		return state;
	}
	public State getPriorState(){
		return priorState;
	}
	
	WG23DataModel wg23dm = null;
	public void setData(WG23DataModel wg23DataModel){
		this.wg23dm = wg23DataModel;		
	}

	public WG23DataModel getWG23DataModel(){
		return wg23dm;
	}
	
	SearchResult selectedDataSearchResult;
	public void setSelectedDataSearchResult(SearchResult selectedDataSearchResult){
		this.selectedDataSearchResult = selectedDataSearchResult;
	}
	
	Query query;
	public void setQueryDataSource(Query query){
		this.query = query;
	}
	
	Retrieve retrieve;
	public void setRetrieveDataSource(Retrieve retrieve){
		this.retrieve = retrieve;
	}
	
	public Rectangle getApplicationPreferredSize() {		
		double x = preferredSize.getX();
		double y = preferredSize.getY();
		double width = preferredSize.getWidth();
		double height = preferredSize.getHeight();
		Rectangle rect = new Rectangle();
		rect.setRefPointX(new Double(x).intValue());
		rect.setRefPointY(new Double(y).intValue());
		rect.setWidth(new Double(width).intValue());
		rect.setHeight(new Double(height).intValue());
		return rect;
	}
	
	public URL getApplicationServiceURL(){
		return appServiceURL;
	}		
		
	public void notifyAddSideTab(){	
		HostMainWindow.addTab(getName(), getID());
	}
		
	public void bringToFront(){
		clientToApplication.bringToFront();
	}
	
	public boolean shutDown(){		
		if(getState().equals(State.IDLE)){
			if(getClientToApplication().setState(State.EXIT)){
				return true;
			}		
		}else{
			cancelProcessing();
			/* if(cancelProcessing()){
				return shutDown();
			}	*/		
		}
		return false;
	}
	
	public void runShutDownSequence(){
		HostMainWindow.removeTab(getID());
		if(getHostEndpoint() != null){
			getHostEndpoint().stop();
		}
		fireTerminateApplication();
		/* voided to make compatibile with the iterator
		//Delete documents from Xindice created for this application
		XindiceManagerFactory.getInstance().deleteAllDocuments(getID().toString());
		//Delete collection created for this application
		XindiceManagerFactory.getInstance().deleteCollection(getID().toString());
		*/
	}
	
	public boolean cancelProcessing(){
		if(getState().equals(State.INPROGRESS) || getState().equals(State.SUSPENDED)){
			return getClientToApplication().setState(State.CANCELED);			
		}else{
			return false;
		}		
	}
	public boolean suspendProcessing(){
		if(getState().equals(State.INPROGRESS)){
			return getClientToApplication().setState(State.SUSPENDED);			
		}else{
			return false;
		}
	}
	
	
	/**
	 * Method is used to create XML native models for all object locators
	 * found  in WG23DataModel.
	 * It uses threads and add NativeModelListener to the NativeModelRunner
	 * @param wg23dm
	 */
	void createNativeModels(WG23DataModel wg23dm){
		if(XindiceManagerFactory.getInstance().createCollection(getID().toString())){
			ObjectLocator[] objLocs = wg23dm.getObjectLocators();
			for (int i = 0; i < objLocs.length; i++){										
				boolean isDICOM = false;
				try {
					isDICOM = DicomUtil.isDICOM(new File(new URI(objLocs[i].getUri())));
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(isDICOM){
					NativeModelRunner nmRunner;
					nmRunner = new NativeModelRunner(objLocs[i]);
					nmRunner.addNativeModelListener(this);
					Thread t = new Thread(nmRunner);
					t.start();
				}								
			}
		}else{
			//TODO
			//Action when system cannot create collection
		}
		
	}
	
	/**
	 * Adds JDOM Document to Xindice collection.
	 * Only valid documents (e.g. not null, with root element) will be added
	 * (non-Javadoc)
	 * @see edu.wustl.xipHost.wg23.NativeModelListener#nativeModelAvailable(org.jdom.Document, org.nema.dicom.wg23.Uuid)
	 */
	public void nativeModelAvailable(Document doc, Uuid objUUID) {				
		XindiceManagerFactory.getInstance().addDocument(doc, getID().toString(), objUUID);		
	}
		
	public void nativeModelAvailable(String xmlNativeModel) {
		// Ignore in XIP Host. 
		// Used by AVT AD		
	}	

	/**
	 * Method returns ModelSetDescriptor containing UUID of native models
	 * as well as UUID of object locators for which native models could
	 * not be created
	 * @param objUUIDs
	 * @return
	 */	
	 public ModelSetDescriptor getModelSetDescriptor(List<Uuid> objUUIDs){				
		String[] models = XindiceManagerFactory.getInstance().getModelUUIDs(getID().toString());							
		List<String> listModels = Arrays.asList(models);
		ModelSetDescriptor msd = new ModelSetDescriptor();
		ArrayOfUUID uuidsModels = new ArrayOfUUID();
		List<Uuid> listUUIDs = uuidsModels.getUuid();
		ArrayOfUUID uuidsFailed = new ArrayOfUUID();
		List<Uuid> listUUIDsFailed = uuidsFailed.getUuid();
		for(int i = 0; i < objUUIDs.size(); i++){
			Uuid uuid = new Uuid();
			if(objUUIDs.get(i) == null || objUUIDs.get(i).getUuid() == null){
				//do not add anything to model set descriptor
			}else if(objUUIDs.get(i).getUuid().toString().trim().isEmpty()){
				//do not add anything to model set descriptor
			}else if(listModels.contains("wg23NM-"+ objUUIDs.get(i).getUuid())){				
				int index = listModels.indexOf("wg23NM-"+ objUUIDs.get(i).getUuid());
				uuid.setUuid(listModels.get(index));
				listUUIDs.add(uuid);
			}else{
				uuid.setUuid(objUUIDs.get(i).getUuid());
				listUUIDsFailed.add(uuid);
			}		
		}					
		msd.setModels(uuidsModels);
		msd.setFailedSourceObjects(uuidsFailed);		
		return msd;
	}
	
	/**
	 * queryResults list hold teh values from queryResultAvailable
	 */
	List<QueryResult> queryResults;
	public List<QueryResult> queryModel(List<Uuid> modelUUIDs, List<String> listXPaths){
		queryResults = new ArrayList<QueryResult>();
		if(modelUUIDs == null || listXPaths == null){
			return queryResults;
		}
		String collectionName = getID().toString();		
		XindiceManager xm = XindiceManagerFactory.getInstance();			
		for(int i = 0; i < listXPaths.size(); i++){
			for(int j = 0; j < modelUUIDs.size(); j++){
				//String[] results = xm.query(service, collectionName, modelUUIDs.get(j), listXPaths.get(i));
				String[] results = xm.query(collectionName, modelUUIDs.get(j), listXPaths.get(i));
				QueryResult queryResult = new QueryResult();
				queryResult.setModel(modelUUIDs.get(j));
				queryResult.setXpath(listXPaths.get(i));
				ArrayOfString arrayOfString = new ArrayOfString();
				List<String> listString = arrayOfString.getString();
				for(int k = 0; k < results.length; k++){							
					listString.add(results[k]);
				}		
				queryResult.setResults(arrayOfString);	
				queryResults.add(queryResult);
			}
		}				
		return queryResults;		
	}

	Iterator<TargetElement> iter;
	@SuppressWarnings("unchecked")
	@Override
	public void fullIteratorAvailable(IteratorEvent e) {
		iter = (Iterator<TargetElement>)e.getSource();
		logger.debug("Full TargetIterator available at time " + System.currentTimeMillis());
	}

	List<TargetElement> targetElements = new ArrayList<TargetElement>();
	@Override
	public void targetElementAvailable(IteratorElementEvent e) {
		synchronized(targetElements){
			TargetElement element = (TargetElement) e.getSource();
			logger.debug("TargetElement available. ID: " + element.getId() + " at time " + System.currentTimeMillis());
			targetElements.add(element);
			targetElements.notify();
		}
	}
	
	String dataSourceDomainName;
	public void setDataSourceDomainName(String dataSourceDomainName){
		this.dataSourceDomainName = dataSourceDomainName;
	}
	
	DataSource dataSource;
	public void setDataSource(DataSource dataSource){
		this.dataSource = dataSource;
	}
	
	RetrieveTarget retrieveTarget;
	public void setRetrieveTarget(RetrieveTarget retrieveTarget){
		this.retrieveTarget = retrieveTarget;
	}
	
	public List<ObjectLocator> retrieveAndGetLocators(List<Uuid> listUUIDs){
		//First check if data was already retrieved
		boolean retrieveComplete = true;
		List<ObjectLocator> listObjLocs = new ArrayList<ObjectLocator>();		
		if(!retrievedData.isEmpty()){
			for(Uuid uuid : listUUIDs){
				String strUuid = uuid.getUuid();
				ObjectLocator objLoc = retrievedData.get(strUuid);
				if(objLoc != null){
					logger.debug(strUuid + " " + objLoc.getUri());
					listObjLocs.add(objLoc);
				} else {					
					retrieveComplete = false;
					listObjLocs.clear();
					break;
				}
			}
			if(retrieveComplete){					
				return listObjLocs;
			}
		}
		//Start data retrieval related to the element	
		//RetrieveTarget retrieveTarget = RetrieveTarget.DICOM_AND_AIM;
		TargetElement element = null;
		synchronized(targetElements){
			element = targetElements.get(numberOfSentNotifications - 1);
			//1. Find targetElement where uuids are; Optimization task
			
			Retrieve retrieve = RetrieveFactory.getInstance(dataSourceDomainName);				
			File importDir = getApplicationTmpDir();
			retrieve.setImportDir(importDir);
			retrieve.setDataSource(dataSource);
			retrieve.addRetrieveListener(this);
			SearchResult subSearchResult = element.getSubSearchResult();
			Criteria originalCriteria = subSearchResult.getOriginalCriteria();
			Map<Integer, Object> dicomCriteria = originalCriteria.getDICOMCriteria();
			Map<String, Object> aimCriteria = originalCriteria.getAIMCriteria();
			List<Patient> patients = subSearchResult.getPatients();
			for(Patient patient : patients){
				dicomCriteria.put(Tag.PatientName, patient.getPatientName());
				dicomCriteria.put(Tag.PatientID, patient.getPatientID());
				List<Study> studies = patient.getStudies();
				for(Study study : studies){
					dicomCriteria.put(Tag.StudyInstanceUID, study.getStudyInstanceUID());
					List<Series> series = study.getSeries();
					for(Series oneSeries : series){					
						dicomCriteria.put(Tag.SeriesInstanceUID, oneSeries.getSeriesInstanceUID());
						if(aimCriteria == null){
							logger.debug("AD AIM criteria: " + aimCriteria);
						}else{
							logger.debug("AD AIM retrieve criteria:");
							Set<String> keys = aimCriteria.keySet();
							Iterator<String> iter = keys.iterator();
							while(iter.hasNext()){
								String key = iter.next();
								String value = (String) aimCriteria.get(key);
								if(!value.isEmpty()){
									logger.debug("Key: " + key + " Value: " + value);
								}					
							}				
						}
						List<ObjectDescriptor> objectDescriptors = new ArrayList<ObjectDescriptor>();
						List<Item> items = oneSeries.getItems();
						for(Item item : items){
							objectDescriptors.add(item.getObjectDescriptor());
						}
						
						//If oneSeries contains subset of items, narrow dicomCriteria to individual SOPInstanceUIDs
						//Then retrieve data item by item
						if(oneSeries.containsSubsetOfItems()){
							for(Item item : items){
								dicomCriteria.remove(Tag.SOPInstanceUID);
								aimCriteria.remove("ImageAnnotation.uniqueIdentifier");
								if(item instanceof ImageItem) {
									String itemSOPInstanceUID = item.getItemID();
									dicomCriteria.put(Tag.SOPInstanceUID, itemSOPInstanceUID);
								} else if (item instanceof AIMItem) {
									String aimUID = item.getItemID();
									aimCriteria.put("ImageAnnotation.uniqueIdentifier", aimUID);
								}
								if(retrieve instanceof LocalFileSystemRetrieve){
									retrieve.setCriteria(selectedDataSearchResult);
								} else {
									retrieve.setCriteria(dicomCriteria, aimCriteria);
								}
								List<ObjectDescriptor> objectDesc = new ArrayList<ObjectDescriptor>();
								objectDesc.add(item.getObjectDescriptor());
								if(item instanceof ImageItem) {
									retrieve.setRetrieveTarget(RetrieveTarget.DICOM);
								} else if (item instanceof AIMItem) {
									retrieve.setRetrieveTarget(RetrieveTarget.AIM_SEG);
								}
								retrieve.setObjectDescriptors(objectDesc);
								Thread t = new Thread(retrieve);
								t.start();
								try {
									t.join();
									//Reset value of SOPInstanceUID in dicomCriteria
									dicomCriteria.remove(Tag.SOPInstanceUID);
								} catch (InterruptedException e) {
									logger.error(e, e);
								}
							}
						} else {
							retrieve.setRetrieveTarget(retrieveTarget);
							dicomCriteria.put(Tag.SOPInstanceUID, "*");
							if(retrieve instanceof LocalFileSystemRetrieve){
								retrieve.setCriteria(selectedDataSearchResult);
							} else {
								retrieve.setCriteria(dicomCriteria, aimCriteria);
								retrieve.setObjectDescriptors(objectDescriptors);
							}														
							Thread t = new Thread(retrieve);
							t.start();
							try {
								t.join();
							} catch (InterruptedException e) {
								logger.error(e, e);
							}
						}						
						//Reset Series level dicomCriteria
						dicomCriteria.remove(Tag.SeriesInstanceUID);
					}
					//Reset Study level dicomCriteria
					dicomCriteria.remove(Tag.StudyInstanceUID);
				}
				//Reset Patient level dicomCriteria
				dicomCriteria.remove(Tag.PatientName);
				dicomCriteria.remove(Tag.PatientID);
			}				
		}			
		//Wait for actual data being retrieved before sending file pointers
		synchronized(retrievedData){
			while(retrievedData.isEmpty()){
				try {
					retrievedData.wait();
				} catch (InterruptedException e) {
					logger.error(e, e);
				}
			}
		}
		if(listUUIDs == null){
			return new ArrayList<ObjectLocator>();
		} else {		
			for(Uuid uuid : listUUIDs){
				String strUuid = uuid.getUuid();
				ObjectLocator objLoc = retrievedData.get(strUuid);
				if(objLoc != null){
					logger.debug(strUuid + " " + objLoc.getUri());
					listObjLocs.add(objLoc);		
				} else {
					logger.warn("Host can't find data for the UUID: " + strUuid);
				}	
			}
			return listObjLocs;
		}			
	}
	
	
	@Override
	public void notifyException(String message) {
		logger.warn(message);
	}


	@Override
	public void queryResultsAvailable(QueryEvent e) {
		// TODO Auto-generated method stub
		
	}

	List<String> retrievedTargetElements = new ArrayList<String>();	
	Map <String, ObjectLocator> retrievedData = new HashMap<String, ObjectLocator>();
	@SuppressWarnings("unchecked")
	@Override
	public void retrieveResultsAvailable(RetrieveEvent e) {
		synchronized(retrievedData){			
			Map<String, ObjectLocator> objectLocators = (Map<String, ObjectLocator>) e.getSource();
			retrievedData.putAll(objectLocators);
			Iterator<String> keySet = objectLocators.keySet().iterator();
			logger.debug("Items retrieved: ");
			while(keySet.hasNext()){
				String uuid = keySet.next();
				ObjectLocator objLoc = objectLocators.get(uuid);
				logger.debug("UUID: " + uuid + " Item location: " + objLoc.getUri());
			}		
			retrievedData.notify();
		}
	}
	
	ApplicationTerminationListener applicationTerminationListener;
	public void addApplicationTerminationListener(ApplicationTerminationListener applicationTerminationListener){
		this.applicationTerminationListener = applicationTerminationListener;
	}
	
	void fireTerminateApplication(){
		ApplicationTerminationEvent event = new ApplicationTerminationEvent(this);
		applicationTerminationListener.applicationTerminated(event);
	}

	/**
	 * Used in JUnit testing when setting the test fixture
	 * @param numberOfSentNotifications
	 */
	public void setNumberOfSentNotifications(int numberOfSentNotifications) {
		this.numberOfSentNotifications = numberOfSentNotifications;
	}

	/**
	 * Used in JUnit testing when setting the test fixture
	 * @param targetElements
	 */
	public void setTargetElements(List<TargetElement> targetElements) {
		this.targetElements = targetElements;
	}
}
