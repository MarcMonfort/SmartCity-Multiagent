/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.upc.edu.Behaviours;

import jade.core.AID;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

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
    }

    public static class Vehiculo {
        public String nombre;
        public int pos_x;
        public int pos_y;
        public int obj_x;
        public int obj_y;
        public int direccion = 0;
        public int velocidad;
        public String calle_actual;
        public int[] carril_inter = {20,30};

    };

    public static int[] getCoord(String c) {
        String[] nums = c.split(",");
        int[] coord = new int[2];
        coord[0] = Integer.parseInt(nums[0]);
        coord[1] = Integer.parseInt(nums[1]);
        return coord;
    }

    //private ArrayList<Vehiculo> info_vehiculos;

    private HashMap<Integer, Vehiculo> info_vehiculo = new HashMap<Integer, Vehiculo>();

    public class EntornoTickerBehaviour extends TickerBehaviour {

        ACLMessage msg;

        public EntornoTickerBehaviour(Agent a, long period) {
            super(a, period);
        }

        /* public void onStart() {
            msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("Termostato", AID.ISLOCALNAME));
            msg.setSender(getAID());
        } */

        /* public int onEnd() {
            System.out.println("Bye..");
            return 0;
        } */

        public void onTick() {
            System.out.println("Vehiculo: Posicion, Velocidad, Dirección"); // print vector de info vehiculos
            System.out.println("Semaforo: Color"); // print vector de info semaforo

            // AQUI SI FUNCIONA CON MULTIPLES AGENTES, ¿¿¿PORQUE???
            MessageTemplate mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_QUERY);
            myAgent.addBehaviour(new AchieveREResponder(myAgent, mt) {
                protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                    
                    // 2 posibilities (accident, nextobstacle)
                    String[] contentArray = request.getContent().split(",");
                    int elID = Integer.parseInt(contentArray[0]);

                    Vehiculo aux = new Vehiculo();
                    aux.pos_x = Integer.parseInt(contentArray[1]);
                    aux.pos_y = Integer.parseInt(contentArray[2]);
                    aux.velocidad = Integer.parseInt(contentArray[3]);
                    aux.direccion = Integer.parseInt(contentArray[4]);

                    info_vehiculo.put(elID, aux);

                    ACLMessage informDone  = request.createReply();
                    informDone.setPerformative(ACLMessage.INFORM);
                    informDone.setContent(request.getSender().getName());
                    System.out.println("recibido " + request.getSender().getName());
                    return informDone;
                }
                protected ACLMessage handleRequest(ACLMessage request) {
                    return null;
            }
            });


            //recorremos info_vehiculo para poner el estado actual.
            for (Integer i : info_vehiculo.keySet()) {
                Vehiculo v = info_vehiculo.get(i);
                System.out.println(i + ": " + v.pos_x + "," + v.pos_y + "," + v.velocidad + "," + v.direccion );
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


        /* MessageTemplate mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_QUERY);
        this.addBehaviour(new AchieveREResponder(this, mt) {
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {

                ACLMessage informDone  = request.createReply();
                informDone.setPerformative(ACLMessage.INFORM);
                informDone.setContent(request.getSender().getName());
                System.out.println("recibido " + request.getSender().getName());
                return informDone;
            }
            protected ACLMessage handleRequest(ACLMessage request) {
                return null;
            }
        }); */

        EntornoTickerBehaviour b = new EntornoTickerBehaviour(this, 3000);
        this.addBehaviour(b);

        EntornoCyclicBehaviour b2 = new EntornoCyclicBehaviour();
        this.addBehaviour(b2);
    }
}
