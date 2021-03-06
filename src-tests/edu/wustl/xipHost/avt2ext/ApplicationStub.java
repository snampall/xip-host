/**
 * Copyright (c) 2009 Washington University in St. Louis. All Rights Reserved.
 */
package edu.wustl.xipHost.avt2ext;

import org.nema.dicom.wg23.ArrayOfObjectLocator;
import edu.wustl.xipHost.application.Application;
import edu.wustl.xipHost.iterator.IterationTarget;

/**
 * @author Jaroslaw Krych
 *
 */
public class ApplicationStub extends Application {

	/**
	 * 
	 */
	public ApplicationStub(String name, String exePath, String vendor, String version, String iconFile,
			String type, boolean requiresGUI, String wg23DataModelType, int concurrentInstances, IterationTarget iterationTarget) {
		super(name, exePath, vendor, version, iconFile, type, requiresGUI, wg23DataModelType, concurrentInstances, iterationTarget);
	}

	ClientToApplicationStub clientToApplication = new ClientToApplicationStub();
	public ClientToApplicationStub getClientToApplication(){
		return clientToApplication;
	}
	
	public void setObjectLocators(ArrayOfObjectLocator arrayObjLocs){
		clientToApplication.setObjectLocators(arrayObjLocs);
	}
	
}
