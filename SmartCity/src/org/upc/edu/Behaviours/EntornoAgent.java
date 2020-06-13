/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.upc.edu.Behaviours;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.tools.sniffer.Sniffer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.HashMap;

/**
 * @author igomez
 */
public class EntornoAgent extends Agent {

    public static class Calle {
        public String nombre;
        public int longitud;
        public int ini_x;
        public int ini_y;
        public int fin_x;
        public int fin_y;
        public int dir_x;
        public int dir_y;
    }

    public static class Vehiculo {
        public int ID;
        public String nombre;
        public int pos_x;
        public int pos_y;
        public int obj_x;
        public int obj_y;
        public int velocidad;
        public String calle_actual;
    }

    public static class Semaforo {
        public String nombre;
        public int pos_x;
        public int pos_y;
        public String calle1;
        public String calle2;
        public String calleCerrada; //calle1 o calle2

    }

    public static int[] getCoord(String c) {
        String[] nums = c.split(",");
        int[] coord = new int[2];
        coord[0] = Integer.parseInt(nums[0]);
        coord[1] = Integer.parseInt(nums[1]);
        return coord;
    }

    //private ArrayList<Vehiculo> info_vehiculos;

    private HashMap<String, Vehiculo> info_vehiculos = new HashMap<>();
    private Calle[] calles;
    private Vehiculo[] vehiculos;
    private Semaforo[] semaforos;
    
    private void inicializarEntorno() throws StaleProxyException {
        String JENA = "./";
        String File = "Ontologia.owl";
        String NamingContext = "http://www.semanticweb.org/sid/smartCity";

        System.out.println("----------------Starting program -------------");

        OntologyParser parser = new OntologyParser(JENA, File, NamingContext);

        parser.loadOntology();
        System.out.println("Ontology loaded");

        ContainerController cc = getContainerController();
        AgentController sniffer = cc.createNewAgent("Sniffer", Sniffer.class.getName(), null);
        sniffer.start();
        System.out.println("Sniffer started");

        System.out.println("INSTANCIAS DE CALLES:");
        calles = parser.getCalles();

        for (int i = 0; i < calles.length; i++) {
            System.out.println(calles[i].nombre + ":");
            System.out.println("  Longitud: " + calles[i].longitud);
            System.out.println("  Pos ini : " + calles[i].ini_x + ", " + calles[i].ini_y);
            System.out.println("  Pos fin : " + calles[i].fin_x + ", " + calles[i].fin_y);

        }

        System.out.println("INSTANCIAS DE VEHICULOS:");
        vehiculos = parser.getVehiculos();

        for (int i = 0; i < vehiculos.length; i++) {
            System.out.println(vehiculos[i].nombre + ":");
            System.out.println("  Calle: " + vehiculos[i].calle_actual);
            System.out.println("  Pos : " + vehiculos[i].pos_x + ", " + vehiculos[i].pos_y);
            System.out.println("  Obj : " + vehiculos[i].obj_x + ", " + vehiculos[i].obj_y);
            System.out.println("  Vel : " + vehiculos[i].velocidad);
            Object[] args = new Object[3];

            String nombre_calle = vehiculos[i].calle_actual;
            int id_calle = Integer.parseInt(nombre_calle.substring(nombre_calle.length() - 1)) - 1;
            args[0] = i; //id vehiculo... se podria obtener deel nombre...
            args[1] = vehiculos[i];
            args[2] = calles[id_calle];

            AgentController ac = cc.createNewAgent(vehiculos[i].nombre, "org.upc.edu.Behaviours.VehiculoAgent", args);
            ac.start();
        }

        System.out.println("INSTANCIAS DE SEMAFOROS:");
        semaforos = parser.getSemaforos();

        for (int i = 0; i < semaforos.length; i++) {
            System.out.println(semaforos[i].nombre + ":");
            System.out.println("  Calle1 : " + semaforos[i].calle1);
            System.out.println("  Calle2 : " + semaforos[i].calle2);

            System.out.println("  Pos : " + semaforos[i].pos_x + ", " + semaforos[i].pos_y);
            System.out.println("  CalleCerrada : " + semaforos[i].calleCerrada);
            Object[] args = new Object[4];

            String nombre_calle1 = semaforos[i].calle1;
            String nombre_calle2 = semaforos[i].calle2;

            int id_calle1 = Integer.parseInt(nombre_calle1.substring(nombre_calle1.length() - 1)) - 1;
            int id_calle2 = Integer.parseInt(nombre_calle2.substring(nombre_calle2.length() - 1)) - 1;

            args[0] = i; //id semaforo... se podria obtener deel nombre...
            args[1] = semaforos[i];
            args[2] = calles[id_calle1];
            args[3] = calles[id_calle2];

            AgentController ac = cc.createNewAgent(semaforos[i].nombre, "org.upc.edu.Behaviours.SemaforoAgent", args);
            ac.start();
        }
    }

    public class EntornoTickerBehaviour extends TickerBehaviour {

        ACLMessage msg;

        public EntornoTickerBehaviour(Agent a, long period) {
            super(a, period);
        }

        public void onTick() {

            System.out.println(" "); // print vector de info 
            for (Vehiculo v : vehiculos){
                System.out.println(v.nombre + "= " + v.pos_x + "," + v.pos_y + "," + v.velocidad);
            }
        }

    }

    

    protected void setup() {

        try {
            inicializarEntorno();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        // REGISTRO DF
        final DFAgentDescription desc = new DFAgentDescription();
        desc.setName(getAID());

        final ServiceDescription sdesc = new ServiceDescription();
        sdesc.setName("Entorno");
        sdesc.setType("Entorno");
        desc.addServices(sdesc);

        try {
            DFService.register(this, getDefaultDF(), desc);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        // FIN REGISTRO DF


        MessageTemplate mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_QUERY);
        this.addBehaviour(new AchieveREResponder(this, mt) {
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {

                // 2 posibilities (accident, nextobstacle)
                String[] contentArray = request.getContent().split(",");
                int elID = Integer.parseInt(contentArray[1]);

                vehiculos[elID].pos_x = Integer.parseInt(contentArray[2]);
                vehiculos[elID].pos_y = Integer.parseInt(contentArray[3]);
                vehiculos[elID].velocidad = Integer.parseInt(contentArray[4]);

                ACLMessage informDone  = request.createReply();
                informDone.setPerformative(ACLMessage.INFORM);
                informDone.setContent("mensaje para ..." + request.getSender().getName());
                return informDone;
            }
            protected ACLMessage handleRequest(ACLMessage request) {
                return null;
            }
        });

        EntornoTickerBehaviour b = new EntornoTickerBehaviour(this, 3000);
        this.addBehaviour(b);

    }
}
