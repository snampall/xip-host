
package org.nema.dicom.PS3_19;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "ApplicationService-20100825", targetNamespace = "http://dicom.nema.org/PS3.19/ApplicationService-20100825")
public class ApplicationService20100825
    extends Service
{

    private final static URL APPLICATIONSERVICE20100825_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(org.nema.dicom.PS3_19.ApplicationService20100825 .class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = org.nema.dicom.PS3_19.ApplicationService20100825 .class.getResource(".");
            url = new URL(baseUrl, "file:/C:/Users/ltarbo01/Documents/DICOM/WG23/wsdl_ft/ApplicationService-20100825.wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'file:/C:/Users/ltarbo01/Documents/DICOM/WG23/wsdl_ft/ApplicationService-20100825.wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        APPLICATIONSERVICE20100825_WSDL_LOCATION = url;
    }

    public ApplicationService20100825(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ApplicationService20100825() {
        super(APPLICATIONSERVICE20100825_WSDL_LOCATION, new QName("http://dicom.nema.org/PS3.19/ApplicationService-20100825", "ApplicationService-20100825"));
    }

    /**
     * 
     * @return
     *     returns IApplicationService20100825
     */
    @WebEndpoint(name = "ApplicationServiceBinding")
    public IApplicationService20100825 getApplicationServiceBinding() {
        return super.getPort(new QName("http://dicom.nema.org/PS3.19/ApplicationService-20100825", "ApplicationServiceBinding"), IApplicationService20100825.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IApplicationService20100825
     */
    @WebEndpoint(name = "ApplicationServiceBinding")
    public IApplicationService20100825 getApplicationServiceBinding(WebServiceFeature... features) {
        return super.getPort(new QName("http://dicom.nema.org/PS3.19/ApplicationService-20100825", "ApplicationServiceBinding"), IApplicationService20100825.class, features);
    }

}