/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.upc.edu.jenatest;

//import javafx.util.Pair;
import org.apache.jena.base.Sys;
//import org.omg.CORBA.Environment;
import org.upc.edu.Behaviours.EntornoAgent;
import org.upc.edu.Behaviours.OntologyParser;
import org.upc.edu.Behaviours.VehiculoAgent;

import java.io.FileNotFoundException;


/**
 * @author Ignasi Gómez-Sebastià
 */
public class Main {

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) throws FileNotFoundException {

        String JENA = "./";
        String File = "Ontologia.owl";
        String NamingContext = "http://www.semanticweb.org/sid/smartCity";

        System.out.println("----------------Starting program -------------");

        OntologyParser tester = new OntologyParser(JENA, File, NamingContext);

        System.out.println("Load the Ontology");
        tester.loadOntology();
        System.out.println("------------------");

        System.out.println("INSTANCIAS DE CALLES:");
        EntornoAgent.Calle[] calles = tester.getCalles();

        for (int i = 0; i < calles.length; i++) {
            System.out.println(calles[i].nombre + ":");
            System.out.println("  Longitud: " + calles[i].longitud);
            System.out.println("  Pos ini : " + calles[i].ini_x + ", " + calles[i].ini_y);
            System.out.println("  Pos fin : " + calles[i].fin_x + ", " + calles[i].fin_y);

        }

        System.out.println("INSTANCIAS DE VEHICULOS:");
        EntornoAgent.Vehiculo[] vehiculos = tester.getVehiculos();

        for (int i = 0; i < vehiculos.length; i++) {
            System.out.println(vehiculos[i].nombre + ":");
            System.out.println("  Calle: " + vehiculos[i].calle_actual);
            System.out.println("  Pos : " + vehiculos[i].pos_x + ", " + vehiculos[i].pos_y);
            System.out.println("  Obj : " + vehiculos[i].obj_x + ", " + vehiculos[i].obj_y);
            System.out.println("  Vel : " + vehiculos[i].velocidad);

        }
        //System.out.println("Get the different individuals");
        //tester.getIndividuals();
        //System.out.println("------------------");

        //System.out.println("Grouping individuals by class");
        //tester.getIndividualsByClass();
        //System.out.println("------------------");

        //System.out.println("Grouping properties by class");
        //tester.getPropertiesByClass();
        //System.out.println("------------------");

        //System.out.println("Run a test Data property");
        //tester.runSparqlQueryDataProperty();
        //System.out.println("------------------");

        //System.out.println("Run a test Object property");
        //tester.runSparqlQueryObjectProperty();
        //System.out.println("------------------");

        //System.out.println("Run and modify");
        //tester.runSparqlQueryModify();
        //System.out.println("------------------");

        //System.out.println("Re-Run to check modification");
        //tester.runSparqlQueryModify();
        //System.out.println("------------------");


        //tester.releaseOntology();

        System.out.println("--------- Program terminated --------------------");

    }

}
