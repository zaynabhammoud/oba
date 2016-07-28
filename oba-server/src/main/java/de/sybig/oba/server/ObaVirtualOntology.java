/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 *
 * @author zaynab
 */
public class ObaVirtualOntology extends ObaOntology {

    OntologyResource ontA, ontB;

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

    public ArrayList exportClasses(OWLOntology ontology) {
        ArrayList<OWLClass> outClasses = new ArrayList<OWLClass>();
        Set<OWLDeclarationAxiom> classes = ontology.getAxioms(AxiomType.DECLARATION);
        int counter = 0;
        clsLoop:
        for (OWLDeclarationAxiom c : classes) {

            OWLEntity entity = c.getSignature().iterator().next();
            if (!(entity instanceof OWLClass)) {
                // skip AnnotationProperties, ObjectProperties, DataProperites
                continue;
            }
            OWLClass cls = (OWLClass) entity;
            outClasses.add(cls);
            counter++;
        }
        return outClasses;
    }

}
