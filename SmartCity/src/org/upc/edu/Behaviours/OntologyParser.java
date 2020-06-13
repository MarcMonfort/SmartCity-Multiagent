package org.upc.edu.Behaviours;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;

import java.util.ArrayList;


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

    public EntornoAgent.Semaforo[] getSemaforos() {
        Individual[] indiv_semaforos = new Individual[3];
        indiv_semaforos[0] = model.getIndividual("http://www.semanticweb.org/sid/smartCity#Semaforo1");
        indiv_semaforos[1] = model.getIndividual("http://www.semanticweb.org/sid/smartCity#Semaforo2");
        indiv_semaforos[2] = model.getIndividual("http://www.semanticweb.org/sid/smartCity#Semaforo3");

        Property tienePosicion  = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tienePosicion");
        Property cierraPasoA  = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#cierraPasoA");
        Property ocurreEn  = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#ocurreEn");

        EntornoAgent.Semaforo[] semaforos = new EntornoAgent.Semaforo[3];
        for (int i = 0; i < 3; i++) {
            semaforos[i] = new EntornoAgent.Semaforo();

            semaforos[i].nombre = indiv_semaforos[i].getLocalName();

            int[] pos = EntornoAgent.getCoord(indiv_semaforos[i].getPropertyValue(tienePosicion).asLiteral().getString());
            semaforos[i].pos_x = pos[0];
            semaforos[i].pos_y = pos[1];

            NodeIterator it = indiv_semaforos[i].listPropertyValues(ocurreEn);
            semaforos[i].calle1 = it.next().asResource().getLocalName();
            semaforos[i].calle2 = it.next().asResource().getLocalName();

            semaforos[i].calleCerrada = indiv_semaforos[i].getPropertyValue(cierraPasoA).asResource().getLocalName();
        }
        return semaforos;
    }

    public EntornoAgent.Semaforo getSemaforo(String nombre) {
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
    }




    public EntornoAgent.Calle[] getCalles() {
        Individual[] indiv_calles = new Individual[6];
        indiv_calles[0] = model.getIndividual("http://www.semanticweb.org/sid/smartCity#Calle1");
        indiv_calles[1] = model.getIndividual("http://www.semanticweb.org/sid/smartCity#Calle2");
        indiv_calles[2] = model.getIndividual("http://www.semanticweb.org/sid/smartCity#Calle3");
        indiv_calles[3] = model.getIndividual("http://www.semanticweb.org/sid/smartCity#Calle4");
        indiv_calles[4] = model.getIndividual("http://www.semanticweb.org/sid/smartCity#Calle5");
        indiv_calles[5] = model.getIndividual("http://www.semanticweb.org/sid/smartCity#Calle6");

        Property tieneLongitud    = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tieneLongitud");
        Property tienePosicionIni = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tienePosicionIni");
        Property tienePosicionFin = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tienePosicionFin");
        Property tieneDireccion   = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tieneDireccion");

        //NUEVO
        Property tieneSiguiente     = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#tieneSiguiente");
        Property contiene           = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#contiene"); //semaforo
        Property tieneInterseccion  = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#tieneInterseccion");


        EntornoAgent.Calle[] calles = new EntornoAgent.Calle[6];
        for (int i = 0; i < 6; i++) {
            int[] coord_ini = EntornoAgent.getCoord(indiv_calles[i].getPropertyValue(tienePosicionIni).asLiteral().getString());
            int[] coord_fin = EntornoAgent.getCoord(indiv_calles[i].getPropertyValue(tienePosicionFin).asLiteral().getString());
            int[] direccion = EntornoAgent.getCoord(indiv_calles[i].getPropertyValue(tieneDireccion).asLiteral().getString());

            calles[i] = new EntornoAgent.Calle();
            calles[i].nombre = indiv_calles[i].getLocalName();
            calles[i].longitud = indiv_calles[i].getPropertyValue(tieneLongitud).asLiteral().getInt();
            calles[i].ini_x = coord_ini[0];
            calles[i].ini_y = coord_ini[1];
            calles[i].fin_x = coord_fin[0];
            calles[i].fin_y = coord_fin[1];
            calles[i].dir_x = direccion[0];
            calles[i].dir_y = direccion[1];

            //NUEVO
            ArrayList<EntornoAgent.Semaforo> semaforos = new ArrayList<>();
            NodeIterator it = indiv_calles[i].listPropertyValues(contiene);
            while (it.hasNext()) {
                //it.asResource().getLocalName(); // nombre calle
                EntornoAgent.Semaforo semaforo = getSemaforo(it.next().asResource().getLocalName());
                semaforos.add(semaforo);
            }
            calles[i].semaforos = semaforos; //array list no array


            String nombreSiguiente = indiv_calles[i].getPropertyValue(tieneSiguiente).asResource().getLocalName();
            calles[i].siguiente = getCalle(nombreSiguiente);


            ArrayList<EntornoAgent.Calle> intersecciones = new ArrayList<>();
            it = indiv_calles[i].listPropertyValues(tieneInterseccion);
            while (it.hasNext()) {
                //it.asResource().getLocalName(); // nombre calle
                EntornoAgent.Calle calle_inter = getCalle(it.next().asResource().getLocalName());
                intersecciones.add(calle_inter);
            }
            calles[i].inter = intersecciones; //array list no array

        }
        return calles;
    }


    public EntornoAgent.Calle getCalle(String name_calle) {
        EntornoAgent.Calle calle;
        Individual indiv;
        String url = "http://www.semanticweb.org/sid/smartCity#" + name_calle;
        indiv = model.getIndividual(url);

        Property tieneLongitud    = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tieneLongitud");
        Property tienePosicionIni = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tienePosicionIni");
        Property tienePosicionFin = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tienePosicionFin");
        Property tieneDireccion   = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tieneDireccion");

        int[] coord_ini = EntornoAgent.getCoord(indiv.getPropertyValue(tienePosicionIni).asLiteral().getString());
        int[] coord_fin = EntornoAgent.getCoord(indiv.getPropertyValue(tienePosicionFin).asLiteral().getString());
        int[] direccion = EntornoAgent.getCoord(indiv.getPropertyValue(tieneDireccion).asLiteral().getString());

        calle = new EntornoAgent.Calle();
        calle.nombre = indiv.getLocalName();
        calle.longitud = indiv.getPropertyValue(tieneLongitud).asLiteral().getInt();
        calle.ini_x = coord_ini[0];
        calle.ini_y = coord_ini[1];
        calle.fin_x = coord_fin[0];
        calle.fin_y = coord_fin[1];
        calle.dir_x = direccion[0];
        calle.dir_y = direccion[1];


        return calle;
    }



    public EntornoAgent.Vehiculo[] getVehiculos() {
        Individual[] indiv_vehiculos = new Individual[2];
        indiv_vehiculos[0] = model.getIndividual("http://www.semanticweb.org/sid/smartCity#Vehiculo1");
        indiv_vehiculos[1] = model.getIndividual("http://www.semanticweb.org/sid/smartCity#Vehiculo2");

        Property tienePosicion  = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tienePosicion");
        Property tieneObjetivo  = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tieneObjetivo");
        Property tieneVelocidad = model.getDatatypeProperty("http://www.semanticweb.org/sid/smartCity#tieneVelocidad");
        Property ocurreEn = model.getObjectProperty("http://www.semanticweb.org/sid/smartCity#ocurreEn");

        EntornoAgent.Vehiculo[] vehiculos = new EntornoAgent.Vehiculo[2];
        for (int i = 0; i < 2; i++) {
            vehiculos[i] = new EntornoAgent.Vehiculo();

            int[] pos = EntornoAgent.getCoord(indiv_vehiculos[i].getPropertyValue(tienePosicion).asLiteral().getString());
            int[] obj = EntornoAgent.getCoord(indiv_vehiculos[i].getPropertyValue(tieneObjetivo).asLiteral().getString());

            vehiculos[i].nombre = indiv_vehiculos[i].getLocalName();
            vehiculos[i].calle_actual = indiv_vehiculos[i].getPropertyValue(ocurreEn).asResource().getLocalName();
            vehiculos[i].velocidad = indiv_vehiculos[i].getPropertyValue(tieneVelocidad).asLiteral().getInt();
            vehiculos[i].pos_x = pos[0];
            vehiculos[i].pos_y = pos[1];
            vehiculos[i].obj_x = obj[0];
            vehiculos[i].obj_y = obj[1];
        }
        return vehiculos;
    }




}
