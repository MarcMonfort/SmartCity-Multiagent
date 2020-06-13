package org.upc.edu.Behaviours;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;


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

        }
        return calles;
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
