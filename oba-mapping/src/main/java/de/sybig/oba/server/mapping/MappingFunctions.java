/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server.mapping;

import de.sybig.oba.server.ObaVirtualOntology;
import de.sybig.oba.server.OntologyFunction;
import de.sybig.oba.server.OntologyFunctions;
import de.sybig.oba.server.OntologyHandler;
import de.sybig.oba.server.OntologyResource;
import de.sybig.oba.server.mapping.SemanticSearchAlgorithm.Candidate;
import java.util.List;
import javax.ws.rs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zaynab
 */
public class MappingFunctions extends OntologyFunctions implements
        OntologyFunction {

    private static Logger log = LoggerFactory.getLogger(MappingFunctions.class);
    private static OntologyResource onto;
    private static ObaVirtualOntology ovo;

    public MappingFunctions() {
        super();
        onto = OntologyHandler.getInstance().getOntology("alignment");
        ovo = (ObaVirtualOntology) onto.getOntology();
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
        StringBuilder out = new StringBuilder();

        out.append("<h1>Available functions</h1>\n");
        out.append("<dt>/GetMappings</dt><dt>Displays the mappings of the alignment virtual ontology</dt>");
        out.append("<dl>");
        return out.toString();
    }

    @GET
    @Path("GetMappings/")
    @Produces("text/html,text/plain")
    public String GetMappings() throws Exception {
        List<Candidate> mappings = ovo.getMappings();
        System.out.println("START PRINTING !!!");
        String out = "<dt><h3>All Mapped Classes : " + mappings.size() + "</h3></dt>";
        out += "<table align=center border=1><tr><th>BeetleID</th><th>Beetle Label</th><th>FlyID</th><th>Fly Label</th><th>Score</th></tr>";
        for (int i = 0; i < mappings.size(); i++) {
            String[] data = mappings.get(i).toString().split(",");
            out += "<tr>";
            for (String s : data) {
                out += "<td>" + s + "</td>";
            }
            out += "</tr>";

        }
        out += "</table>";
        return out;
    }
}
