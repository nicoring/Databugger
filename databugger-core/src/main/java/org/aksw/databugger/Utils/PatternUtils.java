package org.aksw.databugger.Utils;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import org.aksw.databugger.enums.PatternParameterConstraints;
import org.aksw.databugger.patterns.Pattern;
import org.aksw.databugger.patterns.PatternParameter;
import org.aksw.databugger.tests.results.ResultAnnotation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dimitris Kontokostas
 * Description
 * Created: 9/23/13 11:09 AM
 */
public class PatternUtils {
    public static List<Pattern> instantiatePatternsFromModel(QueryExecutionFactory queryFactory) {
        List<Pattern> patterns = new ArrayList<Pattern>();

        String sparqlSelectPatterns = DatabuggerUtils.getAllPrefixes() +
                "SELECT distinct ?sparqlPattern ?id ?desc ?sparql ?sparqlPrev ?variable WHERE { " +
                " ?sparqlPattern a tddo:Pattern ; " +
                "  dcterms:identifier ?id ; " +
                "  dcterms:description ?desc ; " +
                "  tddo:sparqlWherePattern ?sparql ; " +
                "  tddo:sparqlPrevalencePattern ?sparqlPrev ; " +
                "} ORDER BY ?sparqlPattern";
        String sparqlSelectParameters = DatabuggerUtils.getAllPrefixes() +
                " SELECT distinct ?parameterURI ?id ?constraint ?constraintPattern WHERE { " +
                " %%PATTERN%%  tddo:parameter ?parameterURI . " +
                " ?parameterURI a tddo:Parameter . " +
                " ?parameterURI dcterms:identifier ?id . " +
                " OPTIONAL {?parameterURI tddo:parameterConstraint ?constraint .}" +
                " OPTIONAL {?parameterURI tddo:constraintPattern ?constraintPattern .}" +
                " } ";

        QueryExecution qe = queryFactory.createQueryExecution(sparqlSelectPatterns);
        ResultSet results = qe.execSelect();

        while (results.hasNext()) {
            QuerySolution qs = results.next();

            String patternURI = qs.get("sparqlPattern").toString();
            String id = qs.get("id").toString();
            String desc = qs.get("desc").toString();
            String sparql = qs.get("sparql").toString();
            String sparqlPrev = qs.get("sparqlPrev").toString();
            List<PatternParameter> parameters = new ArrayList<PatternParameter>();

            QueryExecution qeNested = queryFactory.createQueryExecution(sparqlSelectParameters.replace("%%PATTERN%%", "<" + patternURI + ">"));
            ResultSet resultsNested = qeNested.execSelect();

            while (resultsNested.hasNext()) {

                QuerySolution parSol = resultsNested.next();

                String parameterURI = parSol.get("parameterURI").toString();
                String parameterID = parSol.get("id").toString();
                String constrainStr = "";
                if (parSol.contains("constrain")) {
                    constrainStr = parSol.get("constrain").toString();
                }
                String constrainPat = "";
                if (parSol.contains("constraintPattern")) {
                    constrainPat = parSol.get("constraintPattern").toString();
                }

                PatternParameterConstraints constrain = PatternParameterConstraints.resolve(constrainStr);
                parameters.add(new PatternParameter(parameterURI, parameterID, constrain, constrainPat));
            }
            qeNested.close();

            // Get annotations from TAG URI
            List<ResultAnnotation> annotations = SparqlUtils.getResultAnnotations(queryFactory, patternURI);

            Pattern pat = new Pattern(id, desc, sparql, sparqlPrev, parameters, annotations);
            if (pat.isValid())
                patterns.add(pat);
            else {
                //TODO logger
                System.err.println("Pattern not valid: " + pat.getId());
                System.exit(-1);
            }
        }
        qe.close();

        return patterns;
    }
}
