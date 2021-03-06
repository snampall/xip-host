
package org.nema.dicom.wg23;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for availableData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="availableData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="patients" type="{http://wg23.dicom.nema.org/}ArrayOfPatient"/>
 *         &lt;element name="objectDescriptors" type="{http://wg23.dicom.nema.org/}ArrayOfObjectDescriptor"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "availableData", propOrder = {
    "patients",
    "objectDescriptors"
})
public class AvailableData {

    @XmlElement(required = true)
    protected ArrayOfPatient patients;
    @XmlElement(required = true)
    protected ArrayOfObjectDescriptor objectDescriptors;

    /**
     * Gets the value of the patients property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfPatient }
     *     
     */
    public ArrayOfPatient getPatients() {
        return patients;
    }

    /**
     * Sets the value of the patients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfPatient }
     *     
     */
    public void setPatients(ArrayOfPatient value) {
        this.patients = value;
    }

    /**
     * Gets the value of the objectDescriptors property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfObjectDescriptor }
     *     
     */
    public ArrayOfObjectDescriptor getObjectDescriptors() {
        return objectDescriptors;
    }

    /**
     * Sets the value of the objectDescriptors property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfObjectDescriptor }
     *     
     */
    public void setObjectDescriptors(ArrayOfObjectDescriptor value) {
        this.objectDescriptors = value;
    }

}
