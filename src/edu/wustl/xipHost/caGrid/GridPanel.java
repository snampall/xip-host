/**
 * Copyright (c) 2009 Washington University in St. Louis. All Rights Reserved.
 */
package edu.wustl.xipHost.caGrid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;
import org.nema.dicom.wg23.State;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.ShortStringAttribute;
import com.pixelmed.dicom.SpecificCharacterSet;
import com.pixelmed.dicom.TagFromName;
import edu.wustl.xipHost.application.AppButton;
import edu.wustl.xipHost.application.Application;
import edu.wustl.xipHost.application.ApplicationEvent;
import edu.wustl.xipHost.application.ApplicationListener;
import edu.wustl.xipHost.application.ApplicationManager;
import edu.wustl.xipHost.application.ApplicationManagerFactory;
import edu.wustl.xipHost.avt2ext.AVTQuery;
import edu.wustl.xipHost.avt2ext.AVTRetrieve2;
import edu.wustl.xipHost.caGrid.GridUtil;
import edu.wustl.xipHost.dataAccess.DataAccessListener;
import edu.wustl.xipHost.dataAccess.Query;
import edu.wustl.xipHost.dataAccess.QueryEvent;
import edu.wustl.xipHost.dataAccess.Retrieve;
import edu.wustl.xipHost.dataAccess.RetrieveEvent;
import edu.wustl.xipHost.dataModel.Item;
import edu.wustl.xipHost.dataModel.Patient;
import edu.wustl.xipHost.dataModel.SearchResult;
import edu.wustl.xipHost.dataModel.Series;
import edu.wustl.xipHost.dataModel.Study;
import edu.wustl.xipHost.gui.HostMainWindow;
import edu.wustl.xipHost.gui.SearchCriteriaPanel;
import edu.wustl.xipHost.gui.UnderDevelopmentDialog;
import edu.wustl.xipHost.gui.checkboxTree.DataSelectionEvent;
import edu.wustl.xipHost.gui.checkboxTree.DataSelectionListener;
import edu.wustl.xipHost.gui.checkboxTree.DataSelectionValidator;
import edu.wustl.xipHost.gui.checkboxTree.NodeSelectionListener;
import edu.wustl.xipHost.gui.checkboxTree.PatientNode;
import edu.wustl.xipHost.gui.checkboxTree.SearchResultTree;
import edu.wustl.xipHost.gui.checkboxTree.StudyNode;
import edu.wustl.xipHost.iterator.IterationTarget;
import edu.wustl.xipHost.localFileSystem.FileManager;
import edu.wustl.xipHost.localFileSystem.FileManagerFactory;
import gov.nih.nci.cagrid.cqlquery.CQLQuery;
import gov.nih.nci.ivi.helper.AIMTCGADataServiceHelper;


/**
 * @author Jaroslaw Krych
 *
 */
public class GridPanel extends JPanel implements ActionListener, ApplicationListener, DataAccessListener, DataSelectionListener {	
	final static Logger logger = Logger.getLogger(GridPanel.class);
	JPanel locationSelectionPanel = new JPanel();
	JLabel lblTitle = new JLabel("Select caGRID DICOM Service Location:");		
	ImageIcon iconGlobus = new ImageIcon("./gif/applications-internet.png");	
	JLabel lblGlobus = new JLabel(iconGlobus, JLabel.CENTER);		
    DefaultComboBoxModel comboModel;	
	JComboBox list;	
	SearchCriteriaPanel criteriaPanel = new SearchCriteriaPanel();			
	JPanel leftPanel = new JPanel();
	RightPanel rightPanel = new RightPanel();
	SearchResultTree resultTree;
	JProgressBar progressBar = new JProgressBar();	
	Font font_1 = new Font("Tahoma", 0, 13);
	Font font_2 = new Font("Tahoma", 0, 12);		
	Color xipColor = new Color(51, 51, 102);
	Color xipBtn = new Color(56, 73, 150);
	Color xipLightBlue = new Color(156, 162, 189);
	GridManager gridMgr;
	NodeSelectionListener nodeSelectionListener = new NodeSelectionListener();
	DataAccessListener l;
	
	public GridPanel(){
		l = this;
		nodeSelectionListener.addDataSelectionListener(this);
		setBackground(xipColor);							
		comboModel = new DefaultComboBoxModel();
		list = new JComboBox(comboModel);
		gridMgr = GridManagerFactory.getInstance();
		List<GridLocation> gridTypeDicomLocations = gridMgr.getGridTypeDicomLocations();
		for(int i = 0; i < gridTypeDicomLocations.size(); i++){
			comboModel.addElement(gridTypeDicomLocations.get(i));
		}
		List<GridLocation> gridTypeAimLocations = gridMgr.getGridTypeAimLocations();
		for(int i = 0; i < gridTypeAimLocations.size(); i++){
			rightPanel.comboModel.addElement(gridTypeAimLocations.get(i));
		}		
		ComboBoxRenderer renderer = new ComboBoxRenderer();		
		list.setRenderer(renderer);
		list.setMaximumRowCount(10);
		//list.setSelectedIndex(-1);
		list.setSelectedIndex(0);
		GridLocation itemDICOM = (GridLocation)list.getSelectedItem();
		selectedGridTypeDicomService = (GridLocation)itemDICOM;
		list.setPreferredSize(new Dimension(465, 25));
		list.setFont(font_2);
		list.setEditable(false);		
		list.addActionListener(this);
		//rightPanel.list.setSelectedIndex(-1);
		rightPanel.list.setSelectedIndex(0);
		GridLocation itemAIM = (GridLocation)rightPanel.list.getSelectedItem();
		selectedGridTypeAimService = (GridLocation)itemAIM;			
		rightPanel.list.addActionListener(this);
		resultTree = rightPanel.getGridJTreePanel(); 
		//resultTree.addTreeSelectionListener(this);		
		resultTree.addMouseListener(ml);
		rightPanel.lblGlobus.addMouseListener(
			new MouseAdapter(){
				public void mouseClicked(MouseEvent e){																
					//TODO display and manage LocationsDilog
					/*new LocationsDialog(HostConfigurator.getHostConfigurator().getMainWindow(), gridMgr.getGridTypeAimLocations());					
					comboModel.removeAllElements();
					List<GridLocation> gridTypeDicomLocations = gridMgr.getGridTypeDicomLocations();
					for(int i = 0; i < gridTypeDicomLocations.size(); i++){
						comboModel.addElement(gridTypeDicomLocations.get(i));
					}
					rightPanel.comboModel.removeAllElements();
					List<GridLocation> gridTypeAimLocations = gridMgr.getGridTypeAimLocations();
					for(int i = 0; i < gridTypeAimLocations.size(); i++){
						rightPanel.comboModel.addElement(gridTypeAimLocations.get(i));
					}
					list.update(list.getGraphics());*/
					new UnderDevelopmentDialog(rightPanel.lblGlobus.getLocationOnScreen());
				}
			}
		);
		//rightPanel.cbxAnnot.addItemListener(this);
		Border border1 = BorderFactory.createLoweredBevelBorder();
		list.setBorder(border1);		
		lblTitle.setForeground(Color.WHITE);
		locationSelectionPanel.add(lblTitle);		
		locationSelectionPanel.add(list);
		lblGlobus.setToolTipText("DICOM service locations");
		lblGlobus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblGlobus.addMouseListener(
			new MouseAdapter(){
				public void mouseClicked(MouseEvent e){																					
					//TODO display and manage LocationsDilog
					/*new LocationsDialog(HostConfigurator.getHostConfigurator().getMainWindow(), gridMgr.getGridTypeDicomLocations());					
					comboModel.removeAllElements();
					List<GridLocation> gridTypeDicomLocations = gridMgr.getGridTypeDicomLocations();
					for(int i = 0; i < gridTypeDicomLocations.size(); i++){
						comboModel.addElement(gridTypeDicomLocations.get(i));
					}
					rightPanel.comboModel.removeAllElements();
					List<GridLocation> gridTypeAimLocations = gridMgr.getGridTypeAimLocations();
					for(int i = 0; i < gridTypeAimLocations.size(); i++){
						rightPanel.comboModel.addElement(gridTypeAimLocations.get(i));
					}
					list.update(list.getGraphics());*/
					new UnderDevelopmentDialog(lblGlobus.getLocationOnScreen());
				}
			}
		);
		locationSelectionPanel.add(lblGlobus);		
		locationSelectionPanel.setBackground(xipColor);		
		buildLayoutLocationSelectionPanel();
		criteriaPanel.getQueryButton().addActionListener(this);
		criteriaPanel.setQueryButtonText("Search");		
		leftPanel.add(criteriaPanel);
		leftPanel.add(locationSelectionPanel);
		leftPanel.setBackground(xipColor);
		buildLeftPanelLayout();
		add(leftPanel);
		add(rightPanel);

		HostMainWindow.getHostIconBar().getApplicationBar().addApplicationListener(this);

		progressBar.setIndeterminate(false);
	    progressBar.setString("");	    
	    progressBar.setStringPainted(true);	    
	    progressBar.setBackground(new Color(156, 162, 189));
	    progressBar.setForeground(xipColor);
	    add(progressBar);
	    	    
		buildLayout();
	}
	
	void buildLayout(){				
		GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        setLayout(layout);         
                
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 0;        
        constraints.insets.top = 10;
        constraints.insets.left = 20;        
        constraints.insets.bottom = 5;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(leftPanel, constraints);        
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 0;        
        constraints.insets.top = 10;
        constraints.insets.left = 5;
        constraints.insets.right = 20;
        constraints.insets.bottom = 5;        
        constraints.anchor = GridBagConstraints.WEST;
        layout.setConstraints(rightPanel, constraints);
        
        constraints.fill = GridBagConstraints.HORIZONTAL;        
        constraints.gridx = 0;
        constraints.gridy = 1; 
        constraints.gridwidth = 2;
        constraints.insets.top = 10;
        constraints.insets.left = 0;
        constraints.insets.right = 0;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(progressBar, constraints);
	}
	
	void buildLayoutLocationSelectionPanel(){
		GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        locationSelectionPanel.setLayout(layout);         
                       
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 0;        
        constraints.insets.top = 10;
        constraints.insets.left = 20;        
        constraints.insets.bottom = 5;        
        constraints.anchor = GridBagConstraints.WEST;
        layout.setConstraints(lblTitle, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 1;        
        constraints.insets.top = 10;
        constraints.insets.left = 20;        
        constraints.insets.bottom = 5;        
        constraints.anchor = GridBagConstraints.WEST;
        layout.setConstraints(list, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 1;        
        constraints.insets.top = 10;
        constraints.insets.left = 20;        
        constraints.insets.bottom = 5;        
        constraints.anchor = GridBagConstraints.WEST;
        layout.setConstraints(lblGlobus, constraints);                
	}	
	
	void buildLeftPanelLayout(){
		GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        leftPanel.setLayout(layout);         
                       
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 0;        
        constraints.insets.top = 10;
        constraints.insets.left = 20;
        constraints.insets.right = 10;
        constraints.insets.bottom = 5;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(locationSelectionPanel, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 1;        
        constraints.insets.top = 10;
        constraints.insets.left = 20;
        constraints.insets.right = 10;
        constraints.insets.bottom = 5;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(criteriaPanel, constraints); 
	}
	
	
	GridLocation selectedGridTypeDicomService;
	GridLocation selectedGridTypeAimService;	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == list){
			Object item = ((JComboBox)e.getSource()).getSelectedItem();
			selectedGridTypeDicomService = (GridLocation)item;			
		}else if(e.getSource() == rightPanel.list){
			Object item = ((JComboBox)e.getSource()).getSelectedItem();
			selectedGridTypeAimService = (GridLocation)item;			
		}else if(e.getSource() == criteriaPanel.getQueryButton()){
			logger.info("Starting GRID query.");
			//Remove existing children from the result JTree and reload JTree
			resultTree.getRootNode().removeAllChildren();
			resultTree.getTreeModel().reload(resultTree.getRootNode());
			rightPanel.cbxAnnot.setEnabled(true);
			progressBar.setString("Processing search request ...");
			progressBar.setIndeterminate(true);
			progressBar.updateUI();							
			setCriteriaList(criteriaPanel.getFilterList());				
			Boolean bln = criteriaPanel.verifyCriteria(criteria);
			if(bln && selectedGridTypeDicomService != null){											
				GridUtil gridUtil = gridMgr.getGridUtil();
				CQLQuery cql = gridUtil.convertToCQLStatement(criteria, CQLTargetName.PATIENT);							
				activeSubqueryMonitor = false;
				GridQuery gridQuery = new GridQuery(cql, selectedGridTypeDicomService, null, null);				
				gridQuery.addDataAccessListener(this);
				Thread t = new Thread(gridQuery); 					
				t.start();									
			}else{
				progressBar.setString("");
				progressBar.setIndeterminate(false);
			}
		}
	}	
	
	AttributeList criteria;	
	void setCriteriaList(AttributeList criteria){
		this.criteria = criteria;
	}
	//getCriteria, map criteria to NCIA Model Map, verify criteria	
	
	
	class ComboBoxRenderer extends JLabel implements ListCellRenderer {
		DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		Dimension preferredSize = new Dimension(440, 15);
		public Component getListCellRendererComponent(JList list, Object value, int index,
			      boolean isSelected, boolean cellHasFocus) {
			    JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
			        isSelected, cellHasFocus);
			    if (value instanceof GridLocation) {
			    	renderer.setText(((GridLocation)value).getShortName());
			    	renderer.setBackground(Color.WHITE);
			    }
			    if(cellHasFocus || isSelected){
			    	renderer.setBackground(xipLightBlue);
			    	renderer.setForeground(Color.WHITE);
			    	renderer.setBorder(new LineBorder(Color.DARK_GRAY));
			    }else{
			    	renderer.setBorder(null);
			    }			    
			    renderer.setPreferredSize(preferredSize);
			    return renderer;
			  }
		
	}
	
	SearchResult result;
	boolean activeSubqueryMonitor;
	boolean subqueryCompleted;
	@Override
	public void queryResultsAvailable(QueryEvent e) {
		synchronized(this){
			GridQuery gridQuery = (GridQuery)e.getSource();
			result = gridQuery.getSearchResult();
			if(activeSubqueryMonitor == true){
				subqueryCompleted = true;
			}
			synchronized(result){
				rightPanel.getGridJTreePanel().updateNodes2(result);
				if(activeSubqueryMonitor){
					result.notify();
				}
				activeSubqueryMonitor = true;
			}
		}	
		progressBar.setString("GridSearch finished");
		progressBar.setIndeterminate(false);
	}
	
	@Override
	public void notifyException(String message) {		
		progressBar.setIndeterminate(false);
		progressBar.invalidate();
		progressBar.setForeground(Color.RED);
		progressBar.validate();
		progressBar.setString("Exception: " + message);
		result = null;							
		rightPanel.getGridJTreePanel().updateNodes2(result);								
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				resultTree.scrollRowToVisible(queryNodeIndex);
				int visibleRows = resultTree.getVisibleRowCount();
				resultTree.scrollRowToVisible(queryNodeIndex + visibleRows);
			}			 
		});
		
	}
	
	List<CQLQuery> getDicomRetrieveCriteria() {
		List<CQLQuery> retrieveCriterias = new ArrayList<CQLQuery>();
		Map<Series, Study> map = getSelectedSeries();
		Set<Series> seriesSet = map.keySet();
		Iterator<Series> iter = seriesSet.iterator();
		while (iter.hasNext()){
			Series series = iter.next();
			String selectedSeriesInstanceUID = series.getSeriesInstanceUID();			
			String selectedStudyInstanceUID = ((Study)map.get(series)).getStudyInstanceUID();
			CQLQuery cqlQuery = null;	
			GridUtil gridUtil = gridMgr.getGridUtil();
			AttributeList attList = new AttributeList();
			try {
				String[] characterSets = { "ISO_IR 100" };
				SpecificCharacterSet specificCharacterSet = new SpecificCharacterSet(characterSets);			
				{ AttributeTag t = TagFromName.StudyInstanceUID; Attribute a = new ShortStringAttribute(t,specificCharacterSet); a.addValue(selectedStudyInstanceUID); attList.put(t,a); }
				{ AttributeTag t = TagFromName.SeriesInstanceUID; Attribute a = new ShortStringAttribute(t,specificCharacterSet); a.addValue(selectedSeriesInstanceUID); attList.put(t,a); }
				{ AttributeTag t = TagFromName.SpecificCharacterSet; Attribute a = new CodeStringAttribute(t); a.addValue(characterSets[0]); attList.put(t,a); }			
			}
			catch (Exception e) {
				e.printStackTrace(System.err);			
			}
			cqlQuery = gridUtil.convertToCQLStatement(attList, CQLTargetName.SERIES);
			retrieveCriterias.add(cqlQuery);
		}
		return retrieveCriterias;
	}	
	
	List<CQLQuery> getAimRetrieveCriteria() {
		List<CQLQuery> retrieveCriterias = new ArrayList<CQLQuery>();
		Map<Series, Study> map = getSelectedSeries();
		Set<Series> seriesSet = map.keySet();
		Iterator<Series> iter = seriesSet.iterator();
		while (iter.hasNext()){
			Series series = iter.next();
			String selectedSeriesInstanceUID = series.getSeriesInstanceUID();			
			String selectedStudyInstanceUID = ((Study)map.get(series)).getStudyInstanceUID();			
			//CQLQuery aimCQL = AIMDataServiceHelper.generateImageAnnotationQuery(selectedStudyInstanceUID, selectedSeriesInstanceUID, null);;
			CQLQuery aimCQL = AIMTCGADataServiceHelper.generateImageAnnotationQuery(selectedStudyInstanceUID, selectedSeriesInstanceUID, null);
			retrieveCriterias.add(aimCQL);
		}
		return retrieveCriterias;
	}
	
	Object selectedNode;
	int queryNodeIndex = 0;
	boolean wasDoubleClick = false;
	MouseListener ml = new MouseAdapter(){
	     public void mouseClicked(final MouseEvent e) {	        
	    	 	int x = e.getX();
		     	int y = e.getY();
		     	nodeSelectionListener.setSearchResultTree(resultTree);
		     	nodeSelectionListener.setSelectionCoordinates(x, y);
		     	nodeSelectionListener.setSearchResult(result);
			    int row = resultTree.getRowForLocation(x, y);
			    final TreePath  path = resultTree.getPathForRow(row);
	    	 	if (e.getClickCount() == 2) {
		        	wasDoubleClick = true;
		        	subqueryCompleted = false;
		        	nodeSelectionListener.setWasDoubleClick(wasDoubleClick);
			     	if (path != null) {    		
			     		DefaultMutableTreeNode queryNode = (DefaultMutableTreeNode)resultTree.getLastSelectedPathComponent();		    
			     		if (queryNode == null) return;		 
			     		if (!queryNode.isRoot()) {
			     			queryNodeIndex = resultTree.getRowForPath(new TreePath(queryNode.getPath()));
			     			selectedNode = queryNode.getUserObject();			     			
			     			AttributeList initialCriteria = criteriaPanel.getFilterList();
			     			if(selectedNode instanceof Patient){			     				
			     				Patient selectedPatient = Patient.class.cast(selectedNode);
			     				logger.info("Starting node query: " + selectedPatient.toString());
			     				//Retrieve studies for selected patient
			     				rightPanel.cbxAnnot.setEnabled(true);
			     				progressBar.setString("Processing search request ...");
			     				progressBar.setIndeterminate(true);
			     				progressBar.updateUI();				     				
			     				String[] characterSets = { "ISO_IR 100" };
			     				SpecificCharacterSet specificCharacterSet = new SpecificCharacterSet(characterSets);			  
			     				try {			     					
			     					{ AttributeTag t = TagFromName.PatientID; Attribute a = new ShortStringAttribute(t,specificCharacterSet); 
			     						a.addValue(selectedPatient.getPatientID());
			     						initialCriteria.put(t,a); }
								} catch (DicomException e1) {
									logger.error(e1, e1);
									notifyException(e1.getMessage());								} 											     						
			     				Boolean bln = criteriaPanel.verifyCriteria(initialCriteria);
			     				if(bln && selectedGridTypeDicomService != null){											
			     					GridUtil gridUtil = gridMgr.getGridUtil();
			     					CQLQuery cql = gridUtil.convertToCQLStatement(initialCriteria, CQLTargetName.STUDY);				
			     					{ AttributeTag t = TagFromName.PatientID; Attribute a = new ShortStringAttribute(t,specificCharacterSet); 
		     							try {
											a.addValue("");
										} catch (DicomException e1) {
											logger.error(e1, e1);
											notifyException(e1.getMessage());
										}
		     							initialCriteria.put(t,a);}
			     					GridQuery gridQuery = new GridQuery(cql, selectedGridTypeDicomService, result, selectedNode);
			     					gridQuery.addDataAccessListener(l);
			     					Thread t = new Thread(gridQuery); 					
			     					t.start();									
			     				}else{
			     					progressBar.setString("");
			     					progressBar.setIndeterminate(false);
			     				}			     				
			     				repaint();
			     			}else if(selectedNode instanceof Study){
			     				Study selectedStudy = Study.class.cast(selectedNode);
			     				logger.info("Starting node query: " + selectedStudy.toString());
			     				//Retrieve series for selected study
			     				rightPanel.cbxAnnot.setEnabled(true);
			     				progressBar.setString("Processing search request ...");
			     				progressBar.setIndeterminate(true);
			     				progressBar.updateUI();	
			     				String[] characterSets = { "ISO_IR 100" };
			     				SpecificCharacterSet specificCharacterSet = new SpecificCharacterSet(characterSets);
			     				try {			     								     					 			     									     						
			     					{ AttributeTag t = TagFromName.StudyInstanceUID; Attribute a = new ShortStringAttribute(t,specificCharacterSet); 
				     					a.addValue(selectedStudy.getStudyInstanceUID());									 
				     					initialCriteria.put(t,a); }
								} catch (DicomException e1) {
									logger.error(e1, e1);
									notifyException(e1.getMessage());
								} 
			     				//setCriteriaList(updatedCriteria);				
			     				Boolean bln = criteriaPanel.verifyCriteria(initialCriteria);
			     				if(bln && selectedGridTypeDicomService != null){											
			     					GridUtil gridUtil = gridMgr.getGridUtil();
			     					CQLQuery cql = gridUtil.convertToCQLStatement(initialCriteria, CQLTargetName.SERIES);	
			     					//CQLQuery cql = GridUtil.convertToCQLWithNCIAHelper(criteria);
			     					try {
			     						{ AttributeTag t = TagFromName.StudyInstanceUID; Attribute a = new ShortStringAttribute(t,specificCharacterSet); 		     							
											a.addValue("");										
											initialCriteria.put(t,a);}
										} catch (DicomException e1) {
											logger.error(e1, e1);
											notifyException(e1.getMessage());
										}		     						
			     					GridQuery gridQuery = new GridQuery(cql, selectedGridTypeDicomService, result, selectedNode);
			     					gridQuery.addDataAccessListener(l);
			     					Thread t = new Thread(gridQuery); 					
			     					t.start();									
			     				}else{
			     					progressBar.setString("");
			     					progressBar.setIndeterminate(false);
			     				}			     				
			     				repaint();
			     			} else if(selectedNode instanceof Series){
			     				Series selectedSeries = Series.class.cast(selectedNode);
			     				logger.info("Starting node query: " + selectedSeries.toString());
			     				//Retrieve items for selected series
			     				rightPanel.cbxAnnot.setEnabled(true);
			     				progressBar.setString("Processing search request ...");
			     				progressBar.setIndeterminate(true);
			     				progressBar.updateUI();
			     				String[] characterSets = { "ISO_IR 100" };
			     				SpecificCharacterSet specificCharacterSet = new SpecificCharacterSet(characterSets);
			     				try {			     								     					 			     									     						
			     					{ AttributeTag t = TagFromName.SeriesInstanceUID; Attribute a = new ShortStringAttribute(t,specificCharacterSet); 
				     					a.addValue(selectedSeries.getSeriesInstanceUID());									 
				     					initialCriteria.put(t,a); }
								} catch (DicomException e1) {
									logger.error(e1, e1);
									notifyException(e1.getMessage());
								}
								Boolean bln = criteriaPanel.verifyCriteria(initialCriteria);
			     				if(bln && selectedGridTypeDicomService != null){											
			     					GridUtil gridUtil = gridMgr.getGridUtil();
			     					CQLQuery cql = gridUtil.convertToCQLStatement(initialCriteria, CQLTargetName.SERIES);	
			     					try {
			     						{ AttributeTag t = TagFromName.SeriesInstanceUID; Attribute a = new ShortStringAttribute(t,specificCharacterSet); 		     							
											a.addValue("");										
											initialCriteria.put(t,a);}
									} catch (DicomException e1) {
											logger.error(e1, e1);
											notifyException(e1.getMessage());
									}		     						
			     					GridQuery gridQuery = new GridQuery(cql, selectedGridTypeDicomService, result, selectedNode);
			     					gridQuery.addDataAccessListener(l);
			     					Thread t = new Thread(gridQuery); 					
			     					t.start();									
			     				}else{
			     					progressBar.setString("");
			     					progressBar.setIndeterminate(false);
			     				}			     				
			     				repaint();
			     				
			     				
			     			}
			     		}
			     	}
		        } else {
		        	Timer timer = new Timer(300, nodeSelectionListener);
		        	timer.setRepeats(false);
		        	timer.start();
		        }
		    }
	};	
	
	
	//TODO check if allRetrieveFiles must be synchronized
	List<File> allRetrivedFiles;
	int numRetrieveThreadsStarted;
	int numRetrieveThreadsReturned;
	public void importedFilesAvailable(GridRetrieveEvent e) {
		if(e.getSource() instanceof GridRetrieve){
			GridRetrieve dicomRetrieve = (GridRetrieve)e.getSource();
			List<File> result = dicomRetrieve.getRetrievedFiles();
			allRetrivedFiles.addAll(result);
		} else if(e.getSource() instanceof GridRetrieveNCIA){
			GridRetrieveNCIA dicomRetrieve = (GridRetrieveNCIA)e.getSource();
			List<File> result = dicomRetrieve.getRetrievedFiles();
			allRetrivedFiles.addAll(result);
		} else if(e.getSource() instanceof AimRetrieve){
			AimRetrieve aimRetrieve = (AimRetrieve)e.getSource();
			List<File> result = aimRetrieve.getRetrievedFiles();
			if(result != null){
				allRetrivedFiles.addAll(result);
			}
		}
		numRetrieveThreadsReturned++;
		if(numRetrieveThreadsStarted == numRetrieveThreadsReturned){
			progressBar.setString("GridRetrieve finished");
			progressBar.setIndeterminate(false);			
			criteriaPanel.getQueryButton().setBackground(xipBtn);
			criteriaPanel.getQueryButton().setEnabled(true);
			File[] files = new File[allRetrivedFiles.size()];
			allRetrivedFiles.toArray(files);		
			FileManager fileMgr = FileManagerFactory.getInstance();						
	        fileMgr.run(files);
		}			
	}
	
	
	SearchResult selectedDataSearchResult;
	@Override
	public void dataSelectionChanged(DataSelectionEvent event) {
		selectedDataSearchResult = (SearchResult)event.getSource();
	}
	
	
	/**
	 * Method is used to update selectedDataSearchResult after sub-queries
	 * If PatientNode selected before sub-query, all of the studies belonging to this patient should not 
	 * only be selected but also added to the selectedDataSearchResult object.
	 * @throws InterruptedException 
	 */
	void updateSelectedDataSearchResult(DefaultMutableTreeNode node) {
		synchronized(result){
			try {
				while(subqueryCompleted == false){
					result.wait();
				}
				//PatientNode is not included, because Patient node are found always in first query and not not sub-queries
				if (node instanceof PatientNode){
					Patient patient = (Patient)node.getUserObject();
					if(selectedDataSearchResult != null){
						Patient selectedPatient = selectedDataSearchResult.getPatient(patient.getPatientID());
						if(selectedPatient != null){
							if(((PatientNode) node).isSelected()){
								Patient updatedPatient = result.getPatient(patient.getPatientID());
								List<Study> studies = updatedPatient.getStudies();
								for(Study study : studies){
									if(!selectedPatient.contains(study.getStudyInstanceUID())){
										selectedPatient.addStudy(study);
									}							
								}
							}
						}
					}
				} else if (node instanceof StudyNode){
					Study study = (Study)node.getUserObject();
					DefaultMutableTreeNode patientNode = (DefaultMutableTreeNode) node.getParent();
					Patient patient = (Patient)patientNode.getUserObject();
					if(selectedDataSearchResult != null){
						Patient selectedPatient = selectedDataSearchResult.getPatient(patient.getPatientID());
						if(selectedPatient != null){
							Study selectedStudy = selectedPatient.getStudy(study.getStudyInstanceUID());
							if(selectedStudy != null){
								if(((StudyNode) node).isSelected()){
									Study updatedStudy = result.getPatient(patient.getPatientID()).getStudy(study.getStudyInstanceUID());
									List<Series> series = updatedStudy.getSeries();
									for(Series oneSeries : series){
										if(!selectedStudy.contains(oneSeries.getSeriesInstanceUID())){
											selectedStudy.addSeries(oneSeries);
										}
									}
								} 
							}						
						}		
					}						 
				} 
			} catch (InterruptedException e) {
				logger.error(e, e);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Value of selectedDataSearchresult: ");
				if(selectedDataSearchResult != null){
					List<Patient> patients = selectedDataSearchResult.getPatients();					
					for (Patient logPatient : patients) {
						if(logPatient != null){
							logger.debug(logPatient.toString());
							List<Study> studies = logPatient.getStudies();
							for (Study logStudy : studies) {
								if(logStudy != null){
									logger.debug("   " + logStudy.toString());
									List<Series> series = logStudy.getSeries();
									for (Series logSeries : series) {
										if(logSeries != null){
											logger.debug("      " + logSeries.toString());
											List<Item> items = logSeries.getItems();
											for(Item logItem : items){
												if(logItem != null){
													logger.debug("         " + logItem.toString());
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
	
	@Override
	public void launchApplication(ApplicationEvent event) {
		logger.debug("Current data source tab: " + this.getClass().getName());
		// If nothing is selected, there is nothing to launch with
		//check if selectedDataSearchresult is not null and at least one PatientNode is selected
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)resultTree.getRootNode();
		boolean isDataSelected = DataSelectionValidator.isDataSelected(rootNode);
		if(isDataSelected){
			AppButton btn = (AppButton)event.getSource();
			ApplicationManager appMgr = ApplicationManagerFactory.getInstance(); 
			Application app = appMgr.getApplication(btn.getApplicationUUID());
			String appID = app.getID().toString();
			logger.debug("Application internal id: " + appID);
			String instanceName = app.getName();
			logger.debug("Application name: " + instanceName);
			File instanceExePath = app.getExePath();
			logger.debug("Exe path: " + instanceExePath);
			String instanceVendor = app.getVendor();
			logger.debug("Vendor: " + instanceVendor);
			String instanceVersion = app.getVersion();
			logger.debug("Version: " + instanceVersion);
			File instanceIconFile = app.getIconFile();
			String type = app.getType();
			logger.debug("Type: " + type);
			boolean requiresGUI = app.requiresGUI();
			logger.debug("Requires GUI: " + requiresGUI);
			String wg23DataModelType = app.getWG23DataModelType();
			logger.debug("WG23 data model type: " + wg23DataModelType);
			int concurrentInstances = app.getConcurrentInstances();
			logger.debug("Number of allowable concurrent instances: " + concurrentInstances);
			IterationTarget iterationTarget = app.getIterationTarget();
			logger.debug("IterationTarget: " + iterationTarget.toString());
			
			//Check if application to be launched is not running.
			//If yes, create new application instance
			State state = app.getState();
			// TODO Fill in with whatever is needed to make this work with Grid
			// TODO replace AVTQuery and AVTRetrieve2 with GridQuery and GridRetrieve
			Query query = new AVTQuery();
			File tmpDir = ApplicationManagerFactory.getInstance().getTmpDir();
			Retrieve retrieve = new AVTRetrieve2(tmpDir); 
			if(state != null && !state.equals(State.EXIT)){
				Application instanceApp = new Application(instanceName, instanceExePath, instanceVendor,
						instanceVersion, instanceIconFile, type, requiresGUI, wg23DataModelType, concurrentInstances, iterationTarget);
				instanceApp.setSelectedDataSearchResult(selectedDataSearchResult);
				instanceApp.setQueryDataSource(query);
				instanceApp.setRetrieveDataSource(retrieve);
				instanceApp.setDoSave(false);
				appMgr.addApplication(instanceApp);		
				instanceApp.launch(appMgr.generateNewHostServiceURL(), appMgr.generateNewApplicationServiceURL());
			}else{
				app.setSelectedDataSearchResult(selectedDataSearchResult);
				app.setQueryDataSource(query);
				app.setRetrieveDataSource(retrieve);
				app.setApplicationTmpDir(tmpDir);
				app.launch(appMgr.generateNewHostServiceURL(), appMgr.generateNewApplicationServiceURL());
			}	
	
			
			//TODO Remove all the following
			// Start background retrieving of data that could eventually be given to the application
			numRetrieveThreadsReturned = 0;
			List<CQLQuery> criteriasDicom = getDicomRetrieveCriteria();
			List<CQLQuery> criteriasAim = getAimRetrieveCriteria();	
			allRetrivedFiles = new ArrayList<File>();
			numRetrieveThreadsStarted = 0;
			//number of criteriaDicom and criteriaAim should be equal 
			int numOfCriterias = criteriasDicom.size();
			if( numOfCriterias > 0){
				progressBar.setString("Processing retrieve request ...");
				progressBar.setIndeterminate(true);
				progressBar.updateUI();	
				criteriaPanel.getQueryButton().setBackground(Color.GRAY);
				criteriaPanel.getQueryButton().setEnabled(false);
				rightPanel.cbxAnnot.setEnabled(true);																
				if(selectedGridTypeDicomService.getProtocolVersion().equalsIgnoreCase("DICOM")){					
					for(int i = 0; i < criteriasDicom.size(); i++){
						CQLQuery cqlQuery = criteriasDicom.get(i);
						try {
							//Retrieve Dicom						
							GridRetrieve gridRetrieve = new GridRetrieve(cqlQuery, selectedGridTypeDicomService, gridMgr.getImportDirectory());
							gridRetrieve.addDataAccessListener(this);						
							Thread t = new Thread(gridRetrieve);
							t.start();						
							numRetrieveThreadsStarted++;							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							//TODO
							//Set to mumber of threads to terminate retrieve
						}																				
					}
				}else if (selectedGridTypeDicomService.getProtocolVersion().equalsIgnoreCase("NBIA-4.2")){
					//Retrieve DICOM from NBIA
					Map<Series, Study> map = getSelectedSeries();
					Set<Series> seriesSet = map.keySet();
					Iterator<Series> iter = seriesSet.iterator();
					while (iter.hasNext()){
						Series series = iter.next();
						String selectedSeriesInstanceUID = series.getSeriesInstanceUID();			
						GridRetrieveNCIA nciaRetrieve = new GridRetrieveNCIA(selectedSeriesInstanceUID, selectedGridTypeDicomService, gridMgr.getImportDirectory());
						nciaRetrieve.addDataAccessListener(l);
						Thread t = new Thread(nciaRetrieve);
						t.start();
						numRetrieveThreadsStarted++;
					}
				}												
			}
			//Retrieve AIM				
			if(rightPanel.cbxAnnot.isSelected() && selectedGridTypeAimService != null){
				for(int i = 0; i < criteriasAim.size(); i++){
					CQLQuery aimCQL = criteriasAim.get(i);						
					try {
						AimRetrieve aimRetrieve = new AimRetrieve(aimCQL, selectedGridTypeAimService, gridMgr.getImportDirectory());
						aimRetrieve.addDataAccessListener(l);
						Thread t = new Thread(aimRetrieve);
						t.start();
						numRetrieveThreadsStarted++;
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}					
				}					
			}
		}
	}

	
	Map<Series, Study>  getSelectedSeries(){
		return null;
	}
	
	@Override
	public void retrieveResultsAvailable(RetrieveEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

