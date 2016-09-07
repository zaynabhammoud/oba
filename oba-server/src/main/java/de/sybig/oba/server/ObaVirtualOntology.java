/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server;

import de.sybig.oba.server.mapping.SemanticSearchAlgorithm.Candidate;
import de.sybig.oba.server.mapping.SemanticSearchAlgorithm.Scores;
import de.sybig.oba.server.mapping.SemanticSearchAlgorithm.TriboliumFunctions;
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
    List<Candidate> mappingsLex, mappingsStr, mappings;
    HashMap<ObaClass, Candidate> mapps;
    HashMap<ObaClass, List<Candidate>> mappsStr;
    private final static int n = 3;

    @Override
    public ObaClass getRoot() {
        return ontA.ontology.getRoot();
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
        mappingsLex = new ArrayList<Candidate>();
        mappingsStr = new ArrayList<Candidate>();
        mapps = new HashMap<ObaClass, Candidate>();
        mappsStr = new HashMap<ObaClass, List<Candidate>>();
        try {
            mappings = alignLex();
            mappingsStr = alignStruc();
            mappings = combineAlign();
        } catch (Exception ex) {
            Logger.getLogger(ObaVirtualOntology.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Candidate> alignLex() throws Exception {
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
                mapps.put(mapped.getCls(), mapped);
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
        ArrayList<String> wordsBArray = new ArrayList<String>();

        int l1 = wordsA.length, l2 = wordsB.length;
        wordsBArray.addAll(Arrays.asList(wordsB));
        if (l1 == l2) {
            int i;
            //Search for flipped names
            for (i = 0; i < l1; i++) {
                if (!wordsBArray.contains(wordsA[i])) {
                    break;
                }
            }
            if (i == l1) {
                return 1.0;
            }
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
            ObaAnnotation label = oCls.getProperty("label");
            if (label != null) {
                    labels.add(label.getValue());
            }
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

    public List<Candidate> alignStruc() throws Exception {
        List<Candidate> outList = new ArrayList<Candidate>();
        TriboliumFunctions func = new TriboliumFunctions(ontA.ontology);
        Scores s = new Scores();
        int sum = getSum();
        ObaClass rootA = new ObaClass(ontA.getCls("TrOn_0000001", func.TRIBOLIUM_NS), ontA.ontology.onto);
        ObaClass rootB = new ObaClass(ontB.getCls("FBbt_10000000", "http://purl.org/obo/owlapi/fly_anatomy.ontology#"), ontB.ontology.onto);
        Set<ObaClass> children = OntologyHelper.getChildren(rootA);
        Candidate c = new Candidate(rootA, rootB, 3.0);
        List<Candidate> l = new ArrayList<Candidate>();
        l.add(c);
        mappsStr.put(rootA, l);
        outList.add(c);
        for (ObaClass child : children) {
            outList.addAll(recAlign(child, c, func, s, sum));
        }
        return outList;
    }

    public List<Candidate> recAlign(ObaClass cls, Candidate startCls, TriboliumFunctions f, Scores s, int sum) throws Exception {
        List<Candidate> outList = new ArrayList<Candidate>();
        List<Candidate> candidates = getCandidatesChildren(cls, startCls, s, sum, 1);
        Candidate best = getMaxCandidate(candidates);

        if (best != null) {
            outList.add(best);
        } else {
            best = startCls;
        }
        //Children mapping
        Set<ObaClass> childrenCls = OntologyHelper.getChildren(cls);
        //The Class is a leaf
        if (childrenCls == null || childrenCls.isEmpty()) {
            return outList;
        }
        for (ObaClass child : childrenCls) {
            outList.addAll(recAlign(child, best, f, s, sum));
        }
        return outList;
    }

    public List<Candidate> getCandidatesChildren(ObaClass cls, Candidate startClass, Scores s, int sum, int level) throws Exception {
        if (level == 4) {
            return new ArrayList<Candidate>();
        }
        List<Candidate> candidates = new ArrayList<Candidate>();
        //loop for children
        Set<ObaClass> children = OntologyHelper.getChildren(startClass.getClsMapped().getReal(), ontB.ontology.onto);
        if (children == null || children.isEmpty()) {
            return candidates;
        }
        String labelA = cls.getProperty("label").getValue();
        for (ObaClass child : children) {
            //calculate the similarity between the class and the child
            String labelB = child.getProperty("label").getValue();
            double similarity = getAggregatedScore(labelA, labelB, s, sum);
            similarity = similarity * 1 / 7;
            similarity += startClass.getSimilarityValue() * 6 / 7;
            Candidate c = new Candidate(cls, child, similarity);
            candidates.add(c);
            candidates.addAll(getCandidatesChildren(cls, c, s, sum, level + 1));
        }
        return candidates;
    }

    public Candidate getMaxCandidate(List<Candidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        Candidate best = candidates.get(0);
        mappsStr.put(best.getCls(), candidates);
        for (int i = 1; i < candidates.size(); i++) {
            if (candidates.get(i).compareTo(best) == 1) {
                best = candidates.get(i);
            }
        }
        return best;
    }

    public Candidate getMaxCandidate(List<Candidate> candidates, Candidate bestLex) {
        Candidate best = candidates.get(0);
        if (bestLex != null && best.getClsMapped() == bestLex.getClsMapped()) {
            best.setSimilarityValue(best.getSimilarityValue() + bestLex.getSimilarityValue());
        }
        for (int i = 1; i < candidates.size(); i++) {
            if (bestLex != null && candidates.get(i).getClsMapped() == bestLex.getClsMapped()) {
                candidates.get(i).setSimilarityValue(candidates.get(i).getSimilarityValue() + bestLex.getSimilarityValue());
            }
            if (candidates.get(i).compareTo(best) == 1) {
                best = candidates.get(i);
            }

        }
        return best;
    }

    public List<Candidate> combineAlign() {
        List<Candidate> outList = new ArrayList<Candidate>();
        Set<ObaClass> classes = mappsStr.keySet();
        for (ObaClass cls : classes) {
            List<Candidate> candidates = mappsStr.get(cls);
            Candidate bestLex = mapps.get(cls);
            outList.add(getMaxCandidate(candidates, bestLex));
        }
        return outList;
    }

    //Getters and Setters
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

    public List<Candidate> getMappingsLex() {
        return mappingsLex;
    }

    public List<Candidate> getMappingsStr() {
        return mappingsStr;
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
}
