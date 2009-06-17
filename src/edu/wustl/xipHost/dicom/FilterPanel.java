/* Copyright (c) 2001-2005, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package edu.wustl.xipHost.dicom;

import java.awt.*; 
import java.awt.event.*; 
import java.util.*;  
import javax.swing.*; 
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import com.pixelmed.dicom.*;

/**
 * <font  face="Tahoma" size="2">
 * The {@link com.pixelmed.query.FilterPanel FilterPanel} class provides a graphical user
 * interface for editing the string values of DICOM attributes in list.
 *
 * <p>Though it is actually fairly general in its functionality, it is present in the
 * {@link com.pixelmed.query com.pixelmed.query} package in order to allow applications
 * to present to the user a list of matching and return keys intended for a query
 * (i.e. a C-FIND request identifier), and edit them to insert matching values.</p>
 *
 * <p>The list is ordered by the information entity of the attributes (patient,
 * study, series, instance), then sorted alphabetically by name within that
 * grouping.</p>
 *
 * <p>The class is not aware of any underlying matching key type semantics and allows
 * the user to enter the string literally, so to enter a wild card the user must know
 * and use the appropriate special characters. For example, entering "*Smith* for the
 * PatientName attribute value should match all patients whose name includes Smith.</p>
 *
 * <p>Note also that it is not necessary for the user to type tab or return after editing each field in
 * order to have the attribute value updated in the list that is returned, every key stroke is reflected
 * immediately in the corresponding attribute.</p>
 *
 * <p>Specific Character Set is specifically excluded from the displayed list, since it is not a
 *  matching key but rather describes the encoding of the list.</p>
 *
 * <p>UID attributes were formerly filtered out from the list, but it turns out to be useful
 * to be able to match on these, for instance on SOP Classes in Study.</p>
 *
 * @author	dclunie
 * </font>
 */
public class FilterPanel extends JPanel implements FocusListener {
	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/query/FilterPanel.java,v 1.9 2007/01/17 23:13:10 dclunie Exp $";
	
	/***/
	private GridBagLayout layout;
	/***/
	private AttributeList filterList;
	
	/***/
	final private class TextFieldForAttribute extends JTextField {
		/***/
		final private Attribute attribute;
		/***/
		final private void replaceAttributeValue(String text) {
			try {
				attribute.removeValues();
				if (text != null && text.length() > 0) {
					attribute.addValue(text);
				}
			}
			catch (DicomException e) {
			}
		}
		
		/**
		 * @param	a
		 */
		public TextFieldForAttribute(Attribute a) {
			super(20);
			attribute = a;
			setText(a.getSingleStringValueOrEmptyString());

			getDocument().addDocumentListener(new DocumentListener() {
				private String getText(DocumentEvent event) {
					String text = null;
					if (event != null) {
						Document document = event.getDocument();
						if (document != null) {
							try {
								text = document.getText(0,document.getLength());
							}
							catch (BadLocationException e) {
								e.printStackTrace(System.err);
							}
						}
					}
					return text;
				}
				public void changedUpdate(DocumentEvent e) {
					replaceAttributeValue(getText(e));
				}
				public void insertUpdate(DocumentEvent e) {
					replaceAttributeValue(getText(e));
				}
				public void removeUpdate(DocumentEvent e) {
					replaceAttributeValue(getText(e));
				}
			});
		}
	}
	
	/***/
	private void addInformationEntityLabelToPanel(int row,InformationEntity ie,GridBagConstraints constraints) {
		// we use a dummy icon just to force better (consistent) vertical spacing;
		// would be nice one day to use a real icon
		/*JLabel label = new JLabel(
			ie.toString(),
			new ImageIcon(new BufferedImage(1, 50, BufferedImage.TYPE_BYTE_GRAY)),
			SwingConstants.LEFT);*/
		JLabel label = new JLabel(ie.toString() + "____________________");
		label.setFont(font_1);
		constraints.gridy = row;
		constraints.gridx = 1;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.insets.left = 10;
        constraints.insets.right = 10;
        constraints.insets.top = 2;
        constraints.insets.bottom = 2;
		layout.setConstraints(label, constraints);
		add(label);
	}
	
	Font font_1 = new Font("Tahoma", 0, 12); 
	private void addAttributeToPanel(int row, String name, GridBagConstraints constraints) {
		DicomDictionary dictionary = filterList.getDictionary();
		AttributeTag t = dictionary.getTagFromName(name);
		Attribute a = filterList.get(t);
		String value = a.getSingleStringValueOrEmptyString();
		JLabel label = new JLabel(name);
		JTextField text = new TextFieldForAttribute(a);
		text.addFocusListener(this);
//		label.setFont(font_1);
//		label.setForeground(Color.WHITE);
//		text.setFont(font_1);
		
		
		constraints.gridy = row;
		constraints.gridx = 1;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets.left = 10;
        constraints.insets.right = 10;
        constraints.insets.top = 2;
        constraints.insets.bottom = 2;
		layout.setConstraints(label, constraints);
		add(label);
		constraints.gridx = 2;
		layout.setConstraints(text, constraints);
		add(text);
	}
	
	private static InformationEntity iterateByInformationEntity[] = {
			InformationEntity.PATIENT,
			InformationEntity.STUDY,
			InformationEntity.PROCEDURESTEP,
			InformationEntity.SERIES,
			InformationEntity.CONCATENATION,
			InformationEntity.INSTANCE,
			InformationEntity.FRAME
	};
		
	/***/
	private void addAttributesToPanel() {
//System.err.println("FilterPanel.addAttributesToPanel()");	
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		DicomDictionary dictionary = filterList.getDictionary();
		
		int rowCountInPanel=0;
		for (int ie = 0; ie < iterateByInformationEntity.length; ++ie) {
			InformationEntity whichIE = iterateByInformationEntity[ie];			
			TreeSet names = new TreeSet();			// sorted within TreeSet by name
			Iterator i = filterList.values().iterator();
			while (i.hasNext()) {
				Attribute a  = (Attribute)i.next();				
				if (a != null && (a instanceof StringAttribute || a instanceof TextAttribute)/* && !(a instanceof UniqueIdentifierAttribute)*/) {					
					AttributeTag t = a.getTag();
					
					if (!t.equals(TagFromName.SpecificCharacterSet) && dictionary.getInformationEntityFromTag(t) == whichIE) {
						//System.out.println(dictionary.getNameFromTag(t));
						names.add(dictionary.getNameFromTag(t));						
					}
				}
			}
			// now add the sorted names for this IE to the panel ...
			i = names.iterator();
			if (i.hasNext()) {
				addInformationEntityLabelToPanel(rowCountInPanel++, whichIE, constraints);
			}
			while (i.hasNext()) {
				addAttributeToPanel(rowCountInPanel++, (String)i.next(), constraints);
			}
		}
	}
	
	/**
	 * <p>Display a list of DICOM attributes and their editable string values.</p>
	 *
	 * @param	list	the list of DICOM attributes to be displayed and values edited
	 */
	public FilterPanel(AttributeList list) {
		super();
		layout = new GridBagLayout();
		setLayout(layout);
		filterList = list;
		addAttributesToPanel();
	}
	
	/**
	 * <p>Get the list of attributes and their current values.</p>
	 *
	 * @return	the list of attributes and their current values
	 */
	public AttributeList getFilterList() { return filterList;}

	public void focusGained(FocusEvent e) {			
		
	}

	public void focusLost(FocusEvent e) {				
		((TextFieldForAttribute)e.getSource()).replaceAttributeValue(((JTextField)e.getSource()).getText());
		//System.out.println(((JTextField)e.getSource()).getText());		
	}
}
