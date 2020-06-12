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
        public int direccion = 0;
        public int velocidad;
        public String calle_actual;
        public int[] carril_inter = {20,30};

    }

    public static class Semaforo {
        public String nombre;
        public int pos_x;
        public int pos_y;
        public String calle1;
        public String calle2;
        public String calleCerrada;

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

        System.out.println("Load the Ontology");
        parser.loadOntology();
        System.out.println("------------------");

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

        ContainerController cc = getContainerController();
        for (int i = 0; i < vehiculos.length; i++) {
            System.out.println(vehiculos[i].nombre + ":");
            System.out.println("  Calle: " + vehiculos[i].calle_actual);
            System.out.println("  Pos : " + vehiculos[i].pos_x + ", " + vehiculos[i].pos_y);
            System.out.println("  Obj : " + vehiculos[i].obj_x + ", " + vehiculos[i].obj_y);
            System.out.println("  Vel : " + vehiculos[i].velocidad);
            Object[] args = new Object[7];
            //args[0] = vehiculos[i].nombre;
            args[0] = i;
            args[1] = vehiculos[i].calle_actual;
            args[2] = vehiculos[i].pos_x;
            args[3] = vehiculos[i].pos_y;
            args[4] = vehiculos[i].obj_x;
            args[5] = vehiculos[i].obj_y;
            args[6] = vehiculos[i].velocidad;
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

        }
    }

    public class EntornoTickerBehaviour extends TickerBehaviour {

        ACLMessage msg;

        public EntornoTickerBehaviour(Agent a, long period) {
            super(a, period);
        }

        public void onStart() {

        }

        /* public int onEnd() {
            System.out.println("Bye..");
            return 0;
        } */

        public void onTick() {
            //System.out.println("Vehiculo: Posicion, Velocidad, Dirección"); // print vector de info vehiculos
            //System.out.println("Semaforo: Color"); // print vector de info semaforo
            System.out.println(" "); // print vector de info semaforo

            // AQUI SI FUNCIONA CON MULTIPLES AGENTES, ¿¿¿PORQUE???
            /*MessageTemplate mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_QUERY);
            myAgent.addBehaviour(new AchieveREResponder(myAgent, mt) {
                protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                    
                    // 2 posibilities (accident, nextobstacle)
                    String[] contentArray = request.getContent().split(",");
                    int elID = Integer.parseInt(contentArray[1]);

                    Vehiculo aux = new Vehiculo();
                    vehiculos[elID].pos_x = Integer.parseInt(contentArray[2]);
                    vehiculos[elID].pos_y = Integer.parseInt(contentArray[3]);
                    vehiculos[elID].velocidad = Integer.parseInt(contentArray[4]);
                    vehiculos[elID].direccion = Integer.parseInt(contentArray[5]);

                    //info_vehiculos.put(elID, aux);
                    //vehiculos[elID] = aux;


                    ACLMessage informDone  = request.createReply();
                    informDone.setPerformative(ACLMessage.INFORM);
                    informDone.setContent(request.getSender().getName());
                    //System.out.println("recibido " + request.getSender().getName());
                    return informDone;
                }
                protected ACLMessage handleRequest(ACLMessage request) {
                    return null;
            }
            });*/


            //recorremos info_vehiculo para poner el estado actual.
            /*for (String i : info_vehiculos.keySet()) {
                Vehiculo v = info_vehiculos.get(i);
                System.out.println(i + ": " + v.pos_x + "," + v.pos_y + "," + v.velocidad + "," + v.direccion );
            }*/
            for (Vehiculo v : vehiculos){
                System.out.println(v.nombre + "= " + v.pos_x + "," + v.pos_y + "," + v.velocidad + "," + v.direccion );
            }
        }

    }

    public class EntornoCyclicBehaviour extends CyclicBehaviour {

        //MessageTemplate tmpl;

        public EntornoCyclicBehaviour() {
        }

        /* public void onStart() {
            tmpl = MessageTemplate.MatchSender((new AID("Termometro", AID.ISLOCALNAME)));
        }

        public int onEnd() {
            System.out.println("Bye..");
            return 0;
        } */

        public void action() {
            final ACLMessage request = receive(); //usar filtro??

            if (request != null) {
                //if(request.get)
                /* final AID sender = request.getSender();
                final ACLMessage reply = request.createReply(); */
                System.out.println("recibido algo...");
            }
            else {
                block();
            }


            /* try {
                final DFAgentDescription desc = new DFAgentDescription();
                desc.setName(sender);
                final DFAgentDescription[] search = DFService.search(EntornoAgent.this, getDefaultDF(), desc);
                final Iterator services = search[0].getAllServices();
                final ServiceDescription service = (ServiceDescription) services.next();
                String content = request.getContent();
                String[] contentArray = content.split(",");
                //float volumeDischarged = Float.valueOf(contentArray[2]);
                //float concentrationDischarged = Float.valueOf(contentArray[4].replace(")", ""));
                if (true) {
                    // DEBUG
                    //illegalDischargesDetected += 1;
                    //
                    reply.setPerformative(ACLMessage.REQUEST);
                    reply.setContent("papito");
                } else {
                    reply.setPerformative(ACLMessage.INFORM);
                }
                EntornoAgent.this.send(reply);


                /* float currentMassOfPollutant = currentVolume * currentConcentration;  // Mass in g
                float massOfPollutantDischarged = volumeDischarged * concentrationDischarged;  // Mass in g
                float totalMassOfPollutant = currentMassOfPollutant + massOfPollutantDischarged;  // Mass in g
                currentVolume += volumeDischarged;  // Volume in m3
                currentConcentration = totalMassOfPollutant / currentVolume;  // Concentration in g/m3 */

            /*} catch (FIPAException e) {
                e.printStackTrace();
            } */

            /* ACLMessage msg = receive(tmpl);
            if (msg != null) {
                int n = Integer.parseInt(msg.getContent());

                if (n < 15) System.out.println("calefacción encendida (" + n + "°C)");
                else if (n > 25) System.out.println("refrigeración encendida (" + n + "°C)");
                else System.out.println("temperatura adecuada (" + n + "°C)");
            }
            else {
                block();
            } */
            //System.out.println("aaa");
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
                vehiculos[elID].direccion = Integer.parseInt(contentArray[5]);

                //info_vehiculos.put(elID, aux);
                //vehiculos[elID] = aux;


                ACLMessage informDone  = request.createReply();
                informDone.setPerformative(ACLMessage.INFORM);
                informDone.setContent(request.getSender().getName());
                //System.out.println("recibido " + request.getSender().getName());
                return informDone;
            }
            protected ACLMessage handleRequest(ACLMessage request) {
                return null;
            }
        });

        EntornoTickerBehaviour b = new EntornoTickerBehaviour(this, 3000);
        this.addBehaviour(b);

        EntornoCyclicBehaviour b2 = new EntornoCyclicBehaviour();
        this.addBehaviour(b2);
    }
}
