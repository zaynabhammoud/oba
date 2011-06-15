/*
 * Created on Apr 16, 2010
 *
 */
package de.sybig.oba.cytomer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.PathSegment;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sybig.oba.server.HtmlBase;
import de.sybig.oba.server.ObaAnnotation;
import de.sybig.oba.server.ObaClass;
import de.sybig.oba.server.ObaObjectPropertyExpression;
import de.sybig.oba.server.OntologyFunction;
import de.sybig.oba.server.OntologyFunctions;
import de.sybig.oba.server.OntologyHelper;
import de.sybig.oba.server.StorageHandler;

public class CytomerFunctions extends OntologyFunctions implements
		OntologyFunction {
	private Logger logger = LoggerFactory.getLogger(CytomerFunctions.class);
	private Properties cytomerProps;
	private ObaClass organCls;
	private Set<ObaClass> organList;

	// private static Map<ObaOntology, Set<OWLObjectProperty>>
	// organRestrictionsMap = new HashMap<ObaOntology,
	// Set<OWLObjectProperty>>();

	/**
	 * A class providing ontology functions specific for Cytomer.
	 * 
	 */
	public CytomerFunctions() {
		super();
		cytomerProps = new Properties();
		try {
			cytomerProps.load(getClass().getResourceAsStream(
					"/cytomer.properties"));
		} catch (IOException e) {
			logger.error("could not load properties for cytomer function class");
			e.printStackTrace();
		}
	}

	// @Override
	// public void setOntology(ObaOntology ontology) {
	// this.ontology = ontology;
	// }

	/**
	 * Gets a short documentation of the implemented functions in html.
	 */
	@GET
	@Path("/")
	@Produces("text/html")
	@Override
	public String getRoot() {
		StringBuffer out = new StringBuffer();
		out.append("<h1>Available functions</h1>\n");
		out.append("<dl>");
		out.append("<dt>/organList</dt><dd>Gets a list of all organs</dd>");
		out.append("<dt>/organsOf/{cls}</dt><dd>Gets a list of organs this class is associated with. The class may be a subclass of an organ or is connected to the organ through the restrictions 'isPartOf', 'isCellOf' and 'isPartOfOrgan'</dd>");
		out.append("<dt>/findUpstreamInSet/{cls}/{partition}/{set}</dt><dd>From the given starting class an upstream search is started until a class of the stored list is found. Besides of the class hierarchy the following relations are used: 'isPartOf', 'isCellOf' and 'isPartOfOrgan'. If at least one class from the stored set is found, all classes found in this step are returned.</dd>");
		out.append("<dt>/findDownstreamInSet/{cls}/{partition}/{set}</dt><dd>From the given starting class an downs search is started until a class of the stored list is found. Besides of the class hierarchy the following relations are used: 'hasPart', 'hasCell' and 'hasOrganPart'. If at least one class from the stored set is found, all classes found in this step are returned.</dd>");
		out.append("</dl>");
		return out.toString();
	}

	/**
	 * Gets the list of organs defined in the ontology. This are the direct or
	 * indirect children of the class 'organ'. From organ the class hierarchy is
	 * descended until a class is found, that is not annotated with
	 * 'abstract=true'. The list of organs may also contain organs specific for
	 * embryonal stages.
	 * 
	 * @return The list of organs
	 */
	@GET
	@Path("/organList")
	@Produces("text/plain, text/html, application/json")
	@HtmlBase("../../cls/")
	public Set<ObaClass> getOrganList() {
		logger.info("getting the list of organs");
		return getOrgans();
	}

/**
	  * Gets the organs the class belongs to. All ancestors of the class are tested, if they belong to the set of organs ({@link CytomerFunctions#getOrgans()). 
	  * @param cls
	  * @param ns
	  * @return
	  */
	@GET
	@Path("/organsOf/{cls}")
	@Produces("text/plain, text/html, application/json")
	@HtmlBase("../../../cls/")
	public Set<ObaClass> getOrgansFor(@PathParam("cls") String cls,
			@QueryParam("ns") String ns) {
		// http://localhost:9998/cytomer/functions/cytomer/organsOf/left_medial_segment_of_liver?ns=http://cytomer.bioinf.med.uni-goettingen.de/organ#
		// cuboidal_epithelial_cell
		logger.info("getting organs for {} in namespace {}", cls, ns);

		ObaClass startClass = ontology.getOntologyClass(cls, ns);
		if (startClass == null) {
			logger.warn("The start class could not be found in the ontology");
			throw new WebApplicationException(404);
		}
		Set<ObaClass> organSet = findOrgans(startClass);
		return organSet;
	}

	/**
	 * Starting form the given class a upstream search is started until a class
	 * stored in the referenced set is found. If the start class itself a direct
	 * or indirect ancestor of it is in the reference set, this class is
	 * returned. Otherwise the graph is searched along the properties retrieved
	 * by the {@link #getSearchUpToSetRestrictions()} from the property file.
	 * 
	 * <ul>
	 * <li>If the start class is not found a web exception with status code 404
	 * is thrown.</li>
	 * <li>If the reference is not found a web exception with status code 404 is
	 * thrown.</li>
	 * <li>If no class is found in the reference set an empty list is returned.</li>
	 * </ul>
	 * 
	 * @param startCls
	 *            The start class
	 * @param partition
	 *            The partition the list is stored in
	 * @param set
	 *            The name of the list with the stored classes
	 * @return The matching class from the reference list
	 */
	@GET
	@Path("/findUpstreamInSet/{cls}/{partition}/{set}")
	@Produces("text/plain, text/html, application/json")
	public Set<ObaClass> findUpstreamInSet(
			@PathParam("cls") PathSegment startCls,
			@PathParam("partition") String partition,
			@PathParam("set") String set) {
		ObaClass startClass;
		try {
			startClass = this.getClassFromPathSegement(startCls, null);
		} catch (IllegalArgumentException ex) {
			logger.error("could not get the start class for 'findUpstreamInSet'");
			throw new WebApplicationException(404);
		}

		// is the class itself member of the refernce set?
		StorageHandler storageHandler = new StorageHandler();
		Set<ObaClass> referenceSet = storageHandler.getStorage(partition, set);
		Set<ObaClass> resultSet = new HashSet<ObaClass>();
		if (referenceSet.contains(startClass)) {
			resultSet.add(startClass);
			return resultSet;
		}

		// first test if we have an ancestors of a class of the set
		Set<ObaClass> startClasses = new HashSet<ObaClass>();
		startClasses.add(startClass);
		resultSet = searchUpStreamToSet(startClasses, referenceSet);
		if (resultSet.size() > 0) {
			return resultSet;
		}

		// search along the properties
		Set<OWLObjectProperty> searchRestrictions = getSearchUpToSetRestrictions();
		Set<ObaClass> found = searchAlongRelationsToSet(startClasses,
				searchRestrictions, referenceSet);
		return found;
	}

	/**
	 * @see #findUpstreamInSet(PathSegment, String, String)
	 * @param startCls
	 * @param partition
	 * @param set
	 * @return
	 */
	@GET
	@Path("/findDownstreamInSet/{cls}/{partition}/{set}")
	@Produces("text/plain, text/html, application/json")
	public Set<ObaClass> findDownstreamInSet(
			@PathParam("cls") PathSegment startCls,
			@PathParam("partition") String partition,
			@PathParam("set") String set) {
		ObaClass startClass;
		try {
			startClass = this.getClassFromPathSegement(startCls, null);
		} catch (IllegalArgumentException ex) {
			logger.error("could not get the start class for 'findUpstreamInSet'");
			throw new WebApplicationException(400);
		}

		// is the class in the reference set?
		StorageHandler storageHandler = new StorageHandler();
		Set<ObaClass> referenceSet = storageHandler.getStorage(partition, set);
		Set<ObaClass> resultSet = new HashSet<ObaClass>();
		if (referenceSet.contains(startClass)) {
			resultSet.add(startClass);
			return resultSet;
		}

		// search along the class hierarchy
		Set<ObaClass> startClasses = new HashSet<ObaClass>();
		startClasses.add(startClass);
		resultSet = searchDownStreamToSet(startClasses, referenceSet);
		if (resultSet.size() > 0) {
			return resultSet;
		}

		// search along the properties
		Set<OWLObjectProperty> searchRestrictions = getSearchDownToSetRestrictions();
		Set<ObaClass> found = searchAlongRelationsToSet(startClasses,
				searchRestrictions, referenceSet);
		return found;
	}

	// public void _get
	private Set<ObaClass> searchAlongRelationsToSet(Set<ObaClass> startClasses,
			Set<OWLObjectProperty> searchRestrictions,
			Set<ObaClass> referenceSet) {
		Set<ObaClass> resultSet = new HashSet<ObaClass>();
		Set<ObaClass> children = new HashSet<ObaClass>();
		for (OWLClass startClass : startClasses) {
			for (ObaObjectPropertyExpression relation : OntologyHelper
					.getObjectRestrictions(startClass)) {
				if (!searchRestrictions.contains(relation.getRestriction())) {
					continue;
				}
				if (referenceSet.contains(relation.getTarget())) {
					resultSet.add(relation.getTarget());
				} else {
					children.add(relation.getTarget());
				}
			}
		}
		if (resultSet.size() > 0 || children.size() < 1) {
			return resultSet;
		}
		return searchAlongRelationsToSet(children, searchRestrictions,
				referenceSet);
	}

	/**
	 * Searches downstream using the is_a relations
	 * 
	 * @param startClasses
	 * @param referenceSet
	 * @return
	 */
	private Set<ObaClass> searchDownStreamToSet(Set<ObaClass> startClasses,
			Set<ObaClass> referenceSet) {
		Set<ObaClass> resultSet = new HashSet<ObaClass>();
		Set<ObaClass> children = new HashSet<ObaClass>();

		for (ObaClass startClass : startClasses) {
			for (ObaClass c : OntologyHelper.getChildren(startClass)) {
				if (referenceSet.contains(c)) {
					resultSet.add(c);
				} else {
					children.add(c);
				}
			}
		}
		if (resultSet.size() > 0 || children.size() < 1) {
			return resultSet;
		}
		return searchDownStreamToSet(children, referenceSet);
	}

	/**
	 * Searches upstream using the is_a relations
	 * 
	 * @param startClasses
	 * @param referenceSet
	 * @return
	 */
	private Set<ObaClass> searchUpStreamToSet(Set<ObaClass> startClasses,
			Set<ObaClass> referenceSet) {
		Set<ObaClass> resultSet = new HashSet<ObaClass>();
		Set<ObaClass> parents = new HashSet<ObaClass>();

		for (ObaClass startClass : startClasses) {
			for (ObaClass c : OntologyHelper.getParents(startClass)) {
				if (referenceSet.contains(c)) {
					resultSet.add(c);
				} else {
					parents.add(c);
				}
			}
		}
		if (resultSet.size() > 0 || parents.size() < 1) {
			return resultSet;
		}
		return searchDownStreamToSet(parents, referenceSet);
	}

	protected Set<ObaClass> getOrgans() {
		if (organList == null) {
			Set<ObaClass> organs = new HashSet<ObaClass>();
			OWLClass organRoot = getOrganCls();
			Set<ObaClass> children = OntologyHelper.getChildren(organRoot,
					ontology.getOntology());

			organs.addAll(findConcreteOrgans(children));
			organList = organs;
		}
		return organList;
	}

	private HashSet<ObaClass> findConcreteOrgans(Set<ObaClass> classes) {
		HashSet<ObaClass> organs = new HashSet<ObaClass>();
		clsloop: for (ObaClass cls : classes) {
			Set<ObaAnnotation> annotations = OntologyHelper
					.getAnnotationProperties(cls, ontology.getOntology());

			for (ObaAnnotation annotation : annotations) {
				if ("abstract".equals(annotation.getName())) {
					if (annotation.getValue().equals("false")) {
						organs.add(cls);
					} else {
						Set<ObaClass> children = OntologyHelper.getChildren(
								cls, ontology.getOntology());
						organs.addAll(findConcreteOrgans(children));
					}
					continue clsloop;
				}
			} // for annotations
			organs.add(cls);
		} // for classes
		return organs;
	}

	//
	private boolean isClsOrgan(OWLClass cls) {
		// Set<OWLClassExpression> parents = cls.getSuperClasses(ontology
		// .getOntology());
		// if (parents.contains(getOrganCls())) {
		// return true;
		// }
		// return false;
		return getOrgans().contains(cls);
	}

	ObaClass getOrganCls() {
		if (organCls == null) {
			organCls = ontology.getOntologyClass(cytomerProps.getProperty(
					"organ_name", "organ"), cytomerProps.getProperty(
					"organ_ns",
					"http://protege.stanford.edu/plugins/owl/protege#"));
			logger.info("getting organ cls with {} in NS {}",
					cytomerProps.getProperty("organ_name"),
					cytomerProps.getProperty("organ_ns"));
		}
		return organCls;
	}

	/**
	 * Searches the organs the entity belongs to. First it is tested, if the
	 * start class is a direct successor of the organ class. In this case the
	 * real organ is searched in the paths from the start class to the organ
	 * class. These organs are returned and the search is stopped.
	 * 
	 * If the start class is not a successor of the organ class, the start class
	 * is expanded upstream using the object property restrictions
	 * 
	 * @param startClass
	 * @return
	 */
	private Set<ObaClass> findOrgans(ObaClass startClass) {
		Set<ObaClass> organSet = new HashSet<ObaClass>();
		// search organs in the class hierarchies
		List<List<ObaClass>> pathsToOrgan = searchXdownstreamOfY(startClass,
				getOrganCls());
		if (pathsToOrgan != null) {
			for (List<ObaClass> path : pathsToOrgan) {
				for (ObaClass cls : path) {
					if (isClsOrgan(cls)) {
						organSet.add(cls);
					}
				}
			}
		}
		Set<ObaObjectPropertyExpression> restrictions = OntologyHelper
				.getObjectRestrictions(startClass, ontology.getOntology());
		for (ObaObjectPropertyExpression restriction : restrictions) {
			if (isOrganRestriction(restriction.getRestriction())) {
				Set<ObaClass> foundOrgans = findOrgans(restriction.getTarget());
				if (foundOrgans != null) {
					organSet.addAll(foundOrgans);
				}
			}
		}

		return organSet;
	}

	// private Set<OWLClass> findInSet(OWLClass startClass) {
	//
	// return null;
	// }

	private boolean isOrganRestriction(OWLObjectProperty prop) {
		if (getOrganRestrictions().contains(prop)) {
			return true;
		}
		return false;
	}

	private Set<OWLObjectProperty> getOrganRestrictions() {
		// if (!organRestrictionsMap.containsKey(ontology)) {
		String organRestrictionsString = cytomerProps.getProperty(
				"organ_restrictions", "isPartOf, isCellOf, isPartOfOrgan");
		return getRestrictionsSet(organRestrictionsString);
		// }
	}

	private Set<OWLObjectProperty> getSearchUpToSetRestrictions() {
		String restrictionsString = cytomerProps.getProperty(
				"set_upstream_restrictions",
				"isPartOf, isCellOf, isPartOfOrgan");
		return getRestrictionsSet(restrictionsString);
	}

	private Set<OWLObjectProperty> getSearchDownToSetRestrictions() {
		String restrictionsString = cytomerProps
				.getProperty("set_downstream_restrictions",
						"hasPart, hasCell, hasOrganPart");
		return getRestrictionsSet(restrictionsString);
	}

	private Set<OWLObjectProperty> getRestrictionsSet(String restrictionsString) {
		String trimmedRestrictionsString = restrictionsString.replace(" ", "");
		String[] restrictionsArray = trimmedRestrictionsString.split(",");
		Set<OWLObjectProperty> restrictions = OntologyHelper
				.getObjectProperties(ontology.getOntology(),
						Arrays.asList(restrictionsArray));
		// organRestrictionsMap.put(ontology, organRestrictions);
		// Set<OWLObjectProperty> r = organRestrictionsMap.get(ontology);
		return restrictions;
	}
}
