/**
 * Copyright (c) 2010 Washington University in St. Louis. All Rights Reserved.
 */
package edu.wustl.xipHost.application;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import org.apache.log4j.Logger;
import edu.wustl.xipHost.iterator.IterationTarget;
import edu.wustl.xipHost.gui.ExceptionDialog;
import edu.wustl.xipHost.gui.FileChooser;
import edu.wustl.xipHost.gui.HostMainWindow;


public class EditApplicationDialog extends JDialog implements ActionListener, FocusListener {
	final static Logger logger = Logger.getLogger(EditApplicationDialog.class);
	JLabel lblName = new JLabel("Name");
	JLabel lblPath = new JLabel("Path");
	JLabel lblVendor = new JLabel("Vendor");
	JLabel lblVersion = new JLabel("Version");
	JLabel lblIconFile = new JLabel("Icon");
	JLabel lblType = new JLabel("Type");
	JLabel lblRequiresGUI = new JLabel("Requires GUI");
	JLabel lblWG23DataModelType = new JLabel("WG23 data model Type");
	JLabel lblConcurrentInstances = new JLabel("Allowable concurrent Instances");
	JLabel lblIterationTarget = new JLabel("Iteration target");
	ImageIcon icon =  new ImageIcon("./gif/Open24.gif");
	JLabel lblImageExePath = new JLabel(icon);
	JLabel lblImageIconFile = new JLabel(icon);	
	JTextField txtName = new JTextField("", 20);
	JTextField txtPath = new JTextField("", 20);
	JTextField txtVendor = new JTextField("", 20);
	JTextField txtVersion = new JTextField("", 20);
	JTextField txtIconFile = new JTextField("", 20);
	DefaultComboBoxModel comboModelType = new DefaultComboBoxModel();
	JComboBox listType = new JComboBox(comboModelType);
	DefaultComboBoxModel comboModelRequiresGUI = new DefaultComboBoxModel();
	JComboBox listRequiresGUI = new JComboBox(comboModelRequiresGUI);
	DefaultComboBoxModel comboModelDataModelType = new DefaultComboBoxModel();
	JComboBox listWG23DataModelType = new JComboBox(comboModelDataModelType);
	JTextField txtConcurrentInstances = new JTextField("", 20);
	DefaultComboBoxModel comboModelIterationTarget = new DefaultComboBoxModel();
	JComboBox listIterationTarget = new JComboBox(comboModelIterationTarget);
	JButton btnOK = new JButton("OK");
	JPanel panel = new JPanel();
	Application app;
	FileChooser fileChooser = new FileChooser(false);	
	Color xipBtn = new Color(56, 73, 150);
	Font font = new Font("Tahoma", 0, 11);	
	Border borderRed = BorderFactory.createLineBorder(Color.RED);
	
	/**
	 * @param owner
	 */
	public EditApplicationDialog(Frame owner, Application app){		
		super(owner, "Edit application", true);
		this.app = app;
		txtName.addKeyListener(customKeyListener);
		txtPath.addKeyListener(customKeyListener);
		txtIconFile.addKeyListener(customKeyListener);
		comboModelType.addElement("Rendering");
		comboModelType.addElement("Analytical");
		listType.setMaximumRowCount(10);
		listType.setPreferredSize(new Dimension((int) txtName.getPreferredSize().getWidth(), (int) txtName.getPreferredSize().getHeight()));
		listType.setFont(font);
		listType.setEditable(false);		
		listType.addActionListener(this);
		comboModelRequiresGUI.addElement(true);
		comboModelRequiresGUI.addElement(false);
		listRequiresGUI.setMaximumRowCount(2);
		listRequiresGUI.setPreferredSize(new Dimension((int) txtName.getPreferredSize().getWidth(), (int) txtName.getPreferredSize().getHeight()));
		listRequiresGUI.setFont(font);
		listRequiresGUI.setEditable(false);		
		listRequiresGUI.addActionListener(this);
		comboModelDataModelType.addElement("Files");
		comboModelDataModelType.addElement("Native models");
		comboModelDataModelType.addElement("Abstract");
		listWG23DataModelType.setMaximumRowCount(3);
		listWG23DataModelType.setPreferredSize(new Dimension((int) txtName.getPreferredSize().getWidth(), (int) txtName.getPreferredSize().getHeight()));
		listWG23DataModelType.setFont(font);
		listWG23DataModelType.setEditable(false);		
		listWG23DataModelType.addActionListener(this);
		comboModelIterationTarget.addElement(IterationTarget.PATIENT);
		comboModelIterationTarget.addElement(IterationTarget.STUDY);
		comboModelIterationTarget.addElement(IterationTarget.SERIES);
		listIterationTarget.setMaximumRowCount(3);
		listIterationTarget.setPreferredSize(new Dimension((int) txtName.getPreferredSize().getWidth(), (int) txtName.getPreferredSize().getHeight()));
		listIterationTarget.setFont(font);
		listIterationTarget.setEditable(false);		
		listIterationTarget.addActionListener(this);
		panel.add(lblName);
		panel.add(txtName);
		panel.add(lblPath);
		panel.add(txtPath);
		panel.add(lblImageExePath);
		panel.add(lblVendor);
		panel.add(txtVendor);
		panel.add(lblVersion);
		panel.add(txtVersion);
		panel.add(lblIconFile);
		panel.add(txtIconFile);
		panel.add(lblImageIconFile);
		panel.add(lblType);
		panel.add(listType);
		panel.add(lblRequiresGUI);
		panel.add(listRequiresGUI);
		panel.add(lblWG23DataModelType);
		panel.add(listWG23DataModelType);
		panel.add(lblConcurrentInstances);
		txtConcurrentInstances.addFocusListener(this);
		panel.add(txtConcurrentInstances);
		panel.add(lblIterationTarget);
		panel.add(listIterationTarget);
		panel.add(btnOK);
		lblName.setForeground(Color.WHITE);
		lblPath.setForeground(Color.WHITE);		
		lblVendor.setForeground(Color.WHITE);
		lblVersion.setForeground(Color.WHITE);
		lblIconFile.setForeground(Color.WHITE);	
		lblType.setForeground(Color.WHITE);
		lblRequiresGUI.setForeground(Color.WHITE);
		lblWG23DataModelType.setForeground(Color.WHITE);
		lblConcurrentInstances.setForeground(Color.WHITE);
		lblIterationTarget.setForeground(Color.WHITE);
		panel.setBackground(xipBtn);
		btnOK.setPreferredSize(new Dimension(100, 25));		
		btnOK.addActionListener(this);
		lblImageExePath.setToolTipText("Select path");		
		lblImageExePath.addMouseListener(			
				new MouseAdapter(){
					public void mouseClicked(MouseEvent e){						
						fileChooser.displayFileChooser();						
						String path = null;					
						try {
							path = fileChooser.getSelectedFiles()[0].getCanonicalPath();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}						
						/* It should be just one file for single selection
						 * Provide exception handling
						 * */
						txtPath.setText(path);
						txtPath.setCaretPosition(0);
					}
				}
		);
		lblImageIconFile.setToolTipText("Select path");
		lblImageIconFile.addMouseListener(
				new MouseAdapter(){
					public void mouseClicked(MouseEvent e){						
						fileChooser.displayFileChooser();						
						String path = null;					
						try {
							path = fileChooser.getSelectedFiles()[0].getCanonicalPath();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}	
						txtIconFile.setText(path);
						txtIconFile.setCaretPosition(0);
					}
				}
		);
		buildLayout();
		add(panel);
		setResizable(false);								
		setPreferredSize(new Dimension(700, 500));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = this.getPreferredSize();
        setBounds((screenSize.width - windowSize.width) / 2, (screenSize.height - windowSize.height) /2,  windowSize.width, windowSize.height);
		pack();
		if(app == null){
			listType.setSelectedIndex(0);
			listRequiresGUI.setSelectedIndex(0);
			listWG23DataModelType.setSelectedIndex(0);
			listIterationTarget.setSelectedIndex(1);
		} else {
			setName(app.getName());
			setExePath(app.getExePath());
			setVendor(app.getVendor());
			setVersion(app.getVersion());
			setIconPath(app.getIconPath());
			setType(app.getType());
			setRequiresGUI(app.requiresGUI());
			setWG23DataModelType(app.getWG23DataModelType());
			setConcurrentInstances(app.getConcurrentInstances());
			setIterationTarget(app.getIterationTarget());
		}
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	JFrame frame = new JFrame();		
		new EditApplicationDialog(frame, null);						
	}

	String type;
	Boolean requiresGUI;
	String wg23DataModelType;
	IterationTarget iterationTarget;
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == listType){
			Object itemType = ((JComboBox)e.getSource()).getSelectedItem();
			type = (String)itemType;
		}else if (e.getSource() == listRequiresGUI){
			Object itemRequiresGUI = ((JComboBox)e.getSource()).getSelectedItem();
			requiresGUI = (Boolean)itemRequiresGUI;
		}else if(e.getSource() == listWG23DataModelType){
			Object itemWG23DataModelType = ((JComboBox)e.getSource()).getSelectedItem();
			wg23DataModelType = (String)itemWG23DataModelType;
		}else if(e.getSource() == listIterationTarget){
			Object itemIterationTarget = ((JComboBox)e.getSource()).getSelectedItem();
			iterationTarget = (IterationTarget)itemIterationTarget;
		}else if(e.getSource() == btnOK){						
			if(type == null){
				type = (String)comboModelType.getElementAt(0);
			}
			if(requiresGUI == null){
				requiresGUI = (Boolean)comboModelRequiresGUI.getElementAt(0);
			}
			if(wg23DataModelType == null){
				wg23DataModelType = (String)comboModelDataModelType.getElementAt(0);
			}
			if(iterationTarget == null){
				iterationTarget = (IterationTarget) comboModelIterationTarget.getElementAt(1);
			}
			if(txtConcurrentInstances.getText().isEmpty()){
				txtConcurrentInstances.setText("Eneter number of instances");
				txtConcurrentInstances.setForeground(Color.RED);
			}
			try{
				if(logger.isDebugEnabled()){
					logger.debug("New application parameters:");
					logger.debug("              Application name: " + txtName.getText());
					logger.debug("          Application exe path: " + txtPath.getText());
					logger.debug("                        Vendor: " + txtVendor.getText());
					logger.debug("                       Version: " + txtVersion.getText());
					logger.debug("                     Icon file: " + txtIconFile.getText());
					logger.debug("              Application type: " + type);
					logger.debug("                  Requires GUI: " + requiresGUI);
					logger.debug("          WG23 data model type: " + wg23DataModelType);
					logger.debug("Allowable concurrent instances: " + txtConcurrentInstances.getText());
					logger.debug("              Iteration target: " + iterationTarget.toString());
				}
				ApplicationManager appMgr = ApplicationManagerFactory.getInstance();
				if(app == null){
					app = new Application(txtName.getText(), txtPath.getText(), txtVendor.getText(), txtVersion.getText(), txtIconFile.getText(), 
							type, requiresGUI, wg23DataModelType, 
							Integer.valueOf(txtConcurrentInstances.getText()), iterationTarget);
					app.setDoSave(true);
					if(app.isValid()){
						appMgr.addApplication(app);
						HostMainWindow.getHostIconBar().getApplicationBar().addApplicationIcon(app);
				    	HostMainWindow.getHostIconBar().getApplicationBar().updateUI();
						//addApplicationTerminationListener
					} else {
						appMgr.addNotValidApplication(app);
					}
				} else {
					boolean appWasValid = app.isValid();
					app.setName(txtName.getText());
					app.setExePath(txtPath.getText());
					app.setVendor(txtVendor.getText());
					app.setVersion(txtVersion.getText());
					app.setIconPath(txtIconFile.getText());
					app.setType(type);
					app.setRequiresGUI(requiresGUI);
					app.setWG23DataModelType(wg23DataModelType);
					app.setConcurrentInstances(Integer.valueOf(txtConcurrentInstances.getText()));
					app.setIterationTarget(iterationTarget);
					if(appWasValid == false && app.isValid()){
						appMgr.getNotValidApplications().remove(app);
						appMgr.addApplication(app);
						HostMainWindow.getHostIconBar().getApplicationBar().addApplicationIcon(app);
				    	HostMainWindow.getHostIconBar().getApplicationBar().updateUI();
					} else if(appWasValid == true && app.isValid() == false) {
						appMgr.removeApplication(app.getID());
						appMgr.addNotValidApplication(app);
						HostMainWindow.getHostIconBar().getApplicationBar().removeApplicationIcon(app);
						HostMainWindow.getHostIconBar().getApplicationBar().updateUI();
					} else if (appWasValid == true && app.isValid()){
						//Update
						appMgr.modifyApplication(app.getID(), app);
						HostMainWindow.getHostIconBar().getApplicationBar().updateApplicationTextAndIcon(app);
						HostMainWindow.getHostIconBar().getApplicationBar().updateUI();
					}
				}
			}catch (IllegalArgumentException e1){
				new ExceptionDialog("Cannot create new application.", 
						"Ensure applications parameters are valid.",
						"Add Application Dialog");
				return;
			}
			dispose();
		}else{
			app = null;
		}
	}
	
	public Application getApplication(){
		return app;
	}

	public void buildLayout(){
		GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        panel.setLayout(layout);        

        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 0;        
        constraints.insets.top = 30;
        constraints.insets.left = 20;
        constraints.insets.right = 15;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(lblName, constraints);       
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(lblPath, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(lblVendor, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(lblVersion, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(lblIconFile, constraints); 
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(lblType, constraints); 
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(lblRequiresGUI, constraints); 
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 7;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(lblWG23DataModelType, constraints); 
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 8;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(lblConcurrentInstances, constraints); 
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 0;
        constraints.gridy = 9;
        constraints.insets.top = 10;
        constraints.insets.bottom = 20;        
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(lblIterationTarget, constraints); 
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 0;        
        constraints.insets.top = 30;
        constraints.insets.left = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(txtName, constraints);       
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(txtPath, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(txtVendor, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(txtVersion, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 4;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(txtIconFile, constraints);  
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(listType, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 6;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(listRequiresGUI, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 7;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(listWG23DataModelType, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 8;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(txtConcurrentInstances, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 9;
        constraints.insets.top = 10;
        constraints.insets.bottom = 15;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(listIterationTarget, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 1;
        constraints.gridy = 10;      
        constraints.insets.top = 10;
        constraints.insets.bottom = 20;        
        constraints.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(btnOK, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0; 
        constraints.insets.left = 10; 
        constraints.insets.right = 20;
        constraints.anchor = GridBagConstraints.WEST;
        layout.setConstraints(lblImageExePath, constraints);
        
        constraints.fill = GridBagConstraints.NONE;        
        constraints.gridx = 2;
        constraints.gridy = 4;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0; 
        constraints.insets.left = 10; 
        constraints.insets.right = 20;
        constraints.anchor = GridBagConstraints.WEST;
        layout.setConstraints(lblImageIconFile, constraints);
	}

	@Override
	public void focusGained(FocusEvent e) {
		txtConcurrentInstances.setForeground(Color.BLACK);
	}

	@Override
	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void setName(String name) {
		txtName.setText(name);
		if(name == null || name.isEmpty() || name.trim().length() == 0) {
			txtName.setForeground(Color.RED);
			txtName.setBorder(borderRed);
			txtName.setPreferredSize(new Dimension((int)txtName.getPreferredSize().getWidth(), (int)txtName.getPreferredSize().getHeight() + 2));
		}
	}
	
	public void setExePath(String exePath){
		txtPath.setText(exePath);
		if(exePath == null) {
			txtPath.setForeground(Color.RED);
			txtPath.setBorder(borderRed);
			txtPath.setPreferredSize(new Dimension((int)txtName.getPreferredSize().getWidth(), (int)txtName.getPreferredSize().getHeight() + 2));
			return;
		}
		File exeFile = new File(exePath);
		if(!exeFile.exists()){
			txtPath.setForeground(Color.RED);
			txtPath.setBorder(borderRed);
			txtPath.setPreferredSize(new Dimension((int)txtName.getPreferredSize().getWidth(), (int)txtName.getPreferredSize().getHeight() + 2));
		}
	}
	
	public void setVendor(String vendor){
		txtVendor.setText(vendor);
	}
	
	public void setVersion(String version){
		txtVersion.setText(version);	
	}
	
	public void setIconPath(String iconFilePath){
		txtIconFile.setText(iconFilePath);
		if(iconFilePath == null){
			txtIconFile.setForeground(Color.RED);
			txtIconFile.setBorder(borderRed);
			txtIconFile.setPreferredSize(new Dimension((int)txtName.getPreferredSize().getWidth(), (int)txtName.getPreferredSize().getHeight() + 2));
			return;
		}
		File file = new File(iconFilePath);
		if(!iconFilePath.trim().isEmpty() && !file.exists()){
			txtIconFile.setForeground(Color.RED);
			txtIconFile.setBorder(borderRed);
			txtIconFile.setPreferredSize(new Dimension((int)txtName.getPreferredSize().getWidth(), (int)txtName.getPreferredSize().getHeight() + 2));
		}
	}
	
	public void setType(String type) {
		this.type = type;
		comboModelType.setSelectedItem(type);
		if(!type.equalsIgnoreCase("Rendering") && !type.equalsIgnoreCase("Analytical")){
			listType.setForeground(Color.RED);
		}
	}
	
	public void setRequiresGUI(boolean requiresGUI) {
		this.requiresGUI = requiresGUI;
		comboModelRequiresGUI.setSelectedItem(requiresGUI);
	}
	
	public void setWG23DataModelType(String wg23DataModelType) {
		this.wg23DataModelType = wg23DataModelType;
		comboModelDataModelType.setSelectedItem(wg23DataModelType);
		if(!wg23DataModelType.equalsIgnoreCase("Files") && !wg23DataModelType.equalsIgnoreCase("Native models") &&
				!wg23DataModelType.equalsIgnoreCase("Abstract")){
			listWG23DataModelType.setForeground(Color.RED);
		}
	}
	
	public void setConcurrentInstances(int concurrentInstances) {
		txtConcurrentInstances.setText(String.valueOf(concurrentInstances));
		if( concurrentInstances <= 0) {
			txtConcurrentInstances.setForeground(Color.RED);
			txtConcurrentInstances.setBorder(borderRed);
			txtConcurrentInstances.setPreferredSize(new Dimension((int)txtName.getPreferredSize().getWidth(), (int)txtName.getPreferredSize().getHeight() + 2));
		}
	}
	
	public void setIterationTarget(IterationTarget iterationTarget) {
		this.iterationTarget = iterationTarget;
		comboModelIterationTarget.setSelectedItem(iterationTarget);
		if(!iterationTarget.equals(IterationTarget.PATIENT) && !iterationTarget.equals(IterationTarget.STUDY) &&
				!iterationTarget.equals(IterationTarget.SERIES)){
			listIterationTarget.setForeground(Color.RED);
		}
	}

	Border borderEmpty = BorderFactory.createEmptyBorder();
	int count = 0;
	KeyListener customKeyListener = new KeyAdapter(){
		public void keyTyped(KeyEvent e){
			JTextField txtField = (JTextField)(e.getSource());
			txtField.setForeground(Color.BLACK);
			txtField.setBorder(borderEmpty);
			txtField.setPreferredSize(new Dimension((int)txtField.getPreferredSize().getWidth(), (int)txtField.getPreferredSize().getHeight() - 2));
			txtField.revalidate();
			txtField.repaint();
		}
	};
	
}
