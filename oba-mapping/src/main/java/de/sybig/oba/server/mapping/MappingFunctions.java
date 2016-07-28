/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server.mapping;

import de.sybig.oba.server.mapping.SemanticSearchAlgorithm.Scores;
import cc.mallet.types.StringKernel;
import de.sybig.oba.server.ObaAnnotation;
import de.sybig.oba.server.ObaVirtualOntology;
import de.sybig.oba.server.OntologyFunction;
import de.sybig.oba.server.OntologyFunctions;
import de.sybig.oba.server.OntologyHandler;
import de.sybig.oba.server.OntologyHelper;
import de.sybig.oba.server.OntologyResource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import javax.ws.rs.*;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zaynab
 */
public class MappingFunctions extends OntologyFunctions implements
        OntologyFunction {

    private static Logger log = LoggerFactory.getLogger(MappingFunctions.class);

    public MappingFunctions() {
        super();
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
        out.append("<dt>writeCSV</dt>");
        out.append("<dl>");
        return out.toString();
    }

    @GET
    @Path("writeCSV/")
    @Produces("text/html, text/plain, application/json")
    public String writeCSV() throws IOException {
        log.info("Writing Started");
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        File file = new File("C:/Users/mashi/Desktop/KernelFBbtBeetle--" + timeStamp + ".csv");
        if (!file.exists()) {
            file.createNewFile();
        }
        log.info("file {} created", file.getPath());
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        ArrayList<String> data = new ArrayList<String>();
        File mapping = new File("C:/Users/mashi/Desktop/Mapping Tools/NEW LogMap/FlyBeetle.csv");
        if (mapping == null) {
            log.info("file not found");
            return "NOT FOUND";
        }

        BufferedReader br = null;
        String line = "";
        br = new BufferedReader(new FileReader(mapping));
        while ((line = br.readLine()) != null) {
            data.add(line);
        }
        OntologyResource ont1 = OntologyHandler.getInstance().getOntology("flyBase");
        OntologyResource ont2 = OntologyHandler.getInstance().getOntology("tribolium");
        if (ont1 == null) {
            log.info("FlyBase not loaded");
            return "FlyBase Null";
        }
        writer.append("Fly Class ID,Fly Class Label,Beetle Class ID,Beetle Class Label,Score\n");
        for (String s : data) {
            String[] sp = s.split(",");
//            log.info("-----"+sp[1]);
            String[] fbbtID = sp[0].split("FBbt");
//            log.info(fbbtID[1]);
//            log.info("-----"+sp[0]);
            String[] trOnID = sp[1].split("TrOn");
//            log.info(trOnID[1]);

            OWLClass cls1 = ont1.getCls("FBbt" + fbbtID[1], "http://purl.org/obo/owlapi/fly_anatomy.ontology#");
            OWLClass cls2 = ont2.getCls("TrOn" + trOnID[1], "http://purl.org/obo/owlapi/tribolium.anatomy#");
            if (cls2 == null) {
                log.info("Tribolium Class Null");
                return "TrOn Class Null";
            }
            if (cls1 == null) {
                log.info("FlyBase Class Null");
                return "FBbt Class Null";
            }
            String name1 = processString(getLabel(cls1, ont1));
            String name2 = processString(getLabel(cls2, ont2));
            String score = StringKernelCal(name1, name2);
            writer.append("FBbt" + fbbtID[1] + "," + name1 + "," + "TrOn" + trOnID[1] + "," + name2 + "," + score + "\n");
            writer.flush();
        }
        writer.close();
        return file.getPath();

    }

    protected String getLabel(OWLClass cls, OntologyResource onto) {
        OWLOntology ont = onto.getOntology().getOntology();
        if (ont == null) {
            log.info("ONTOLOGY Null");
            return "";
        }
        Set<ObaAnnotation> annotations = OntologyHelper
                .getAnnotationProperties(cls, ont);
        for (ObaAnnotation annot : annotations) {
            // log.info(annot.getName()+"  "+annot.getValue()+"\n");
            if (annot.getName().equals("label")) {
//                log.info(annot.getValue());
                return annot.getValue();
            }
        }
        return "";
    }

    protected String processString(String label) {
        return label.toLowerCase().replace("larval", "larva").replace("l1", "larva").replace("_", " ").replace("-", " ").trim().replace("ium", "");
    }

    /**
     *
     * @param s1
     * @param s2
     * @return
     */
    @GET
    @Path("StringKErnelCal/{s}/{t}")
    @Produces("text/html, text/plain, application/json")
    public String StringKernelCal(@PathParam("s") String s1, @PathParam("t") String s2) {
        String out;
        StringKernel sk = new StringKernel();
        out = String.valueOf(sk.K(s1, s2));
        return out;
    }

    @GET
    @Path("StrictStringMatcher/")
    @Produces("text/html, text/plain, application/json")
    public String StrictStringMatcher() {
        String out = "<dl>";

        OntologyResource or = OntologyHandler.getInstance().getOntology("alignment");
        ObaVirtualOntology ovo = (ObaVirtualOntology) or.getOntology();

        ArrayList<OWLClass> classes1, classes2;

        OntologyResource or1 = ovo.getOntA();
        OntologyResource or2 = ovo.getOntB();

        classes1 = ovo.exportClasses(or1.getOntology().getOntology());
        classes2 = ovo.exportClasses(or2.getOntology().getOntology());

        for (OWLClass cls : classes1) {
            out += "<dt>" + getLabel(cls, or1) + "</dt>";
        }

        return out;
    }

    @GET
    @Path("getExact/")
    @Produces("text/html, text/plain, application/json")
    public String getExact() {
        String out = "<table align=center border=1><tr><th>TrOnID</th><th>TrOn Label</th><th>FBbtID Matched</th><th>FBbt Label Matched</th></tr>";

        int countClasses1, countClasses2, countMatched = 0;
        OntologyResource or = OntologyHandler.getInstance().getOntology("alignment");
        ObaVirtualOntology ovo = (ObaVirtualOntology) or.getOntology();

        ArrayList<OWLClass> classes1, classes2;

        OntologyResource or1 = ovo.getOntA();
        OntologyResource or2 = ovo.getOntB();

        classes1 = ovo.exportClasses(or1.getOntology().getOntology());
        classes2 = ovo.exportClasses(or2.getOntology().getOntology());

        countClasses1 = classes1.size();
        countClasses2 = classes2.size();

        for (int i = classes1.size() / 2; i < classes2.size(); i++) {
            OWLClass cls = classes1.get(i);
            out += "<tr align=center><td>" + cls.toStringID().split("#")[1] + "</td><td>" + getLabel(cls, or1) + "</td> ";
            OWLClass matched = searchExactMatched(cls, classes2, or1, or2);
            if (matched != null) {
                out += "<td>" + matched.toStringID().split("#")[1] + "</td><td>" + getLabel(matched, or2) + "</td></tr>";
                countMatched++;
                classes2.remove(matched);
            } else {
                out += "<td>--</td><td>--</td></tr>";
            }
        }
//        for (int i=classes2.size()/2;i<classes2.size();i++) {
//            OWLClass cls=classes2.get(i);
//            out += "<tr align=center><td>" + cls.toStringID().split("#")[1] + "</td><td>" + getLabel(cls, or2) + "</td> ";
//            OWLClass matched = searchExactMatched(cls, classes1, or2, or1);
//            if(matched!=null){
//                out+="<td>"+matched.toStringID().split("#")[1] + "</td><td>" + getLabel(matched, or1) + "</td></tr>";
//                countMatched++;
//                classes2.remove(matched);
//            }
//            else
//                out+="<td>--</td><td>--</td></tr>";
//        }
        out += "</table>";
        out = "<h1>Exact Labels</h1><dl><dt></dt><dt><h3>Statistics</h3><dt></dt><dt><h4>Classes : </h4></dt><dt>Ontology 1= Tribolium with " + countClasses1 + " classes</dt><dt>Ontology 2= FlyBase with " + countClasses2 + " classes</dt><dt></dt><dt><h4>Matched Classes with Exact Names : " + countMatched + "</h4></dt><dt></dt>" + out;
        return out;
    }

    protected OWLClass searchExactMatched(OWLClass cls, ArrayList<OWLClass> classes, OntologyResource ont1, OntologyResource ont2) {
        String clsLabel = processString(getLabel(cls, ont1));
        Scores score = new Scores();
        for (OWLClass c : classes) {
            String label = processString(getLabel(c, ont2));
            if (score.StringKernel(clsLabel, label) == 1) {
                return c;
            }
        }
        return null;
    }

    @GET
    @Path("getStringKernelSimilar/")
    @Produces("text/html, text/plain, application/json")
    public String getStringKernelSimilar() {
        String out = "<table align=center border=1><tr><th>TrOnID</th><th>TrOn Label</th><th>FBbtID Matched</th><th>FBbt Label Matched</th><th>StringKernel Score</th></tr>";

        int countClasses1, countClasses2, countMatched = 0;
        OntologyResource or = OntologyHandler.getInstance().getOntology("alignment");
        ObaVirtualOntology ovo = (ObaVirtualOntology) or.getOntology();

        ArrayList<OWLClass> classes1, classes2;

        OntologyResource or1 = ovo.getOntA();
        OntologyResource or2 = ovo.getOntB();

        classes1 = ovo.exportClasses(or1.getOntology().getOntology());
        classes2 = ovo.exportClasses(or2.getOntology().getOntology());

        countClasses1 = classes1.size();
        countClasses2 = classes2.size();
        Scores s = new Scores();
        for (int i = 0; i < classes1.size() / 5; i++) {
            OWLClass exact = null;
            OWLClass cls = classes1.get(i);

            String label1 = getLabel(cls, or1);
            for (OWLClass candidate : classes2) {
                String label2 = getLabel(candidate, or2);
                double score = s.StringKernel(processString(label1), processString(label2));
                if (score == 1) {
                    exact = candidate;
                }
                if (score >= 0.6) {
                    out += "<tr align=center><td>" + cls.toStringID().split("#")[1] + "</td><td>" + label1 + "</td> ";
                    out += "<td>" + candidate.toStringID().split("#")[1] + "</td><td>" + label2 + "</td><td>" + score + "</td></tr>";
                    countMatched++;
                }

            }
            if (exact != null) {
                classes2.remove(exact);
            }
        }
        out += "</table>";
        out = "<h1>Similar Labels</h1><dl><dt></dt><dt><h3>Statistics</h3><dt></dt><dt><h4>Classes : </h4></dt><dt>Ontology 1= Tribolium with " + countClasses1 + " classes</dt><dt>Ontology 2= FlyBase with " + countClasses2 + " classes</dt><dt></dt><dt><h4>Matched Classes with Similar Names : " + countMatched + "</h4></dt><dt></dt>" + out;
        return out;
    }

    @GET
    @Path("getISUBSimilar/")
    @Produces("text/html, text/plain, application/json")
    public String getISUBSimilar() {
        String out = "<table align=center border=1><tr><th>TrOnID</th><th>TrOn Label</th><th>FBbtID Matched</th><th>FBbt La0bel Matched</th><th>ISUB Score</th></tr>";

        int countClasses1, countClasses2, countMatched = 0;
        OntologyResource or = OntologyHandler.getInstance().getOntology("alignment");
        ObaVirtualOntology ovo = (ObaVirtualOntology) or.getOntology();

        ArrayList<OWLClass> classes1, classes2;

        OntologyResource or1 = ovo.getOntA();
        OntologyResource or2 = ovo.getOntB();

        classes1 = ovo.exportClasses(or1.getOntology().getOntology());
        classes2 = ovo.exportClasses(or2.getOntology().getOntology());

        countClasses1 = classes1.size();
        countClasses2 = classes2.size();
        Scores s = new Scores();
        for (int i = 0; i < classes1.size() / 5; i++) {
            OWLClass exact = null;
            OWLClass cls = classes1.get(i);

            String label1 = getLabel(cls, or1);
            for (OWLClass candidate : classes2) {
                String label2 = getLabel(candidate, or2);
                double score = s.ISUBSimilarity(processString(label1), processString(label2));
                if (score == 1) {
                    exact = candidate;
                }
                if (score >= 0.6) {
                    out += "<tr align=center><td>" + cls.toStringID().split("#")[1] + "</td><td>" + label1 + "</td> ";
                    out += "<td>" + candidate.toStringID().split("#")[1] + "</td><td>" + label2 + "</td><td>" + score + "</td></tr>";
                    countMatched++;
                }

            }
            if (exact != null) {
                classes2.remove(exact);
            }
        }
        out += "</table>";
        out = "<h1>Similar Labels</h1><dl><dt></dt><dt><h3>Statistics</h3><dt></dt><dt><h4>Classes : </h4></dt><dt>Ontology 1= Tribolium with " + countClasses1 + " classes</dt><dt>Ontology 2= FlyBase with " + countClasses2 + " classes</dt><dt></dt><dt><h4>Matched Classes with Similar Names : " + countMatched + "</h4></dt><dt></dt>" + out;
        return out;
    }

    @GET
    @Path("getAllScoresSimilar/")
    @Produces("text/html, text/plain, application/json")
    public String getAllScoresSimilar() {
        String out = "<table align=center border=1><tr><th>TrOnID</th><th>TrOn Label</th><th>FBbtID Matched</th><th>FBbt La0bel Matched</th><th>Winkler Score</th><th>Levenshtein Score</th><th>StringKernel Score</th><th>ISUB Score</th></tr>";

        int countClasses1, countClasses2, countMatched = 0;
        OntologyResource or = OntologyHandler.getInstance().getOntology("alignment");
        ObaVirtualOntology ovo = (ObaVirtualOntology) or.getOntology();

        ArrayList<OWLClass> classes1, classes2;

        OntologyResource or1 = ovo.getOntA();
        OntologyResource or2 = ovo.getOntB();

        classes1 = ovo.exportClasses(or1.getOntology().getOntology());
        classes2 = ovo.exportClasses(or2.getOntology().getOntology());

        countClasses1 = classes1.size();
        countClasses2 = classes2.size();
        Scores s = new Scores();
        for (int i = 0; i < countClasses1 / 2; i++) {
        OWLClass exact = null;
        OWLClass cls = classes1.get(i);

        String label1 = getLabel(cls, or1);
        String label11 = processString(label1);
        
        for (OWLClass candidate : classes2) {
            String label2 = getLabel(candidate, or2);
            double score1=s.ISUBSimilarity(label11, label2);
            double score2=s.LevenSimilarity(label11, label2);
            if(score1>0.1 && score2<10){
            out += "<tr align=center><td>" + cls.toStringID().split("#")[1] + "</td><td>" + label1 + "</td> ";
            out += "<td>" + candidate.toStringID().split("#")[1] + "</td>";
            label2 = processString(label2);
            out += "<td>" + label2 + "</td><td>" + s.winkler(label11, label2) + "</td><td>" + score2 + "</td><td>" + s.StringKernel(label11, label2) + "</td><td>" + score1 + "</td></tr>";
            }
        }
       }
        out += "</table>";
        out = "<h1>Similar Labels</h1><dl><dt></dt><dt><h3>Statistics</h3><dt></dt><dt><h4>Classes : </h4></dt><dt>Ontology 1= Tribolium with " + countClasses1 + " classes</dt><dt>Ontology 2= FlyBase with " + countClasses2 + " classes</dt><dt></dt><dt><h4>Matched Classes with Similar Names : " + countMatched + "</h4></dt><dt></dt>" + out;
        return out;
    }

}
