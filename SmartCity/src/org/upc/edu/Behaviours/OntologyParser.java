package org.upc.edu.Behaviours;

import org.apache.jena.base.Sys;
import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.ResultBinding;

import java.util.ArrayList;
import java.util.HashMap;


public class OntologyParser {
    OntModel model;
    String JENAPath;
    String OntologyFile;
    String NamingContext;
    OntDocumentManager dm;

    public OntologyParser(String _JENA_PATH, String _File, String _NamingContext) {
        this.JENAPath= _JENA_PATH;
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

    public HashMap<String, EntornoAgent.Semaforo> getSemaforos() {

        Property tienePosicion  = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tienePosicion");
        Property cierraPasoA  = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#cierraPasoA");
        Property ocurreEn  = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#ocurreEn");


        String queryString = "\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX sc: <http://www.semanticweb.org/sid/smartCity#>\n" +
                "SELECT ?x \n" +
                "WHERE {\n" +
                "    ?x rdf:type sc:AgenteSemaforo.\n" +
                "} LIMIT 100";

        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();
        HashMap<String, EntornoAgent.Semaforo> list_semaforos = new HashMap<>();

        while (results.hasNext()) {
            ResultBinding res = (ResultBinding) results.next();
            String uri = res.get("x").asResource().getURI();

            Individual indiv = model.getIndividual(uri);

            EntornoAgent.Semaforo semaforo = new EntornoAgent.Semaforo();

            semaforo.nombre = indiv.getLocalName();

            int[] pos = EntornoAgent.getCoord(indiv.getPropertyValue(tienePosicion).asLiteral().getString());
            semaforo.pos_x = pos[0];
            semaforo.pos_y = pos[1];

            NodeIterator it = indiv.listPropertyValues(ocurreEn);

            EntornoAgent.Calle calle1 = new EntornoAgent.Calle();
            EntornoAgent.Calle calle2 = new EntornoAgent.Calle();

            calle1.nombre = it.next().asResource().getLocalName();
            calle2.nombre = it.next().asResource().getLocalName();

            semaforo.calle1 = calle1;
            semaforo.calle2 = calle2;

            //semaforos[i].calle1 = it.next().asResource().getLocalName();
            //semaforos[i].calle2 = it.next().asResource().getLocalName();

            semaforo.calleCerrada = indiv.getPropertyValue(cierraPasoA).asResource().getLocalName();

            list_semaforos.put(semaforo.nombre, semaforo);

        }
        qe.close();
        return list_semaforos;


    }

    /* public EntornoAgent.Semaforo getSemaforo(String nombre) {
        Individual indiv;

        String url = "http://www.semanticweb.org/sid/smartCity#" + nombre;
        indiv = model.getIndividual(url);

        Property tienePosicion  = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tienePosicion");
        Property cierraPasoA  = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#cierraPasoA");
        Property ocurreEn  = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#ocurreEn");

        EntornoAgent.Semaforo semaforo = new EntornoAgent.Semaforo();

        semaforo.nombre = indiv.getLocalName();

        int[] pos = EntornoAgent.getCoord(indiv.getPropertyValue(tienePosicion).asLiteral().getString());
        semaforo.pos_x = pos[0];
        semaforo.pos_y = pos[1];

        NodeIterator it = indiv.listPropertyValues(ocurreEn);
        semaforo.calle1 = it.next().asResource().getLocalName();
        semaforo.calle2 = it.next().asResource().getLocalName();

        semaforo.calleCerrada = indiv.getPropertyValue(cierraPasoA).asResource().getLocalName();
        return semaforo;
    } */




    public HashMap<String, EntornoAgent.Calle> getCalles() {

        Property tieneLongitud    = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tieneLongitud");
        Property tienePosicionIni = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tienePosicionIni");
        Property tienePosicionFin = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tienePosicionFin");
        Property tieneDireccion   = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tieneDireccion");

        //NUEVO
        Property tieneSiguiente     = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#tieneSiguiente");
        Property contiene           = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#contiene"); //semaforo
        Property tieneInterseccion  = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#tieneInterseccion");


        String queryString = "\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX sc: <http://www.semanticweb.org/sid/smartCity#>\n" +
                "SELECT ?x \n" +
                "WHERE {\n" +
                "    ?x rdf:type sc:Calle.\n" +
                "} LIMIT 100";

        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();
        HashMap<String, EntornoAgent.Calle> list_calles = new HashMap<>();

        while (results.hasNext()) {
            ResultBinding res = (ResultBinding) results.next();
            String uri = res.get("x").asResource().getURI();

            Individual indiv = model.getIndividual(uri);


            EntornoAgent.Calle calle = new EntornoAgent.Calle();

            int[] coord_ini = EntornoAgent.getCoord(indiv.getPropertyValue(tienePosicionIni).asLiteral().getString());
            int[] coord_fin = EntornoAgent.getCoord(indiv.getPropertyValue(tienePosicionFin).asLiteral().getString());
            int[] direccion = EntornoAgent.getCoord(indiv.getPropertyValue(tieneDireccion).asLiteral().getString());

            calle.nombre = indiv.getLocalName();
            calle.longitud = indiv.getPropertyValue(tieneLongitud).asLiteral().getInt();
            calle.ini_x = coord_ini[0];
            calle.ini_y = coord_ini[1];
            calle.fin_x = coord_fin[0];
            calle.fin_y = coord_fin[1];
            calle.dir_x = direccion[0];
            calle.dir_y = direccion[1];

            //NUEVO
            ArrayList<EntornoAgent.Semaforo> semaforos = new ArrayList<>();
            NodeIterator it = indiv.listPropertyValues(contiene);
            while (it.hasNext()) {
                EntornoAgent.Semaforo semaforo = new EntornoAgent.Semaforo();
                semaforo.nombre = it.next().asResource().getLocalName();
                semaforos.add(semaforo);
            }
            calle.semaforos = semaforos;


            EntornoAgent.Calle siguiente = new EntornoAgent.Calle();
            siguiente.nombre = indiv.getPropertyValue(tieneSiguiente).asResource().getLocalName();
            calle.siguiente = siguiente;


            ArrayList<EntornoAgent.Calle> intersecciones = new ArrayList<>();
            it = indiv.listPropertyValues(tieneInterseccion);
            while (it.hasNext()) {
                EntornoAgent.Calle interseccion = new EntornoAgent.Calle();
                interseccion.nombre = it.next().asResource().getLocalName();
                intersecciones.add(interseccion);
            }
            calle.inter = intersecciones;

            list_calles.put(calle.nombre, calle);

        }

        qe.close();
        return list_calles;
    }


    public HashMap<String, EntornoAgent.Vehiculo> getVehiculos() {

        Property tienePosicion  = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tienePosicion");
        Property tieneObjetivo  = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tieneObjetivo");
        Property tieneVelocidad = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tieneVelocidad");
        Property ocurreEn = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#ocurreEn");

        String queryString = "\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX sc: <http://www.semanticweb.org/sid/smartCity#>\n" +
                "SELECT ?x \n" +
                "WHERE {\n" +
                "    ?x rdf:type sc:AgenteVehiculo.\n" +
                "} LIMIT 100";

        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();
        HashMap<String, EntornoAgent.Vehiculo> list_vehiculos = new HashMap<>();

        while (results.hasNext()) {
            ResultBinding res = (ResultBinding) results.next();
            String uri = res.get("x").asResource().getURI();

            Individual indiv = model.getIndividual(uri);

            EntornoAgent.Vehiculo vehiculo = new EntornoAgent.Vehiculo();

            int[] pos = EntornoAgent.getCoord(indiv.getPropertyValue(tienePosicion).asLiteral().getString());
            int[] obj = EntornoAgent.getCoord(indiv.getPropertyValue(tieneObjetivo).asLiteral().getString());

            vehiculo.nombre = indiv.getLocalName();
            vehiculo.velocidad = indiv.getPropertyValue(tieneVelocidad).asLiteral().getInt();
            vehiculo.pos_x = pos[0];
            vehiculo.pos_y = pos[1];
            vehiculo.obj_x = obj[0];
            vehiculo.obj_y = obj[1];

            EntornoAgent.Calle calleActual = new EntornoAgent.Calle();
            calleActual.nombre = indiv.getPropertyValue(ocurreEn).asResource().getLocalName();
            vehiculo.calleActual = calleActual;
            //vehiculos[i].calle_actual = indiv_vehiculos[i].getPropertyValue(ocurreEn).asResource().getLocalName();

            list_vehiculos.put(vehiculo.nombre, vehiculo);

        }
        qe.close();
        return list_vehiculos;
    }




}
