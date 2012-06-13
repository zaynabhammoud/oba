/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.client;

import de.sybig.oba.server.JsonAnnotation;
import de.sybig.oba.server.JsonCls;
import java.util.Set;

/**
 *
 * @author juergen.doenitz@bioinf.med.uni-goettingen.de
 */
public class OboClass extends OntologyClass {

    public OboClass() {
        super();
    }

    public OboClass(JsonCls c) {
        super(c);
    }

    public String getLabel() {
        if (getLabels() != null && getLabels().size() == 1) {
            return ((JsonAnnotation) super.getLabels().iterator().next()).getValue(); //FIXME remove cast?
        }
        return null;
    }

    public String getDefinition() {
        Set<JsonAnnotation> defs = getAnnotationValues("def");
        if (defs != null && defs.size() > 0) {
            return defs.iterator().next().getValue();
        }
        return null;
    }
    @Override
	protected OntologyClass createNewOntologyClass(JsonCls c) {
		return new OboClass(c);
	}
}
