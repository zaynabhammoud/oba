/*
 * Created on June 6, 2016
 *
 */
package de.sybig.oba.server.flyBase;

import de.sybig.oba.server.OntologyFunction;
import de.sybig.oba.server.OntologyFunctions;
import java.io.IOException;
import java.util.Properties;
import javax.ws.rs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlyBaseFunctions extends OntologyFunctions implements
        OntologyFunction {

    private Logger log = LoggerFactory.getLogger(FlyBaseFunctions.class);
    private Properties flyBase;

    /**
     * A class providing functions for aligning two ontologies.
     *
     */
    public FlyBaseFunctions() {
        super();
                flyBase = new Properties();
        try {
            flyBase.load(getClass().getResourceAsStream(
                    "/flyBase.properties"));
        } catch (IOException e) {
            log.error("could not load properties for flyBase function class");
            e.printStackTrace();
        }
    }

    @Override
    public String getVersion() {
        return "1.3";
    }

    /**
     * Gets a short documentation of the implemented functions in html.
     */
    @GET
    @Path("/")
    @Produces("text/html")
    @Override
    public String getRoot() {
        StringBuffer out = new StringBuffer();

        return out.toString();
    }
}
