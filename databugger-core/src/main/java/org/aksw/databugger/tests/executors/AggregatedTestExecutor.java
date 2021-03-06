package org.aksw.databugger.tests.executors;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import org.aksw.databugger.Utils.SparqlUtils;
import org.aksw.databugger.sources.Source;
import org.aksw.databugger.tests.TestCase;
import org.aksw.databugger.tests.results.AggregatedTestCaseResult;
import org.aksw.databugger.tests.results.TestCaseResult;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import java.util.Arrays;
import java.util.List;

/**
 * User: Dimitris Kontokostas
 * Executes results for AggregatedTestCaseResult sets
 * Created: 2/2/14 4:05 PM
 */
public class AggregatedTestExecutor extends TestExecutor {

    @Override
    protected List<TestCaseResult> executeSingleTest(Source source, TestCase testCase) {
        int total = -1, prevalence = -1;

        try {
            prevalence = getCountNumber(source.getExecutionFactory(), testCase.getSparqlPrevalenceQuery(), "total");
        } catch (QueryExceptionHTTP e) {
            if (SparqlUtils.checkStatusForTimeout(e))
                prevalence = -1;
            else
                prevalence = -2;
        } catch (Exception e) {
            prevalence = -2;
        }

        if (prevalence != 0) {
            // if prevalence !=0 calculate total
            try {
                total = getCountNumber(source.getExecutionFactory(), testCase.getSparqlAsCountQuery(), "total");
            } catch (QueryExceptionHTTP e) {
                if (SparqlUtils.checkStatusForTimeout(e))
                    total = -1;
                else
                    total = -2;
            } catch (Exception e) {
                total = -2;
            }
        } else
            // else total will be 0 anyway
            total = 0;

        return Arrays.<TestCaseResult>asList(new AggregatedTestCaseResult(testCase, total, prevalence));
    }

    private int getCountNumber(QueryExecutionFactory model, String query, String var) {
        return getCountNumber(model, QueryFactory.create(query), var);
    }

    private int getCountNumber(QueryExecutionFactory model, Query query, String var) {

        int result = 0;
        QueryExecution qe = null;
        try {
            qe = model.createQueryExecution(query);
            ResultSet results = qe.execSelect();

            if (results != null && results.hasNext()) {
                QuerySolution qs = results.next();
                result = qs.get(var).asLiteral().getInt();
            }
        } finally {
            if (qe != null)
                qe.close();
        }

        return result;

    }
}
