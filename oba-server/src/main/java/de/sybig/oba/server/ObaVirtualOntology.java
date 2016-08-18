/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server;

import de.sybig.oba.server.mapping.SemanticSearchAlgorithm.Candidate;
import de.sybig.oba.server.mapping.SemanticSearchAlgorithm.Scores;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 *
 * @author zaynab
 */
public class ObaVirtualOntology extends ObaOntology {

    OntologyResource ontA, ontB;
    ArrayList<OWLClass> clsA, clsB;
    ArrayList<String> labelsA, labelsB;
    HashMap<String, Double> thds;
    HashMap<String, Double> weights;
    List<Candidate> mappings;
    private final static int n = 3;    

    public HashMap<String, Double> getThds() {
        return thds;
    }

    public HashMap<String, Double> getWeights() {
        return weights;
    }

    public void setWeights(HashMap<String, Double> weights) {
        this.weights = weights;
    }

    public List<Candidate> getMappings() {
        return mappings;
    }

    public void setMappings(List<Candidate> mappings) {
        this.mappings = mappings;
    }

    public void setThds(HashMap<String, Double> thds) {
        this.thds = thds;
    }

    /**
     * Sets the ontology used in iteration. We consider that the OntA is one
     * that we will iterate over it.
     *
     * @param ontology
     */
    public void setOntA(OntologyResource onto) {
        ontA = onto;
    }

    /**
     * Sets the ontology used in comparison. The elements of this ontology will
     * be compared and mapped to OntA.
     *
     * @param ontology
     */
    public void setOntB(OntologyResource onto) {
        ontB = onto;
    }

    public OntologyResource getOntA() {
        return ontA;
    }

    public OntologyResource getOntB() {
        return ontB;
    }

    public ArrayList<OWLClass> getClsA() {
        return clsA;
    }

    public ArrayList<OWLClass> getClsB() {
        return clsB;
    }

    public ArrayList<String> getLabelsA() {
        return labelsA;
    }

    public ArrayList<String> getLabelsB() {
        return labelsB;
    }

    /**
     * Get the root node of the ontology. The root is returned as proxy from
     * Type ObaClass with the ontology set.
     *
     * @return The root of the ontology.
     */
    @Override
    public ObaClass getOntologyClass(final String cls, final String ns) {
        return ontA.ontology.getOntologyClass(cls, ns);
    }

    @Override
    protected void scanClasses(OWLOntology ontology)
            throws CorruptIndexException, LockObtainFailedException,
            IOException {

    }

    @Override
    public synchronized void init() throws OWLOntologyCreationException {
        logger.info("Initializing ontology alignment");
        exportClasses(ontA.getOntology().getOntology(), 'A');
        exportClasses(ontB.getOntology().getOntology(), 'B');
        String[] line1 = properties.getProperty("thds").trim().split("/");
        String[] line2 = properties.getProperty("weights").trim().split("/");
        thds = new HashMap<String, Double>();
        weights = new HashMap<String, Double>();
        for (String thsd : line1) {
            String[] th = thsd.split(",");
            if (th.length != 2) {
                continue;
            }
            thds.put(th[0], Double.valueOf(th[1]));
        }
        for (String weight : line2) {
            String[] we = weight.split(",");
            if (we.length != 2) {
                continue;
            }
            weights.put(we[0], Double.valueOf(we[1]));
        }
        mappings = new ArrayList<Candidate>();
        try {
            mappings = align();
        } catch (Exception ex) {
            Logger.getLogger(ObaVirtualOntology.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Candidate> align() throws Exception {
        long startTime = System.currentTimeMillis();
        logger.info("Alignment just STARTED !!!!");
        OWLOntology ont1 = ontA.ontology.getOntology(), ont2 = ontB.ontology.getOntology();
        int sum = getSum();
        Scores s = new Scores();
        List<Candidate> outList = new ArrayList<Candidate>();
        int countClasses1 = clsA.size();
        ArrayList<OWLClass> maps = clsB;
        for (int i = 0; i < countClasses1; i++) {
            Candidate mapped = null;
            for (int j = 0; j < maps.size(); j++) {
                Candidate c = new Candidate(new ObaClass(clsA.get(i), ont1), new ObaClass(maps.get(j), ont2));
                c.setSimilarityValue(getAggregatedScore(c.getNameString(), c.getMostSimilarOntRes(), s, sum));
                if (c.getSimilarityValue() == 0) {
                    continue;
                }
                if (c.getSimilarityValue() == 1) {
                    mapped = c;
                    maps.remove(j);
                    continue;
                }
                if (mapped == null) {
                    mapped = c;
                } else if (c.compareTo(mapped) == 1) {
                    mapped = c;
                } else if (c.compareTo(mapped) == 0) {
                    if (s.MetricLCS(labelsA.get(i), labelsB.get(j)) <= s.MetricLCS(labelsA.get(i), mapped.getMostSimilarOntRes())) {
                        mapped = c;
                    }
                }
            }
            if (mapped == null) {
                continue;
            }
            if (mapped.getSimilarityValue() > thds.get("total")) {
                outList.add(mapped);
            }
        }
        logger.info("Alignment just FINISHED !!!!");
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        logger.info("TOTAL TIME : " + elapsedTime / 1000 + " seconds !!!!!!");
        return outList;
    }

    protected String processString(String label) {
        return label.toLowerCase().replace("larval", "larva").replace("tibial", "tibia").replace("_", " ").replace("-", " ").trim().replace("ium", "");
    }

    protected double getAggregatedScore(String labelA, String labelB, Scores s, int sum) throws Exception {
        double score = 0.0;
        labelA = processString(labelA);
        labelB = processString(labelB);

        if ((labelA.contains("pupal") && !labelB.contains("pupal"))
                || (!labelA.contains("pupal") && labelB.contains("pupal"))
                || (labelA.contains("larva") && !labelB.contains("larva"))
                || (!labelA.contains("larva") && labelB.contains("larva"))
                || (labelA.contains("segment") && !labelB.contains("segment"))
                || (labelB.contains("segment") && !labelA.contains("segment"))
                || (labelA.matches(".*\\d.*") && !labelB.matches(".*\\d.*"))
                || (!labelA.matches(".*\\d.*") && labelB.matches(".*\\d.*"))) {
            return 0;
        }

        if (labelB.startsWith("pupal/adult")) {
            if (labelA.startsWith("pupal")) {
                labelB = labelB.replace("pupal/adult", "pupal");
            } else if (labelA.startsWith("adult")) {
                labelB = labelB.replace("pupal/adult", "adult");
            }
        }

        if (labelB.startsWith("embryonic/larva")) {
            if (labelA.startsWith("larva")) {
                labelB = labelB.replace("embryonic/larva", "larva");
            } else if (labelA.startsWith("embryonic")) {
                labelB = labelB.replace("embryonic/larva", "embryonic");
            }
        }

        if (labelA.equals(labelB)) {
            return 1.0;
        }

        String[] wordsA = labelA.split(" "), wordsB = labelB.split(" ");
        ArrayList<String> wordsBArray=new ArrayList<String>();
        
        int l1 = wordsA.length, l2 = wordsB.length;
        wordsBArray.addAll(Arrays.asList(wordsB));
        if (l1 == l2) {
            int i;
            //Search for flipped names
            for(i=0;i<l1;i++)
                if(!wordsBArray.contains(wordsA[i]))
                    break;
            if(i==l1)
                return 1.0;
            for (i = 0; i < l1; i++) {
                if (!wordsA[i].equals(wordsB[i])) {
                    labelA += wordsA[i] + " ";
                    labelB += wordsB[i] + " ";
                }
            }
        } else {
            int l = wordsA.length < wordsB.length ? wordsA.length : wordsB.length;
            for (int i = 0; i < l; i++) {
                if (wordsA[i].equals(wordsB[i])) {
                    wordsA[i] = "";
                    wordsB[i] = "";
                }
            }
            for (int i = l1 - 1, j = l2 - 1; i < l && j < l; i--, j--) {
                if (wordsA[i].equals(wordsB[j])) {
                    wordsA[i] = "";
                    wordsB[i] = "";
                }
            }
            labelA = String.join(" ", wordsA);
            labelB = String.join(" ", wordsB);
        }

        if (labelA.equals("") || labelB.equals("")) {
            return 0.0;
        }

        score += s.ISUBSimilarity(labelA, labelB) * weights.get("ISUB");

        score += (1 - s.NormalizedLevenDistance(labelA, labelB)) * weights.get("NormLeven");

        score += (1 - s.MetricLCS(labelA, labelB)) * weights.get("MetricLCS");

        score += s.NGram(labelA, labelB, n) * weights.get("NGram");

        score += s.StringKernel(labelA, labelB) * weights.get("StringKernel");

        score += s.PreComputedCosine(labelA, labelB, n) * weights.get("cosine");

        score = score / sum;
        return score;
    }

    protected int getSum() {
        int sum = 0;
        sum += weights.get("ISUB");
        sum += weights.get("NormLeven");
        sum += weights.get("MetricLCS");
        sum += weights.get("NGram");
        sum += weights.get("StringKernel");
        sum += weights.get("cosine");
        return sum;
    }

    public void exportClasses(OWLOntology ontology, char letter) {
        ArrayList<OWLClass> outClasses = new ArrayList<OWLClass>();
        ArrayList<String> labels = new ArrayList<String>();
        Set<OWLDeclarationAxiom> classes = ontology.getAxioms(AxiomType.DECLARATION);
        for (OWLDeclarationAxiom c : classes) {

            OWLEntity entity = c.getSignature().iterator().next();
            if (!(entity instanceof OWLClass)) {
                // skip AnnotationProperties, ObjectProperties, DataProperites
                continue;
            }
            OWLClass cls = (OWLClass) entity;
            outClasses.add(cls);
            ObaClass oCls = new ObaClass(cls, ontology);
            labels.add(oCls.getProperty("label").getValue());
        }
        switch (letter) {
            case 'A':
                labelsA = labels;
                clsA = outClasses;
                break;
            case 'B':
                labelsB = labels;
                clsB = outClasses;
                ;
                break;
        }
    }
  
}
