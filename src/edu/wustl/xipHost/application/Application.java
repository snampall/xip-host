/**
 * Copyright (c) 2008 Washington University in Saint Louis. All Rights Reserved.
 */
package edu.wustl.xipHost.application;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.xml.ws.Endpoint;
import org.jdom.Document;
import org.nema.dicom.wg23.ArrayOfString;
import org.nema.dicom.wg23.ArrayOfUUID;
import org.nema.dicom.wg23.AvailableData;
import org.nema.dicom.wg23.Host;
import org.nema.dicom.wg23.ModelSetDescriptor;
import org.nema.dicom.wg23.ObjectLocator;
import org.nema.dicom.wg23.QueryResult;
import org.nema.dicom.wg23.Rectangle;
import org.nema.dicom.wg23.State;
import org.nema.dicom.wg23.Uuid;

import edu.wustl.xipHost.avt2ext.AVTUtil;
import edu.wustl.xipHost.avt2ext.iterator.IteratorElementEvent;
import edu.wustl.xipHost.avt2ext.iterator.IteratorEvent;
import edu.wustl.xipHost.avt2ext.iterator.TargetElement;
import edu.wustl.xipHost.avt2ext.iterator.TargetIterator;
import edu.wustl.xipHost.avt2ext.iterator.TargetIteratorListener;
import edu.wustl.xipHost.dicom.DicomUtil;
import edu.wustl.xipHost.gui.HostMainWindow;
import edu.wustl.xipHost.hostControl.Util;
import edu.wustl.xipHost.hostControl.XindiceManager;
import edu.wustl.xipHost.hostControl.XindiceManagerFactory;
import edu.wustl.xipHost.wg23.ClientToApplication;
import edu.wustl.xipHost.wg23.HostImpl;
import edu.wustl.xipHost.wg23.NativeModelListener;
import edu.wustl.xipHost.wg23.NativeModelRunner;
import edu.wustl.xipHost.wg23.WG23DataModel;

public class Application implements NativeModelListener, TargetIteratorListener {	
	UUID id;
	String name;
	File exePath;
	String vendor;
	String version;
	File iconFile;
	
	/* Application is a WG23 compatibile application*/	
	public Application(String name, File exePath, String vendor, String version, File iconFile){								
		if(name == null || exePath == null || vendor == null || version == null){
			throw new IllegalArgumentException("Application parameters are invalid: " + 
					name + " , " + exePath + " , " + vendor + " , " + version);	
		} else if(name.isEmpty() || name.trim().length() == 0 || exePath.exists() == false){
			try {
				throw new IllegalArgumentException("Application parameters are invalid: " + 
						name + " , " + exePath.getCanonicalPath() + " , " + vendor + " , " + version);
			} catch (IOException e) {
				throw new IllegalArgumentException("Application exePath is invalid. Application name: " + 
						name);
			}
		} else{
			id = UUID.randomUUID();
			this.name = name;
			this.exePath = exePath;
			this.vendor = vendor;
			this.version = version;
			if(iconFile != null && iconFile.exists()){
				this.iconFile = iconFile;
			}else{
				this.iconFile = null;
			}
			
		}		
	}
	
	
	//verify this pattern
	/*public boolean verifyFileName(String fileName){		
		String str = "/ \\ : * ? \" < > | ,  ";		
        Pattern filePattern = Pattern.compile(str);             
        boolean matches = filePattern.matcher(fileName).matches();
        return matches;
    }
	
	public static void main (String args[]){
		Application app = new Application("ApplicationTest", new File("test.txt"), "", "");
		System.out.println(app.getExePath().getName());
		System.out.println(app.verifyFileName(app.getExePath().getName()));
	}*/		
		
	public UUID getID(){
		return id;
	}		
	public String getName(){
		return name;
	}
	public void setName(String name){
		if(name == null || name.isEmpty() || name.trim().length() == 0){
			throw new IllegalArgumentException("Invalid application name: " + name);
		}else{
			this.name = name;
		}		
	}
	public File getExePath(){
		return exePath;
	}
	public void setExePath(File path){
		if(path == null){
			throw new IllegalArgumentException("Invalid exePath name: " + path);
		}else{
			exePath = path;
		}		
	}
	public String getVendor(){
		return vendor;
	}
	public void setVendor(String vendor){
		if(vendor == null){
			throw new IllegalArgumentException("Invalid vendor: " + vendor);
		}else{
			this.vendor = vendor;
		}		
	}
	public String getVersion(){
		return version;
	}
	public void setVersion(String version){
		if(version == null){
			throw new IllegalArgumentException("Invalid version: " + version);
		}else{
			this.version = version;
		}		
	}
		
	public File getIconFile(){
		return iconFile;
	}
	public void setIconFile(File iconFile){
		if(iconFile == null){
			throw new IllegalArgumentException("Invalid exePath name: " + iconFile);
		}else{
			this.iconFile = iconFile;
		}	
	}
	
	
	//Each application has:
	//1. Out directories assigned
	//2. clientToApplication
	//3. Host scheleton (reciever)
	//4. Data assigned for processing
	//5. Data produced
	//when launching diploy service and set URLs
	
	ClientToApplication clientToApplication;
	public void startClientToApplication(){
		clientToApplication = new ClientToApplication(getApplicationServiceURL());
	}
	public ClientToApplication getClientToApplication(){
		return clientToApplication;
	}
	
	
	//Implementation HostImpl is used to be able to add WG23Listener
	//It is eventually casted to Host type
	Host host = new HostImpl(this);	
			
	//All loaded application by default will be saved again.
	//New instances of an application will be saved only when the save checkbox is selected
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
	public void launch(URL hostServiceURL, URL appServiceURL){						
		this.hostServiceURL = hostServiceURL;
		this.appServiceURL = appServiceURL;				
		setApplicationOutputDir(ApplicationManagerFactory.getInstance().getOutputDir());
		setApplicationTmpDir(ApplicationManagerFactory.getInstance().getTmpDir());
		setApplicationPreferredSize(HostMainWindow.getApplicationPreferredSize());
		//prepare native models
		//createNativeModels(getWG23DataModel());		
		//diploy host service				
		hostEndpoint = Endpoint.publish(hostServiceURL.toString(), host);
		// Ways of launching XIP application: exe, bat, class or jar
		//if(((String)getExePath().getName()).endsWith(".exe") || ((String)getExePath().getName()).endsWith(".bat")){
		try {
			if(getExePath().toURI().toURL().toExternalForm().endsWith(".exe") || getExePath().toURI().toURL().toExternalForm().endsWith(".bat")){
				//TODO unvoid
				try {																							
					Runtime.getRuntime().exec("cmd /c start /min " + getExePath().toURI().toURL().toExternalForm() + " " + "--hostURL" + " " + hostServiceURL.toURI().toURL().toExternalForm() + " " + "--applicationURL" + " " + appServiceURL.toURI().toURL().toExternalForm());		         
		            /*List< String > command = new LinkedList<String>();		            
		            command.add("cmd");
		            command.add("/c");
		            command.add("start");
		            command.add("/min");		            		            		            
		            command.add(getExePath().getCanonicalPath());		            		            
		            command.add("--hostURL");		            
		            command.add(hostServiceURL.toURI().toURL().toExternalForm());		           
		            command.add( "--applicationURL" );
		            command.add(appServiceURL.toURI().toURL().toExternalForm());		            
		            ProcessBuilder builder = new ProcessBuilder(command);
		            String str ="";
		            for(int i = 0; i < command.size(); i++){
		            	str = str + command.get(i) + " ";
		            }
		            System.out.println(str);		            
		            File dir = getExePath().getParentFile();		            
		            builder.directory(dir);
		            builder.start();*/
				} catch (IOException e) {			
					e.printStackTrace();					
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			} else if (getExePath().toURI().toURL().toExternalForm().endsWith(".sh")){
				try {		
					//Runtime.getRuntime().exec("/bin/sh " + getExePath().getCanonicalPath() + " " + "--hostURL" + " " + hostServiceURL.toURI().toURL().toExternalForm() + " " + "--applicationURL" + " " + appServiceURL.toURI().toURL().toExternalForm());					
					System.out.println(getExePath().toURI().toURL().toExternalForm() + " " + "--hostURL" + " " + hostServiceURL.toURI().toURL().toExternalForm() + " " + "--applicationURL" + " " + appServiceURL.toURI().toURL().toExternalForm());
					Runtime.getRuntime().exec("open " + getExePath().toURI().toURL().toExternalForm() + " " + "--hostURL" + " " + hostServiceURL.toURI().toURL().toExternalForm() + " " + "--applicationURL" + " " + appServiceURL.toURI().toURL().toExternalForm());
					
					/*List<String> command = new ArrayList<String>();																
					command.add("/bin/sh");				
				    command.add(getExePath().getCanonicalPath());
				    command.add("--hostURL");
				    command.add(hostServiceURL.toURI().toURL().toExternalForm());
				    command.add("--applicationURL");
				    command.add(appServiceURL.toURI().toURL().toExternalForm());
				    
				    ProcessBuilder builder = new ProcessBuilder(command);
				    //Map<String, String> environ = builder.environment();
				    //builder.directory(new File(System.getenv("temp")));

				    //System.out.println("Directory : " + System.getenv("temp") );
				    final Process process = builder.start();
				    InputStream is = process.getInputStream();
				    InputStreamReader isr = new InputStreamReader(is);
				    BufferedReader br = new BufferedReader(isr);
				    String line;
				    while ((line = br.readLine()) != null) {
				      System.out.println(line);
				    }
				    System.out.println("Program terminated!");
				    */
				} catch (IOException e) {			
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			} else {
				try {
					Runtime.getRuntime().exec(getExePath().toURI().toURL().toExternalForm() + " " + "--hostURL" + " " + hostServiceURL.toURI().toURL().toExternalForm() + " " + "--applicationURL" + " " + appServiceURL.toURI().toURL().toExternalForm());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// TODO Auto-generated catch block			
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//startIterator
		
	}	
	
	public Endpoint getHostEndpoint(){
		return hostEndpoint;
	}
		
	File appOutputDir;
	public void setApplicationOutputDir(File outDir){				
		try {
			appOutputDir = Util.create("xipOUT_" + getName() + "_", "", outDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public void setState(State state){
		priorState = this.state;
		this.state = state;				
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
			if(cancelProcessing()){
				return shutDown();
			}			
		}
		return false;
	}
	
	public void runShutDownSequence(){
		HostMainWindow.removeTab(getID());		
		getHostEndpoint().stop();		
		//Delete documents from Xindice created for this application
		XindiceManagerFactory.getInstance().deleteAllDocuments(getID().toString());
		//Delete collection created for this application
		XindiceManagerFactory.getInstance().deleteCollection(getID().toString());
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

	TargetIterator iter;
	@Override
	public void fullIteratorAvailable(IteratorEvent e) {
		iter = (TargetIterator)e.getSource();
	}

	
	AVTUtil util = new AVTUtil();
	@Override
	public void targetElementAvailable(IteratorElementEvent e) {
		TargetElement element = (TargetElement) e.getSource();
		WG23DataModel wg23data = util.getWG23DataModel(element);
		AvailableData availableData = wg23data.getAvailableData();			            			
		if (iter == null && getClientToApplication().getState().equals(State.INPROGRESS)) {
			getClientToApplication().notifyDataAvailable(availableData, false);
		} else if (iter != null && getClientToApplication().getState().equals(State.INPROGRESS)) {
			getClientToApplication().notifyDataAvailable(availableData, true);
		}
	}
	
}
