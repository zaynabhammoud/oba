/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server.mapping.SemanticSearchAlgorithm;

import de.sybig.oba.server.ObaClass;
import de.sybig.oba.server.ObaVirtualOntology;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author zaynab
 */
public class OWLAlignmentFormat {

    OWLDataFactory dataFactory;
    OWLOntology ont;
    OWLOntologyManager manager;
    
    protected List<OWLOntologyChange> listAxioms2Add;

    public OWLAlignmentFormat(OWLDataFactory dataFactory, OWLOntologyManager manager) {
        this.dataFactory = dataFactory;
        this.manager = manager;

    }

    public String addAnnotation(String id, String property, String value) {
        IRI iri = IRI.create("http://purl.org/obo/owlapi/tribolium.anatomy" + id);
        OWLClass owlcls = dataFactory.getOWLClass(iri);
        ObaClass cls = new ObaClass(owlcls, ont);
        try {
            return addAnnotation(cls, property, value);
        } catch (OWLOntologyStorageException ex) {
            Logger.getLogger(ObaVirtualOntology.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public String addAnnotation(ObaClass cls, String property, String value) throws OWLOntologyStorageException {
        OWLClass owlcls = cls.getReal();
        OWLAnnotationProperty prop = dataFactory.getOWLAnnotationProperty(IRI.create("http://www.w3.org/2002/07/owl#" + property));
        OWLLiteral literal = dataFactory.getOWLLiteral(value, "en");
        OWLAnnotation annot = dataFactory.getOWLAnnotation(prop, literal);
        Set<OWLAnnotation> annotations = cls.getAnnotations(ont);
        OWLAxiom ax = dataFactory.getOWLAnnotationAssertionAxiom(owlcls.getIRI(), annot);
        manager.applyChange(new AddAxiom(ont, ax));
        for (OWLAnnotation an : annotations) {
            ax = dataFactory.getOWLAnnotationAssertionAxiom(owlcls.getIRI(), an);
            manager.applyChange(new AddAxiom(ont, ax));
        }
        File temp = new File("temp.owl");
        IRI ir = IRI.create(temp);
        manager.saveOntology(ont, new OWLXMLOntologyFormat(), ir);
        return temp.getPath();
    }

    public OWLAxiom createEquivalenceMapping(OWLClass cls1, OWLClass cls2) {
        return dataFactory.getOWLEquivalentClassesAxiom(cls1, cls2);
    }

    public OWLAxiom createDataPropertyEquivalenceMapping(OWLDataProperty dprop1, OWLDataProperty dprop2) {

        return dataFactory.getOWLEquivalentDataPropertiesAxiom(dprop1, dprop2);

    }

    public OWLAxiom createObjectPropertyEquivalenceMapping(OWLObjectProperty oprop1, OWLObjectProperty oprop2) {

        return dataFactory.getOWLEquivalentObjectPropertiesAxiom(oprop1, oprop2);

    }

    public OWLAxiom createSubClassOfMapping(OWLClass cls1, OWLClass cls2) {

        return dataFactory.getOWLSubClassOfAxiom(cls1, cls2);

    }

    public OWLAxiom createSubDataPropertyMapping(OWLDataProperty dprop1, OWLDataProperty dprop2) {

        return dataFactory.getOWLSubDataPropertyOfAxiom(dprop1, dprop2);

    }

    public OWLAxiom createSubObjectPropertyMapping(OWLObjectProperty oprop1, OWLObjectProperty oprop2) {

        return dataFactory.getOWLSubObjectPropertyOfAxiom(oprop1, oprop2);

    }

    public OWLAxiom createSuperClassOfMapping(OWLClass cls1, OWLClass cls2) {

        return dataFactory.getOWLSubClassOfAxiom(cls2, cls1);

    }

    public OWLAxiom createSuperDataPropertyMapping(OWLDataProperty dprop1, OWLDataProperty dprop2) {

        return dataFactory.getOWLSubDataPropertyOfAxiom(dprop2, dprop1);

    }

    public OWLAxiom createSuperObjectPropertyMapping(OWLObjectProperty oprop1, OWLObjectProperty oprop2) {

        return dataFactory.getOWLSubObjectPropertyOfAxiom(oprop2, oprop1);

    }

    public OWLAxiom createSameIndividualMapping(OWLNamedIndividual indiv1, OWLNamedIndividual indiv2) {

        return dataFactory.getOWLSameIndividualAxiom(indiv1, indiv2);
    }

        public void addClassMapping2Output(String iri1, String iri2, int dir_mapping, double conf) {	
			listAxioms2Add.add(new AddAxiom(
					ont,
					createEquivalenceMapping(
							dataFactory.getOWLClass(IRI.create(iri1)),
							dataFactory.getOWLClass(IRI.create(iri2)))));
		
	}
	
	
	/**
	 * Creates OWL axioms for mapping and adds it to the list
	 */
	public void addDataPropMapping2Output(String iri1, String iri2, int dir_mapping, double conf) {	
			listAxioms2Add.add(new AddAxiom(
					ont,
					createDataPropertyEquivalenceMapping(
							dataFactory.getOWLDataProperty(IRI.create(iri1)),
							dataFactory.getOWLDataProperty(IRI.create(iri2)))));
	}   
}
