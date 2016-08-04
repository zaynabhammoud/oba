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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
    HashMap<String,Double> weights;
    private final static int n=2;

    public HashMap<String, Double> getThds() {
        return thds;
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
    public ObaClass getRoot() {
        // the root could also be retrieved directly from the ontology with
        // ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing()'
        ObaOntology obOntA = ontA.getOntology();
        return obOntA.getRoot();
    }

    @Override
    protected void scanClasses(OWLOntology ontology)
            throws CorruptIndexException, LockObtainFailedException,
            IOException {

    }
    
    @Override
    public synchronized void init() throws OWLOntologyCreationException {
        exportClasses(ontA.getOntology().getOntology(),'A');
        exportClasses(ontB.getOntology().getOntology(),'B');
        String[] line1=properties.getProperty("thds").trim().split("/");
        String[] line2=properties.getProperty("weights").trim().split("/");
        thds=new HashMap<String,Double>();
        weights=new HashMap<String,Double>();
        for(String thsd:line1){
            String[] th=thsd.split(",");
            if(th.length!=2){
                continue;
            }
            thds.put(th[0], Double.valueOf(th[1]));
        }
        for(String weight:line2){
            String[] we=weight.split(",");
            if(we.length!=2){
                continue;
            }
            weights.put(we[0], Double.valueOf(we[1]));
        }
    }
    public List<Candidate> align() throws Exception{
        OWLOntology ont1=ontA.ontology.getOntology(),ont2=ontB.ontology.getOntology();
        List<Candidate> outList=new ArrayList<Candidate>();
        Scores s = new Scores();
        int countClasses1=clsA.size();
        int countClasses2=clsB.size();
        for (int i = 0; i < countClasses1 / 2; i++) {
            for (int j = 0; j < countClasses2; j++) {
                Candidate c=new Candidate(new ObaClass(clsA.get(i),ont1),new ObaClass(clsA.get(j),ont2));
                c.setSimilarityValue(getAggregatedScore(c.getNameString(),c.getMostSimilarOntRes()));
                outList.add(c);
            }
        }
       return outList;
    }
    
     protected String processString(String label) {
        return label.toLowerCase().replace("larval", "larva").replace("l1", "larva").replace("_", " ").replace("-", " ").trim().replace("ium", "");
    }
     
    protected double getAggregatedScore(String labelA,String labelB) throws Exception{
        double score=0.0;
        int sum=0;
        Scores s=new Scores();
        labelA=processString(labelA);
        labelB=processString(labelB);
        
        score+=s.ISUBSimilarity(labelA, labelB)*weights.get("ISUB");
        sum+=weights.get("ISUB");
        
        score+=(1-s.NormalizedLevenDistance(labelA, labelB))*weights.get("NormLeven");
        sum+=weights.get("NormLeven");
        
        score+=(1-s.MetricLCS(labelA, labelB))*weights.get("MetricLCS");
        sum+=weights.get("MetricLCS");
        
        score+=s.NGram(labelA, labelB,n)*weights.get("NGram");
        sum+=weights.get("NGram");
        
        score+=s.StringKernel(labelA, labelB)*weights.get("StringKernel");
        sum+=weights.get("StringKernel");
        
        score+=s.PreComputedCosine(labelA, labelB,n)*weights.get("cosine");
        sum+=weights.get("cosine");
        
        score=score/sum;
        return score;
    }
    
    public void exportClasses(OWLOntology ontology,char letter) {
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
        switch(letter){
            case 'A':labelsA=labels;clsA=outClasses;break;
            case 'B':labelsB=labels;clsB=outClasses;;break;
        }
    }

}
