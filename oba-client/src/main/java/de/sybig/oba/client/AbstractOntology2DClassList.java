package de.sybig.oba.client;


import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.sybig.oba.server.Json2DClsList;
import de.sybig.oba.server.JsonCls;
import de.sybig.oba.server.JsonClsList;
/**
 *
 * @author juergen.doenitz@bioinf.med.uni-goettingen.de
 */
@XmlRootElement
@XmlType
public abstract class AbstractOntology2DClassList <CL extends AbstractOntologyClassList<C>, C extends OntologyClass>
		extends Json2DClsList<JsonClsList<JsonCls>, OntologyClass> {
private List<OntologyClassList> listEntities;

	@Override
	public List getEntities() {
		if (_entities == null) {
			return null;
		}
		if (listEntities == null) {
			listEntities = new LinkedList<OntologyClassList>();
			for (JsonClsList c : _entities) {
				if (c instanceof OntologyClassList) {
					listEntities.add((OntologyClassList) c);
				}
				OntologyClassList nc = (OntologyClassList) new OntologyClassList(
						(JsonClsList) c);
				listEntities.add(nc);
			}
		}
		return listEntities;
	}

	public void setConnector(GenericConnector connector) {
		for (Object list : getEntities()) {

			if (list instanceof OntologyClassList) {
				OntologyClassList ol = (OntologyClassList) list;
				ol.setConnector(connector);
			}
		}
	}
}
