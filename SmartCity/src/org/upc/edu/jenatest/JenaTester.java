/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jenatest;


import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.ResultBinding;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;


/**
 * @author Ignasi Gómez-Sebastià
 */
public class JenaTester {
    OntModel model;
    String JENAPath;
    String OntologyFile;
    String NamingContext;
    OntDocumentManager dm;

    public JenaTester(String _JENA_PATH, String _File, String _NamingContext) {
        this.JENAPath = _JENA_PATH;
        this.OntologyFile = _File;
        this.NamingContext = _NamingContext;
    }


    public void loadOntology() {
        System.out.println("· Loading Ontology");
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
        dm = model.getDocumentManager();
        dm.addAltEntry(NamingContext,
                "file:" + JENAPath + OntologyFile);
        model.read(NamingContext);
    }

    public void releaseOntology() throws FileNotFoundException {
        System.out.println("· Releasing Ontology");
        if (!model.isClosed()) {
            model.write(new FileOutputStream("./PizzaPatatitasNew.owl", true));
            model.close();
        }
    }

    public void getIndividuals() {
        //List of ontology properties
        for (Iterator<Individual> i = model.listIndividuals(); i.hasNext(); ) {
            Individual dummy = i.next();
            System.out.println("Ontology has individual: ");
            System.out.println("   ·Individual: " + dummy);
            Property nameProperty = model.getProperty("<http://www.co-ode.org/ontologies/pizza/pizza.owl#hasPizzaName>");
            RDFNode nameValue = dummy.getPropertyValue(nameProperty);
            System.out.println("   hasPizzaName Property: " + nameValue);
        }
    }

    public void getIndividualsByClass() {
        Iterator<OntClass> classesIt = model.listNamedClasses();
        while (classesIt.hasNext()) {
            OntClass actual = classesIt.next();
            System.out.println("Class: '" + actual.getURI() + "' has individuals:");
            OntClass pizzaClass = model.getOntClass(actual.getURI());
            for (Iterator<Individual> i = model.listIndividuals(pizzaClass); i.hasNext(); ) {
                System.out.println("    · " + i.next());
            }
        }
    }

    public void getPropertiesByClass() {
        Iterator<OntClass> classesIt = model.listNamedClasses();
        while (classesIt.hasNext()) {
            OntClass actual = classesIt.next();
            System.out.println("Class: '" + actual.getURI() + "' has properties:");
            OntClass pizzaClass = model.getOntClass(actual.getURI());
            //List of ontology properties
            Iterator<OntProperty> itProperties = pizzaClass.listDeclaredProperties();

            while (itProperties.hasNext()) {
                OntProperty property = itProperties.next();
                System.out.println("    · Name :" + property.getLocalName());
                System.out.println("        · Domain :" + property.getDomain());
                System.out.println("        · Range :" + property.getRange());
                System.out.println("        · Inverse :" + property.hasInverse());
                System.out.println("        · IsData :" + property.isDatatypeProperty());
                System.out.println("        · IsFunctional :" + property.isFunctionalProperty());
                System.out.println("        · IsObject :" + property.isObjectProperty());
                System.out.println("        · IsSymetric :" + property.isSymmetricProperty());
                System.out.println("        · IsTransitive :" + property.isTransitiveProperty());
            }
        }
    }

    private void addInstances(String classUri, String className) {
        System.out.println("   Adding instance to '" + className + "'");
        OntClass pizzaClass = model.getOntClass(classUri);
        Individual particularPizza = pizzaClass.createIndividual("The " + className + " I am eating right now");
        Property nameProperty = model.getProperty("<http://www.co-ode.org/ontologies/pizza/pizza.owl#hasPizzaName>");
        particularPizza.addProperty(nameProperty, "A yummy" + className);
    }

    public void getClasses() {
        //List of ontology classes
        Iterator<OntClass> classesIt = model.listNamedClasses();

        OntClass pizza = model.getOntClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#Pizza");

        while (classesIt.hasNext()) {
            OntClass actual = classesIt.next();
            System.out.println("Ontology has class: " + actual.getURI());
            if (actual.hasSuperClass(pizza)) {
                addInstances(actual.getURI(), actual.getLocalName());
            }
        }
    }


    public void runSparqlQueryDataProperty() {
        String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "PREFIX pizza: <http://www.co-ode.org/ontologies/pizza/pizza.owl#> "
                + "SELECT ?Pizza ?PizzaName "
                + "where {"
                + " ?Pizza a ?y. "
                + " ?y rdfs:subClassOf pizza:Pizza. "
                + " ?Pizza pizza:hasPizzaName ?PizzaName"
                + "}";

        Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();

        while (results.hasNext()) {
            ResultBinding res = (ResultBinding) results.next();
            Object Pizza = res.get("Pizza");
            Object PizzaName = res.get("PizzaName");
            System.out.println("Pizza = " + Pizza + " <- PizzaName -> " + PizzaName);
        }
        qe.close();
    }


    public void runSparqlQueryObjectProperty() {
        String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "PREFIX pizza: <http://www.co-ode.org/ontologies/pizza/pizza.owl#> "
                + "SELECT ?Pizza ?PizzaBase ?PizzaTopping "
                + "where {?Pizza a ?y. ?y rdfs:subClassOf pizza:Pizza. "
                + "?Pizza pizza:hasBase ?PizzaBase. "
                + "?Pizza pizza:hasTopping ?PizzaTopping. "
                + "?Pizza pizza:hasPizzaName \"MySuperMarioPizza\"}";
        //queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX pizza: <http://www.co-ode.org/ontologies/pizza/pizza.owl#> SELECT ?Pizza ?PizzaBase ?PizzaTopping where {?Pizza a ?y. ?y rdfs:subClassOf pizza:Pizza. ?Pizza pizza:hasBase ?PizzaBase. ?Pizza pizza:hasTopping ?PizzaTopping. ?Pizza pizza:hasPizzaName?PizzaName. FILTER regex(?PizzaName, \"^My\") }";
        //queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX pizza: <http://www.co-ode.org/ontologies/pizza/pizza.owl#> SELECT ?Pizza ?PizzaBase ?PizzaTopping where {?Pizza a ?y. ?y rdfs:subClassOf pizza:Pizza. ?Pizza pizza:hasBase ?PizzaBase. ?Pizza pizza:hasTopping ?PizzaTopping. ?Pizza pizza:hasPrice ?PizzaPrice. FILTER (?PizzaPrice < 20) }";

        Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();

        while (results.hasNext()) {
            ResultBinding res = (ResultBinding) results.next();
            Object Pizza = res.get("Pizza");
            Object PizzaBase = res.get("PizzaBase");
            Object PizzaTopping = res.get("PizzaTopping");
            System.out.println("Pizza = " + Pizza + " <- hasPizzaBase -> " + PizzaBase);
            System.out.println("Pizza = " + Pizza + " <- hasPizzaTopping -> " + PizzaTopping);
        }
        qe.close();
    }

    public void runSparqlQueryModify() {
        String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "PREFIX pizza: <http://www.co-ode.org/ontologies/pizza/pizza.owl#> "
                + "SELECT ?Pizza ?Eaten "
                + "where {?Pizza a ?y. "
                + "?y rdfs:subClassOf pizza:Pizza. "
                + "Optional {?Pizza pizza:Eaten ?Eaten}}";

        Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();

        while (results.hasNext()) {
            ResultBinding res = (ResultBinding) results.next();
            Object Pizza = res.get("Pizza");
            Object Eaten = res.get("Eaten");
            if (Eaten == null) {
                System.out.println("Pizza = " + Pizza + " <-> false");
                Individual actualPizza = model.getIndividual(Pizza.toString());
                Property eatenProperty = model.getProperty("http://www.co-ode.org/ontologies/pizza/pizza.owl#Eaten");
                Literal rdfBoolean = model.createTypedLiteral(Boolean.valueOf("true"));
                actualPizza.addProperty(eatenProperty, rdfBoolean);
            } else {
                System.out.println("Pizza = " + Pizza + " <-> " + Eaten);
            }
        }
        qe.close();
    }
}
