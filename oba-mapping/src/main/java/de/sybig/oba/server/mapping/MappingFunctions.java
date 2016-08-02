/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server.mapping;

import de.sybig.oba.server.mapping.SemanticSearchAlgorithm.Scores;
import cc.mallet.types.StringKernel;
import de.sybig.oba.server.ObaClass;
import de.sybig.oba.server.ObaVirtualOntology;
import de.sybig.oba.server.OntologyFunction;
import de.sybig.oba.server.OntologyFunctions;
import de.sybig.oba.server.OntologyHandler;
import de.sybig.oba.server.OntologyResource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.ws.rs.*;
import org.semanticweb.owlapi.model.OWLClass;
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

    public MappingFunctions() {
        super();
        onto = OntologyHandler.getInstance().getOntology("alignment");
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
        File file = new File("C:/Users/mashi/Desktop/FBbtBeetle--" + timeStamp + ".csv");
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

            ObaClass cls1 = new ObaClass(ont1.getCls("FBbt" + fbbtID[1], "http://purl.org/obo/owlapi/fly_anatomy.ontology#"), ont1.getOntology().getOntology());
            ObaClass cls2 = new ObaClass(ont2.getCls("TrOn" + trOnID[1], "http://purl.org/obo/owlapi/tribolium.anatomy#"), ont2.getOntology().getOntology());
            if (cls2 == null) {
                log.info("Tribolium Class Null");
                return "TrOn Class Null";
            }
            if (cls1 == null) {
                log.info("FlyBase Class Null");
                return "FBbt Class Null";
            }
            String name1 = processString(cls1.getProperty("label").getValue());
            String name2 = processString(cls2.getProperty("label").getValue());
            String score = StringKernelCal(name1, name2);
            writer.append("FBbt" + fbbtID[1] + "," + name1 + "," + "TrOn" + trOnID[1] + "," + name2 + "," + score + "\n");
            writer.flush();
        }
        writer.close();
        return file.getPath();

    }
    
    @GET
    @Path("writeAllScore/")
    @Produces("text/html, text/plain, application/json")
    public String writeAllScore() throws FileNotFoundException, IOException, Exception{
                log.info("Writing Started");

        ObaVirtualOntology ovo=(ObaVirtualOntology) onto.getOntology();
        ArrayList<String> labels1,labels2;
        labels1=ovo.getLabelsA();
        labels2=ovo.getLabelsB();
        Scores s=new Scores();
        int countClasses1=labels1.size(),countClasses2=labels2.size();
        String out="";
        int i;
        for (i = 0; i < countClasses1/2; i++) {
            String label1 = labels1.get(i);
            String label11 = processString(label1);

            for (int j = 0; j < countClasses2; j++) {
                String label2 = labels2.get(j);
                String label22 = processString(label2);
                double score1 = s.ISUBSimilarity(label11, label22);
                double score2 = s.LevenSimilarity(label11, label22);
                double score3 = s.StringKernel(label11, label22);
                double score4=s.DamerauDistance(label11, label22);
                double score5=s.QGram(label11, label22, 2);
                if (score1 > 0.1 && score2 < 10 && score3 > 0.4 && score4<5 && score5<10) {
                    out +=label1 + ","+ label2 + "," + s.winkler(label11, label22) + "," + score2 + "," + score3 + "," + score1+","+s.NormalizedLevenDistance(label11, label22) +","+score4+"," +s.LCS(label11, label22) +","+s.MetricLCS(label11, label22) +","+s.NGram(label11, label22, 2)+","+score5+","+s.PreComputedCosine(label11, label22, 2)+ "\n";
                }
            }
        }
        log.info("Last index i="+i);
        out = "Beetle Label,Fly Label,Winkler (no thsd),Levenshtein (thsd=10),StringKernel (thsd=0.4),ISUB (thsd=0.1),NormalizedLevenshtein (no thsd),Damerau (thsd=5),LCS (no thsd),MetricLCS (no thsd),N-Gram 2 (no thsd),Q-Gram 2 (thsd=7),Pre ComputedCosine 2 (no thsd)\n"+out;
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        File file = new File("C:/Users/mashi/Desktop/AllFBbtBeetle--" + timeStamp + ".csv");
        if (!file.exists()) {
            file.createNewFile();
        }
        log.info("file {} created", file.getPath());
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        
        writer.append(out);
        writer.close();
        return file.getPath();
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
    @Path("getExact/")
    @Produces("text/html, text/plain, application/json")
    public String getExact() {
        String out = "<table align=center border=1><tr><th>TrOnID</th><th>TrOn Label</th><th>FBbtID Matched</th><th>FBbt Label Matched</th></tr>";

        int countClasses1, countClasses2, countMatched = 0;
        ObaVirtualOntology ovo = (ObaVirtualOntology) onto.getOntology();

        ArrayList<OWLClass> classes1, classes2;
        ArrayList<String> labelsA, labelsB;

        OntologyResource or1 = ovo.getOntA();
        OntologyResource or2 = ovo.getOntB();

        classes1 = ovo.getClsA();
        classes2 = ovo.getClsB();

        labelsA = ovo.getLabelsA();
        labelsB = ovo.getLabelsB();

        countClasses1 = classes1.size();
        countClasses2 = classes2.size();

        for (int i = classes1.size() / 2; i < classes2.size(); i++) {
            OWLClass cls = classes1.get(i);
            out += "<tr align=center><td>" + cls.toStringID().split("#")[1] + "</td><td>" + labelsA.get(i) + "</td> ";
            int matched = searchExactMatchedIndex(labelsA.get(i), labelsB);
            if (matched != -1) {
                out += "<td>" + classes2.get(matched).toStringID().split("#")[1] + "</td><td>" + labelsB.get(matched) + "</td></tr>";
                countMatched++;
                classes2.remove(matched);
            } else {
                out += "<td>--</td><td>--</td></tr>";
            }
        }
        out += "</table>";
        out = "<h1>Exact Labels</h1><dl><dt></dt><dt><h3>Statistics</h3><dt></dt><dt><h4>Classes : </h4></dt><dt>Ontology 1= Tribolium with " + countClasses1 + " classes</dt><dt>Ontology 2= FlyBase with " + countClasses2 + " classes</dt><dt></dt><dt><h4>Matched Classes with Exact Names : " + countMatched + "</h4></dt><dt></dt>" + out;
        return out;
    }

    protected int searchExactMatchedIndex(String cls, ArrayList<String> classes) {
        String clsLabel = processString(cls);
        Scores score = new Scores();
        for (int i = 0; i < classes.size(); i++) {
            String label = processString(classes.get(i));
            if (score.StringKernel(clsLabel, label) == 1) {
                return i;
            }
        }
        return -1;
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
        ArrayList<String> labelsA, labelsB;

        OntologyResource or1 = ovo.getOntA();
        OntologyResource or2 = ovo.getOntB();

        classes1 = ovo.getClsA();
        classes2 = ovo.getClsB();

        labelsA = ovo.getLabelsA();
        labelsB = ovo.getLabelsB();
        countClasses1 = classes1.size();
        countClasses2 = classes2.size();
        Scores s = new Scores();
        for (int i = 0; i < classes1.size() / 5; i++) {
            int exact = -1;
            OWLClass cls = classes1.get(i);

            String label1 = labelsA.get(i);
            for (int j = 0; j < classes2.size(); i++) {
                String label2 = labelsB.get(j);
                double score = s.StringKernel(processString(label1), processString(label2));
                if (score == 1) {
                    exact = j;
                }
                if (score >= 0.6) {
                    out += "<tr align=center><td>" + cls.toStringID().split("#")[1] + "</td><td>" + label1 + "</td> ";
                    out += "<td>" + classes2.get(j).toStringID().split("#")[1] + "</td><td>" + label2 + "</td><td>" + score + "</td></tr>";
                    countMatched++;
                }

            }
            if (exact != -1) {
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
        ArrayList<String> labelsA, labelsB;

        OntologyResource or1 = ovo.getOntA();
        OntologyResource or2 = ovo.getOntB();

        classes1 = ovo.getClsA();
        classes2 = ovo.getClsB();

        labelsA = ovo.getLabelsA();
        labelsB = ovo.getLabelsB();
        countClasses1 = classes1.size();
        countClasses2 = classes2.size();
        Scores s = new Scores();
        for (int i = 0; i < classes1.size() / 5; i++) {
            int exact = -1;
            OWLClass cls = classes1.get(i);

            String label1 = labelsA.get(i);
            for (int j = 0; j < classes2.size(); j++) {
                String label2 = labelsB.get(j);
                double score = s.ISUBSimilarity(processString(label1), processString(label2));
                if (score >= 0.6) {
                    out += "<tr align=center><td>" + cls.toStringID().split("#")[1] + "</td><td>" + label1 + "</td> ";
                    out += "<td>" + classes2.get(j).toStringID().split("#")[1] + "</td><td>" + label2 + "</td><td>" + score + "</td></tr>";
                    countMatched++;
                }
                if (score == 1) {
                    exact = j;
                }

            }
            if (exact != -1) {
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
        ArrayList<String> labelsA, labelsB;

        OntologyResource or1 = ovo.getOntA();
        OntologyResource or2 = ovo.getOntB();

        classes1 = ovo.getClsA();
        classes2 = ovo.getClsB();

        labelsA = ovo.getLabelsA();
        labelsB = ovo.getLabelsB();
        countClasses1 = classes1.size();
        countClasses2 = classes2.size();
        Scores s = new Scores();
        for (int i = 0; i < countClasses1 / 2; i++) {
            int exact = -1;
            OWLClass cls = classes1.get(i);

            String label1 = labelsA.get(i);
            String label11 = processString(label1);

            for (int j = 0; j < classes2.size(); j++) {
                String label2 = labelsB.get(j);
                String label22 = processString(label2);
                double score1 = s.ISUBSimilarity(label11, label22);
                double score2 = s.LevenSimilarity(label11, label22);
                if (score1 > 0.1 && score2 < 10) {
                    out += "<tr align=center><td>" + cls.toStringID().split("#")[1] + "</td><td>" + label1 + "</td> ";
                    out += "<td>" + classes1.get(i).toStringID().split("#")[1] + "</td>";
                    out += "<td>" + label2 + "</td><td>" + s.winkler(label11, label22) + "</td><td>" + score2 + "</td><td>" + s.StringKernel(label11, label22) + "</td><td>" + score1 + "</td></tr>";
                }
            }
        }
        out += "</table>";
        out = "<h1>Similar Labels</h1><dl><dt></dt><dt><h3>Statistics</h3><dt></dt><dt><h4>Classes : </h4></dt><dt>Ontology 1= Tribolium with " + countClasses1 + " classes</dt><dt>Ontology 2= FlyBase with " + countClasses2 + " classes</dt><dt></dt><dt><h4>Matched Classes with Similar Names : " + countMatched + "</h4></dt><dt></dt>" + out;
        return out;
    }

}
