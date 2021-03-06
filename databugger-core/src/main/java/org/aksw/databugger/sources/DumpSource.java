package org.aksw.databugger.sources;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.databugger.enums.TestAppliesTo;
import org.aksw.databugger.exceptions.TripleReaderException;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;

import java.util.List;

/**
 * User: Dimitris Kontokostas
 * Defines a source based on an RDF Dump
 * This can be any type of dump (ttl, nt, rdfa, rdf, etc)
 * This has to be more sophisticated in the end (e.g. read zippped files) but there is always time to improve ;)
 * TODO refactor sources and make this a subclass of DatasetSource
 * Created: 2/6/14 9:32 AM
 */
public class DumpSource extends Source {

    public DumpSource(String prefix, String uri) {
        this(prefix, uri, null);
    }

    public DumpSource(String prefix, String uri, List<SchemaSource> schemata) {
        super(prefix, uri);
        if (schemata != null)
            addReferencesSchemata(schemata);
    }

    @Override
    public TestAppliesTo getSourceType() {
        return TestAppliesTo.Dataset;
    }

    @Override
    protected QueryExecutionFactory initQueryFactory() {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
        try {
            // Read & load the URI
            model.read(getUri());
            //Load all the related ontologies as well (for more consistent querying
            for (Source src : getReferencesSchemata()) {
                QueryExecutionFactory qef = src.getExecutionFactory();
                if (qef instanceof QueryExecutionFactoryModel ) {
                    model.add(((QueryExecutionFactoryModel) qef).getModel());
                }
            }
        } catch (Exception e) {
            log.error("Cannot read dump URI: " + getUri() + " Reason: " + e.getMessage());
            e.printStackTrace();
        }
        return new QueryExecutionFactoryModel(model);
    }
}
