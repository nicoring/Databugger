package org.aksw.databugger.tests.results;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import org.aksw.databugger.services.PrefixService;
import org.aksw.databugger.tests.TestCase;

import java.util.List;

/**
 * User: Dimitris Kontokostas
 * Description
 * Created: 2/2/14 3:57 PM
 */
public class ExtendedTestCaseResult extends RLOGTestCaseResult {

    private final List<ResultAnnotation> resultAnnotations;

    public ExtendedTestCaseResult(TestCase testCase, String resource, String message, String logLevel) {
        super(testCase, resource, message, logLevel);
        this.resultAnnotations = testCase.getResultAnnotations();
    }

    public ExtendedTestCaseResult(TestCase testCase, RLOGTestCaseResult rlogResult) {
        super(testCase, rlogResult.getResource(), rlogResult.getMessage(), rlogResult.getLogLevel());
        this.resultAnnotations = testCase.getResultAnnotations();
    }

    @Override
    public Resource serialize(Model model, String sourceURI) {
        Resource resource = super.serialize(model, sourceURI)
                .addProperty(RDF.type, model.createResource(PrefixService.getPrefix("tddo") + "ExtendedTestCaseResult"));

        for (ResultAnnotation annotation : resultAnnotations) {
            resource.addProperty(ResourceFactory.createProperty(PrefixService.getPrefix("tddo"), "resultAnnotation"), annotation.serializeAsTestCase(model));
        }

        return resource;
    }

}
