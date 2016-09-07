/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server.mapping.SemanticSearchAlgorithm;

import de.sybig.oba.server.*;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class TriboliumFunctions {

    public static final String TRIBOLIUM_NS = "http://purl.org/obo/owlapi/tribolium.anatomy"; //TODO move to config
    private static final String DEV_STAGES_ID = "TrOn_0000024";
    private static final Logger log = LoggerFactory.getLogger(TriboliumFunctions.class);
    private volatile Map<ObaClass, ObaClass> concreteClasses;
    private Set<ObaClass> mixedClasses;
    private Set<ObaClass> devStages;
    private OWLObjectProperty partOfRestriction;
    private Map<ObaClass, Set<ObaClass>> hasParts;
    private Set<ObaClass> genericClasses;
    private Set<ObaClass> concreteAndAdditinalClasses;
    private ObaOntology ontology;

    
    public TriboliumFunctions(ObaOntology ont){
        reset();
        ontology=ont;
        log.info("Tribolium Function -- Ontology initiated Successfully");
        setDevStages();
        log.info("Tribolium Function -- Dev. Stages Classes initiated Successfully "+devStages.size()+" classes");
        setConcreteClasses();
        log.info("Tribolium Function -- Concrete Classes initiated Successfully "+concreteClasses.size()+" classes");
        setGenericClasses();
        log.info("Tribolium Function -- Generic Classes initiated Successfully "+genericClasses.size()+" classes");
        setConcreteAndAdditClasses();
        log.info("Tribolium Function -- Concrete Classes Set initiated Successfully "+concreteAndAdditinalClasses.size()+" classes");
        setMixedClasses();
        log.info("Tribolium Function -- Mixed Classes initiated Successfully "+mixedClasses.size()+" classes");
        addDevStagesToConcreteClasses();
        log.info("Tribolium Function -- Dev. Stages added successfully "+concreteClasses.size()+" classes");
        initHasPartsMap();
        log.info("Tribolium Function -- Has Part Map initiated Successfully "+hasParts.size()+" classes");
    }
    
    //Setters
    private void reset() {
        concreteClasses = null;
        mixedClasses = null;
        devStages = null;
        partOfRestriction = null;
        hasParts = null;
        genericClasses = null;
        concreteAndAdditinalClasses = null;
    }

    /**
     * Get all concrete classes. A concrete class has, direct or indirect, a
     * partOf relation to a developmental stage.
     *
     * @return The concrete classes
     */
    private void setConcreteClasses() {
        if (concreteClasses == null) {
            log.info("creating the list of concrete classes");
            ObaClass root = ontology.getRoot();
            concreteClasses = new HashMap<ObaClass, ObaClass>();
            addDevStagesToConcreteClasses();
            HashSet<String> restrictions = new HashSet<String>();

            restrictions.add("part_of");
            partOfRestriction = OntologyHelper.getObjectProperties(root.getOntology(), restrictions).iterator().next();

            for (ObaClass child : OntologyHelper.getChildren(root)) {
                findConcreteClassesDownstream(child);
            }
            fixPodomer();
            log.info("{} classes found", concreteClasses.size());
        }
    }

    private void addDevStagesToConcreteClasses() {
        for (ObaClass stage : devStages) {
            concreteClasses.put(stage, stage);
        }
    }

    private void setConcreteAndAdditClasses() {
        if (concreteAndAdditinalClasses == null) {
            concreteAndAdditinalClasses = new HashSet<ObaClass>();
            concreteAndAdditinalClasses.addAll(concreteClasses.keySet());
        }
    }

    private void setGenericClasses() {
        if (genericClasses == null) {
            genericClasses = new HashSet<ObaClass>();
            addToGenericClasses(ontology.getRoot());
            fixPodomer();
        }
    }

    private void setMixedClasses() {
        if (mixedClasses == null) {
            mixedClasses = new HashSet<ObaClass>();
            final String adult = "adult";
            final String pupa = "pupa";
            String larva = "larva";

            Set<ObaClass> allConcrete = concreteClasses.keySet();
            String label;
            l1:
            for (ObaClass c : allConcrete) {
                for (ObaClass p : OntologyHelper.getParents(c)) {
                    if (isParentDirectGeneric(p)) {
                        continue l1;
                    }
                    label = c.getProperty("label").getValue();
                    if (label.contains(adult)
                            || label.contains(larva) || label.contains(pupa)) {
                        continue l1;
                    }
                }
                mixedClasses.add(c);
            }
        }
    }

    /**
     * Set all ontology classes of organisms in specific developmental stage.
     * This are all classes below of the node "organism" and their children.
     *
     */

    private void setDevStages() {
        if (devStages == null) {
            devStages = new HashSet<ObaClass>();
            ObaClass devStageCls = ontology.getOntologyClass(DEV_STAGES_ID,
                    TRIBOLIUM_NS);
            for (ObaClass child : OntologyHelper.getChildren(devStageCls)) {
                addDevStagesDownstream(child);
            }
        }
    }
    
    private void fixPodomer() {
        ObaClass podomer = ontology.getOntologyClass("TrOn_0000035", null);
        if (genericClasses != null) {
            genericClasses.add(podomer);
        }
        if (concreteClasses != null) {
            concreteClasses.remove(podomer);
        }
    }
    
    private void initHasPartsMap() {
        hasParts = new HashMap<ObaClass, Set<ObaClass>>();
        ObaClass root = ontology.getRoot();
        for (ObaClass child : OntologyHelper.getChildren(root)) {
            findHasParts(child);
        }
    }

    //Getters

    public static String getTRIBOLIUM_NS() {
        return TRIBOLIUM_NS;
    }

    public static String getDEV_STAGES_ID() {
        return DEV_STAGES_ID;
    }

    public Map<ObaClass, ObaClass> getConcreteClasses() {
        return concreteClasses;
    }

    public Set<ObaClass> getMixedClasses() {
        return mixedClasses;
    }

    public Set<ObaClass> getDevStages() {
        return devStages;
    }

    public Map<ObaClass, Set<ObaClass>> getHasParts() {
        return hasParts;
    }

    public Set<ObaClass> getGenericClasses() {
        return genericClasses;
    }

    public Set<ObaClass> getConcreteAndAdditinalClasses() {
        return concreteAndAdditinalClasses;
    }
    
        /**
     * Returns a list of classes with a partOf relation to the given class. If
     * no relations are found,
     * <code>null</code> is returned.
     *
     * @param cls
     * @return
     */
    protected Set<ObaClass> getHasPart(ObaClass cls) {
        return hasParts.get(cls);
    }

    
    //Functions
    
    /**
     * Get the concrete classes downstream of a generic class. Therefor a
     * breath-first-search is started at the start class. The graph is traversed
     * down along the class hierarchy and the "hasPart" relations. From each
     * branch the first concrete class is added to the result list. It is not
     * assumed, that a concrete class has further sub classes.
     *
     * @param cls A generic class to start the search from.
     * @param ns The name space of the class.
     * @return Concrete classes downstream of the given generic class.
     */
    public Set<ObaClass> findConcreteFor(String cls,String ns) {
        ObaClass startClass = ontology.getOntologyClass(cls, ns);
        Set<ObaClass> concreteCls = findConcrete(startClass);
        return concreteCls;
    }

    /**
     * Get all concrete classes, linked to the given developmental stage,
     * downstream of the start class. See also {@link
     * #findConcreteFor(String, String)}
     *
     * @param genericCls The generic class with optional name space as matrix
     * parameter
     * @param devStageCls The developmental stage
     * @param ns The optional name space of the developmental stage
     * @return
     */
    public Set<ObaClass> devStageOfCls(ObaClass genericCls,ObaClass devStageCls,String ns) {
        Set<ObaClass> allConcrete = findConcrete(genericCls);
        HashSet<ObaClass> usedDevStages = new HashSet<ObaClass>();
        usedDevStages.add(devStageCls);
        //TODO call recursive, reuse addDevStagesDownstream()
        for (ObaClass child : OntologyHelper.getChildren(devStageCls)) {
            usedDevStages.add(child);
        }
        Set<ObaClass> result = new HashSet<ObaClass>();
        for (ObaClass c : allConcrete) {
            if (mixedClasses.contains(c)
                    || (concreteClasses.containsKey(c) && usedDevStages.contains(concreteClasses.get(c)))) {
                result.add(c);
            }
        }
        return result;
    }

    public ObaClass devStageOfCls(ObaClass concreteCls,String ns) {
        if (concreteCls == null) {
            return null;
        }
        if (!concreteClasses.containsKey(concreteCls)) {
            log.error("Class {} is not generic ", concreteCls);
            return null;
        }
        return concreteClasses.get(concreteCls);
    }

    public Set<ObaClass> getAllClasses() {
        HashSet<ObaClass> all = new HashSet<ObaClass>();
        ObaClass r = ontology.getRoot();
        all.add(r);
        addChildsToSet(r, all);
        return all;
    }

    public Set<ObaClass> getClsLoops() {
        HashSet<ObaClass> all = new HashSet<ObaClass>();
        ObaClass r = ontology.getRoot();
        searchClsLoops(r, all);
        return all;
    }

    public Set<ObaClass> getRelationLoops(String relation) {
        //TODO realy use parameter ;)
        HashSet<ObaClass> all = new HashSet<ObaClass>();
        ObaClass r = ontology.getRoot();
        searchPropertyLoops(r, all, getPartOfRestriction());
        return all;
    }

    private void addDevStagesDownstream(ObaClass start) {
        devStages.add(start);
        //TODO reenable
        // at the moment we don't find concrete classes for larval head, because
        // we search with larva and all classes are linked to L1
        for (ObaClass child : OntologyHelper.getChildren(start)) {
            addDevStagesDownstream(child);
        }
    }

    /**
     * Do a breath-first search starting at the start node. From each branch the
     * first concrete class is added to the result list. It is not assumed, that
     * a concrete class has further sub classes.
     *
     * @param startClass
     * @return
     */
    protected Set<ObaClass> findConcrete(ObaClass startClass) {
        Set<ObaClass> result = new HashSet<ObaClass>();
        findDownToConcrete(startClass, result);
        return result;
    }

    /**
     * @param cls
     */
    protected void findConcreteClassesDownstream(ObaClass cls) {

        HashSet<ObaClass> upstreamPartOf = new HashSet<ObaClass>();
        for (ObaObjectPropertyExpression ope : getPartOfRestrictions(cls)) {
            upstreamPartOf.add(ope.getTarget());
        }
        ObaClass devStage = checkIfConcrete(cls, upstreamPartOf);
        if (devStage != null) {
            addToConcreteClasses(cls, devStage);
            return;
        }

        for (ObaClass child : OntologyHelper.getChildren(cls)) {
            findConcreteClassesDownstream(child);
        }
    }

    private void addToConcreteClasses(ObaClass cls, ObaClass devStage) {
        concreteClasses.put(cls, devStage);
        for (ObaClass child : OntologyHelper.getChildren(cls)) {
            if (child.equals(cls)) {
                log.warn("sub class loop {} " + cls);
                return;
            }
            if (cls.equals(child)) {
                continue;
            }
            addToConcreteClasses(child, devStage);
        }
    }

    /**
     * Get the partOf restrictions of a class. If no partOf restrictions are
     * found an empty set is returned.
     *
     * @param cls
     * @return
     */
    protected Set<ObaObjectPropertyExpression> getPartOfRestrictions(ObaClass cls) {
        HashSet<ObaObjectPropertyExpression> partOf = new HashSet<ObaObjectPropertyExpression>();
        for (ObaObjectPropertyExpression ope : OntologyHelper.getObjectRestrictions(cls)) {
            if (!ope.getRestriction().equals(partOfRestriction)) {
                continue;
            }
            partOf.add(ope);
        }
        return partOf;
    }

    /**
     * Checks if the given class is concrete. A class is concrete if: <ul>
     * <li>If the class was already added to the list of concrete classes (by
     * its parents)</li> <li>The class has a direct partOf relation to one of
     * the stages.</li> <li>recursion</li> </ul> The class is not added to the
     * list of concrete classes, only a boolean is returned.
     *
     * @param start The class to test
     * @param upstreamCls
     * @return
     */
    protected ObaClass checkIfConcrete(ObaClass start, Set<ObaClass> upstreamCls) {
        ObaClass devStage = isConcrete(start);
        if (devStage != null) {
            return devStage;
        }

        for (ObaClass cls : upstreamCls) {
            devStage = isConcrete(cls);
            if (devStage != null) {
                return devStage;
            }
        }

        for (ObaClass cls : upstreamCls) {
            HashSet<ObaClass> newUpstream = new HashSet<ObaClass>();
            newUpstream.addAll(OntologyHelper.getParents(cls));
            for (ObaObjectPropertyExpression ope : getPartOfRestrictions(cls)) {
                newUpstream.add(ope.getTarget());
            }
            if (newUpstream.contains(cls)) {
                log.warn("partOf loop {}", cls);
                continue;
            }
            devStage = checkIfConcrete(cls, newUpstream);
            if (devStage != null) {
                return devStage;
            }
        } // for
        return null;
    }

    /**
     * Checks if the given class is a concrete class. A class is concrete, if it
     * was already added to the list of concrete classes (through it parents),
     * has a direct partOf relation to a developmental stage, or has a concrete
     * parent.
     *
     * @param cls
     * @return
     */
    protected ObaClass isConcrete(ObaClass cls) {
        if (concreteClasses.containsKey(cls)) {
            return concreteClasses.get(cls);
        }
        for (ObaObjectPropertyExpression ope : getPartOfRestrictions(cls)) {
            if (devStages.contains(ope.getTarget())) {
                return ope.getTarget();
            }
        }
        for (ObaClass p : OntologyHelper.getParents(cls)) {
            if (p.equals(cls)) {
                log.warn("subclass loop {}", p);
                break;
            }
            ObaClass x = isConcrete(p);
            if (x != null) {

                return x;
            }

        }
        return null;
    }

    /**
     * If the start class is a concrete class, the class is added to the result
     * list. Otherwise
     * <code>findDownToConcrete</code> is called for each child of the start
     * class and for each class connected. with "hasPart".
     *
     * @param cls The start class
     * @param result
     */
    protected void findDownToConcrete(ObaClass cls, Set<ObaClass> result) {
        if (concreteClasses.keySet().contains(cls) || mixedClasses.contains(cls)) {
            result.add(cls);
        }
        Set<ObaClass> downstreamClasses = new HashSet<ObaClass>();
        downstreamClasses.addAll(OntologyHelper.getChildren(cls));
        Set<ObaClass> hp = getHasPart(cls);
        if (hp != null) {
            downstreamClasses.addAll(hp);
        }
        for (ObaClass child : downstreamClasses) {
            if (child.equals(cls)) {
                log.error("loop detected " + cls);
                continue;
            }
            findDownToConcrete(child, result);
        }
    }
    
    private void findHasParts(ObaClass cls) {
        Set<ObaObjectPropertyExpression> partOfRestrictions = getPartOfRestrictions(cls);
        for (ObaObjectPropertyExpression ope : partOfRestrictions) {
            ObaClass target = ope.getTarget();
            if (!hasParts.containsKey(target)) {
                hasParts.put(target, new HashSet<ObaClass>());
            }
            hasParts.get(target).add(cls);
        }

        for (ObaClass child : OntologyHelper.getChildren(cls)) {
            if (cls.equals(child)) {
                log.warn("cls-loop " + cls);
                continue;
            }
            findHasParts(child);
        }
    }

    private void addToGenericClasses(ObaClass parent) {
        if (concreteClasses.containsKey(parent)) {
            return;
        }
        genericClasses.add(parent);
        Set<ObaClass> children = OntologyHelper.getChildren(parent);
        if (children == null) {
            return;
        }
        for (ObaClass child : children) {
            addToGenericClasses(child);
        }
    }

    /**
     * Checks if the children of the class belongs to more than one
     * developmental stage.
     *
     * @param parent
     * @return
     */
    protected boolean isParentDirectGeneric(ObaClass parent) {

        Set<ObaClass> childrenDevStages = new HashSet();
        ObaClass multi = null;
        Set<ObaClass> children = OntologyHelper.getChildren(parent);
        for (ObaClass c : children) {
            ObaClass ds = concreteClasses.get(c);
            if (ds == null) {
                continue;
            }
            if (ds.getProperty("label").getValue().endsWith("male")) {
                ds = OntologyHelper.getParents(ds).iterator().next();
            }
            if (childrenDevStages.contains(ds)) {
                if (multi == null) {
                    multi = ds;
                }
                if (multi != ds) {
                    return false;
                }
            }
            childrenDevStages.add(ds);
        }
        return childrenDevStages.size() > 1;
    }

    private void addChildsToSet(ObaClass parent, Set<ObaClass> set) {
        Set<ObaClass> children = OntologyHelper.getChildren(parent);
        if (children == null) {
            return;
        }
        for (ObaClass child : children) {
            if (child.equals(parent)) {
                continue;
            }
            set.add(child);
            addChildsToSet(child, set);
        }
    }

    protected void searchClsLoops(ObaClass parent, Set<ObaClass> set) {
        Set<ObaClass> children = OntologyHelper.getChildren(parent);
        if (children == null) {
            return;
        }
        for (ObaClass child : children) {
            if (child.equals(parent)) {
                set.add(child);
                continue;
            }
            searchClsLoops(child, set);
        }
    }

    protected void searchPropertyLoops(ObaClass parent, Set<ObaClass> set, OWLObjectProperty restriction) {
        HashSet<ObaObjectPropertyExpression> partOf = new HashSet<ObaObjectPropertyExpression>();
        for (ObaObjectPropertyExpression ope : OntologyHelper.getObjectRestrictions(parent)) {
            if (!ope.getRestriction().equals(restriction)) {
                continue;
            }
            if (ope.getTarget().equals(parent)) {
                set.add(parent);
            }
        }
        Set<ObaClass> children = OntologyHelper.getChildren(parent);
        if (children == null) {
            return;
        }

        for (ObaClass child : children) {
            if (child.equals(parent)) {
                continue;
            }
            searchPropertyLoops(child, set, restriction);
        }
    }

    protected OWLObjectProperty getRestriction(String restriction) {
        Set<String> r = new HashSet<String>();
        r.add(restriction);
        return OntologyHelper.getObjectProperties(ontology.getOntology(), r).iterator().next();
    }

    protected OWLObjectProperty getPartOfRestriction() {
        if (partOfRestriction == null) {
            partOfRestriction = getRestriction("part_of");
        }
        return partOfRestriction;
    }
}
