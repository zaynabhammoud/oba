/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
        String[] line=properties.getProperty("thds").trim().split("/");
        thds=new HashMap<String,Double>();
        for(String thsd:line){
            String[] th=thsd.split(",");
            if(th.length!=2){
                continue;
            }
            thds.put(th[0], Double.valueOf(th[1]));
        }
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
