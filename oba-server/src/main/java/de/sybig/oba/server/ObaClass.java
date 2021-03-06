package de.sybig.oba.server;

import java.util.Set;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObjectVisitor;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;

public class ObaClass implements OWLClass {
	private OWLClass real;
	private OWLOntology onto;

	public ObaClass(OWLClass cls, OWLOntology ontology) {
		super();
		setReal(cls);
		setOntology(ontology);
	}

	public OWLClass getReal() {
		return real;
	}

	public void setReal(OWLClass real) {
		if (real == null) {
			throw new IllegalArgumentException();
		}
		this.real = real;
	}

	public OWLOntology getOntology() {
		return onto;
	}

	public void setOntology(OWLOntology ontology) {
		this.onto = ontology;
	}

	@Override
	public ClassExpressionType getClassExpressionType() {
		return real.getClassExpressionType();
	}

	@Override
	public boolean isAnonymous() {
		return real.isAnonymous();
	}

	@Override
	public boolean isClassExpressionLiteral() {
		return real.isClassExpressionLiteral();
	}

	@Override
	public OWLClass asOWLClass() {
		return real.asOWLClass();
	}

	@Override
	public boolean isOWLThing() {
		return real.isOWLThing();
	}

	@Override
	public boolean isOWLNothing() {
		return real.isOWLNothing();
	}

	@Override
	public OWLClassExpression getNNF() {
		return real.getNNF();
	}

	@Override
	public OWLClassExpression getComplementNNF() {
		return real.getComplementNNF();
	}

	@Override
	public OWLClassExpression getObjectComplementOf() {
		return real.getObjectComplementOf();
	}

	@Override
	public Set<OWLClassExpression> asConjunctSet() {
		return real.asConjunctSet();
	}

	@Override
	public boolean containsConjunct(OWLClassExpression ce) {
		return real.containsConjunct(ce);
	}

	@Override
	public Set<OWLClassExpression> asDisjunctSet() {
		return real.asDisjunctSet();
	}

	@Override
	public void accept(OWLClassExpressionVisitor visitor) {
		real.accept(visitor);
	}

	@Override
	public <O> O accept(OWLClassExpressionVisitorEx<O> visitor) {
		return real.accept(visitor);
	}

	@Override
	public Set<OWLEntity> getSignature() {
		return real.getSignature();
	}

	@Override
	public Set<OWLClass> getClassesInSignature() {
		return real.getClassesInSignature();
	}

	@Override
	public Set<OWLDataProperty> getDataPropertiesInSignature() {
		return real.getDataPropertiesInSignature();
	}

	@Override
	public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
		return real.getObjectPropertiesInSignature();
	}

	@Override
	public Set<OWLNamedIndividual> getIndividualsInSignature() {
		return real.getIndividualsInSignature();
	}

	@Override
	public Set<OWLDatatype> getDatatypesInSignature() {
		return real.getDatatypesInSignature();
	}

	@Override
	public Set<OWLClassExpression> getNestedClassExpressions() {
		return real.getNestedClassExpressions();
	}

	@Override
	public void accept(OWLObjectVisitor visitor) {
		real.accept(visitor);
	}

	@Override
	public <O> O accept(OWLObjectVisitorEx<O> visitor) {
		return real.accept(visitor);
	}

	@Override
	public boolean isTopEntity() {
		return real.isTopEntity();
	}

	@Override
	public boolean isBottomEntity() {
		return real.isBottomEntity();
	}

	@Override
	public int compareTo(OWLObject o) {
		return real.compareTo(o);
	}

	public boolean equals(Object o) {
		return real.equals(o);
	}

	public int hashCode() {
		return real.hashCode();
	}

	@Override
	public EntityType getEntityType() {
		return real.getEntityType();
	}

	@Override
	public <E extends OWLEntity> E getOWLEntity(EntityType<E> entityType) {
		return real.getOWLEntity(entityType);
	}

	@Override
	public boolean isType(EntityType entityType) {
		return real.isType(entityType);
	}

	@Override
	public Set<OWLAnnotation> getAnnotations(OWLOntology ontology) {
		return real.getAnnotations(ontology);
	}

	@Override
	public Set<OWLAnnotation> getAnnotations(OWLOntology ontology,
			OWLAnnotationProperty annotationProperty) {
		return real.getAnnotations(ontology, annotationProperty);
	}

	@Override
	public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(
			OWLOntology ontology) {
		return real.getAnnotationAssertionAxioms(ontology);
	}

	@Override
	public boolean isBuiltIn() {
		return real.isBuiltIn();
	}

	@Override
	public boolean isOWLClass() {
		return real.isOWLClass();
	}

	@Override
	public boolean isOWLObjectProperty() {
		return real.isOWLObjectProperty();
	}

	@Override
	public OWLObjectProperty asOWLObjectProperty() {
		return real.asOWLObjectProperty();
	}

	@Override
	public boolean isOWLDataProperty() {
		return real.isOWLDataProperty();
	}

	@Override
	public OWLDataProperty asOWLDataProperty() {
		return real.asOWLDataProperty();
	}

	@Override
	public boolean isOWLNamedIndividual() {
		return real.isOWLNamedIndividual();
	}

	@Override
	public OWLNamedIndividual asOWLNamedIndividual() {
		return real.asOWLNamedIndividual();
	}

	@Override
	public boolean isOWLDatatype() {
		return real.isOWLDatatype();
	}

	@Override
	public OWLDatatype asOWLDatatype() {
		return real.asOWLDatatype();
	}

	@Override
	public boolean isOWLAnnotationProperty() {
		return real.isOWLAnnotationProperty();
	}

	@Override
	public OWLAnnotationProperty asOWLAnnotationProperty() {
		return real.asOWLAnnotationProperty();
	}

	@Override
	public String toStringID() {
		return real.toStringID();
	}

	@Override
	public Set<OWLAxiom> getReferencingAxioms(OWLOntology ontology) {
		return real.getReferencingAxioms(ontology);
	}

	@Override
	public Set<OWLAxiom> getReferencingAxioms(OWLOntology ontology,
			boolean includeImports) {
		return real.getReferencingAxioms(ontology, includeImports);
	}

	@Override
	public void accept(OWLEntityVisitor visitor) {
		real.accept(visitor);
	}

	@Override
	public <O> O accept(OWLEntityVisitorEx<O> visitor) {
		return real.accept(visitor);
	}

	@Override
	public IRI getIRI() {
		return real.getIRI();
	}

	@Override
	public void accept(OWLNamedObjectVisitor visitor) {
		real.accept(visitor);
	}

	@Override
	public Set<OWLClassExpression> getSuperClasses(OWLOntology ontology) {
		return real.getSuperClasses(ontology);
	}

	@Override
	public Set<OWLClassExpression> getSuperClasses(Set<OWLOntology> ontologies) {
		return real.getSuperClasses(ontologies);
	}

	@Override
	public Set<OWLClassExpression> getSubClasses(OWLOntology ontology) {
		return real.getSubClasses(ontology);
	}

	@Override
	public Set<OWLClassExpression> getSubClasses(Set<OWLOntology> ontologies) {
		return real.getSubClasses(ontologies);
	}

	/**
	 * @see OWLClass#getEquivalentClasses(OWLOntology)
	 */
	@Override
	public Set<OWLClassExpression> getEquivalentClasses(OWLOntology ontology) {
		return real.getEquivalentClasses(ontology);
	}

	/**
	 * @see OWLClass#getEquivalentClasses(Set)
	 */
	@Override
	public Set<OWLClassExpression> getEquivalentClasses(
			Set<OWLOntology> ontologies) {
		return real.getEquivalentClasses(ontologies);
	}

	/**
	 * @see OWLClass#getDisjointClasses(OWLOntology)
	 */
	@Override
	public Set<OWLClassExpression> getDisjointClasses(OWLOntology ontology) {
		return real.getDisjointClasses(ontology);
	}

	/**
	 * @see OWLClass#getDisjointClasses(Set)
	 */
	@Override
	public Set<OWLClassExpression> getDisjointClasses(
			Set<OWLOntology> ontologies) {
		return real.getDisjointClasses(ontologies);
	}

	/**
	 * @see OWLClass#getIndividuals(OWLOntology)
	 */
	@Override
	public Set<OWLIndividual> getIndividuals(OWLOntology ontology) {
		return real.getIndividuals(ontology);
	}

	/**
	 * @see OWLClass#getIndividuals(Set)
	 */
	@Override
	public Set<OWLIndividual> getIndividuals(Set<OWLOntology> ontologies) {
		return real.getIndividuals(ontologies);
	}

	/**
	 * @see OWLClass#isDefined(OWLOntology)
	 */
	@Override
	public boolean isDefined(OWLOntology ontology) {

		return real.isDefined(ontology);
	}

	/**
	 * @see OWLClass#isDefined(Set)
	 */
	@Override
	public boolean isDefined(Set<OWLOntology> ontologies) {
		return real.isDefined(ontologies);
	}

	public String toString() {
		return real.toString();
	}
}
