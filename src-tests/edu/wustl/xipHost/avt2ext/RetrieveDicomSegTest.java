/**
 * Copyright (c) 2009 Washington University in St. Louis. All Rights Reserved.
 */
package edu.wustl.xipHost.avt2ext;

import static org.junit.Assert.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.siemens.scr.avt.ad.api.ADFacade;
import edu.wustl.xipHost.avt2ext.AVTFactory;

/**
 * @author Jaroslaw Krych
 *
 */
public class RetrieveDicomSegTest {
	static ADFacade adService;
	static File avt2ext_seg_retrieve_dir;
	
	@BeforeClass
	public static void setUp()throws Exception {
		adService = AVTFactory.getADServiceInstance();
	}
	
	@Before
	public void setUpBeforeTest() throws Exception {
		avt2ext_seg_retrieve_dir = new File("./test-content/AVT2EXT_SEG_Retrieve");
		if(avt2ext_seg_retrieve_dir.exists() == false) {
			avt2ext_seg_retrieve_dir.mkdir();
		} else {
			File[] files = avt2ext_seg_retrieve_dir.listFiles();
			if(files.length > 0) {
				for(int i = 0 ; i < files.length; i++) {
					File file = files[i];
					file.delete();
				}
			}
		}
	}

	@After
	public void tearDownAfterTest() throws Exception {
		File[] files = avt2ext_seg_retrieve_dir.listFiles();
		for(int i = 0 ; i < files.length; i++) {
			File file = files[i];
			file.delete();
		}
	}
	
	
	//INFO: dataset must be preloaded prior to running these JUnit tests from AD_Preload_JUnit_Tests. Use PreloadDICOM and PreloadAIM utility classes in avt2ext to preload database.
	@Test
	public void testRetrieveDicomSegTest_1A() throws IOException{
		String aimUID2 = "1.3.6.1.4.1.5962.99.1.1772356583.1829344988.1264492774375.3.0";
		List<DicomObject> dicomSegObjs = adService.retrieveSegmentationObjects(aimUID2);
		int numSegObjects;
		boolean dicomSEG1 = false;
		boolean dicomSEG2 = false;
		for(numSegObjects = 0; numSegObjects < dicomSegObjs.size(); numSegObjects++){
    		DicomObject dicom = dicomSegObjs.get(numSegObjects);
    		String sopInstanceUID = dicom.getString(Tag.SOPInstanceUID);
    		DicomObject segDicom = adService.getDicomObject(sopInstanceUID);
    		String dicomSEGSOPInstanceUID = segDicom.getString(Tag.SOPInstanceUID);
			String dirPath = new File("./test-content/AVT2EXT_SEG_Retrieve").getCanonicalPath() + File.separator;
    		String fileName = dirPath + sopInstanceUID + ".dcm";
			File file = new File(fileName);
			DicomOutputStream dout = new DicomOutputStream(new FileOutputStream(fileName));
			dout.writeDicomFile(segDicom);
			dout.close();
			long fileLength = (file.length() / 1024);		
			
			if(dicomSEGSOPInstanceUID.equalsIgnoreCase("1.2.276.0.7230010.3.1.4.2554264370.29928.1264492790.3")){
				dicomSEG1 = true;
			} else if(dicomSEGSOPInstanceUID.equalsIgnoreCase("1.2.276.0.7230010.3.1.4.2554264370.29928.1264492801.5")){
				dicomSEG2 = true;
			}			
			//Assert retrieved file size
			assertTrue("Retrieved DICOM SEG object file size is zero KB", fileLength > 0);
    	}
		//Assert number of retrieved DICOM SEG objects
		assertEquals("Number of retrieved DICOM SEG object is not as expected.", 2, numSegObjects);
		//Assert DICOM SEG object SOPInstanceUID
		assertTrue("Wrong retrieved DICOM SEG objects.", dicomSEG1 == true && dicomSEG2 == true);
	}
}
