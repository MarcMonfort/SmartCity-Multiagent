/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

import java.util.Random;

/**
 * @author igomez
 */
public class VehiculoAgent extends Agent {


    String vID;

    int dist_next_obstacle;

    EntornoAgent.Vehiculo miVehiculo;
    EntornoAgent.Calle calleActual; // voy a suponer que sabe en que calle esta
    int myID;



    public class VehiculoTickerBehaviour extends TickerBehaviour {

        //ACLMessage msg;

        public VehiculoTickerBehaviour(Agent a, long period) {
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
            // mirar si se llega al final del carril
            boolean fin_calle = false;

            miVehiculo.pos_x += miVehiculo.velocidad * calleActual.dir_x;
            miVehiculo.pos_y += miVehiculo.velocidad * calleActual.dir_y;

            if (miVehiculo.pos_x == calleActual.fin_x && miVehiculo.pos_y == calleActual.fin_y){
                miVehiculo.velocidad = 0;
                System.out.println("Ha llegado al final de la calle");
                // preguntar siguientes calles
            }


            if (dist_next_obstacle == 0) { // tanto si es otro vehiculo o un semaforo rojo
                System.out.println("va a chocar...");
                miVehiculo.velocidad = 0;

                /*final DFAgentDescription desc = new DFAgentDescription();
                final ServiceDescription sdesc = new ServiceDescription();
                sdesc.setType("Entorno");
                desc.addServices(sdesc);
                try {
                    final DFAgentDescription[] environments = DFService.search(VehiculoAgent.this, getDefaultDF(), desc, new SearchConstraints());
                    final AID environment = environments[0].getName();
                    final ACLMessage aclMessage = new ACLMessage(ACLMessage.QUERY_IF);

                    aclMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
                    aclMessage.setSender(VehiculoAgent.this.getAID());
                    aclMessage.addReceiver(environment);
                    aclMessage.setContent("checkCar," + myID + "," + miVehiculo.pos_x + "," + miVehiculo.pos_y + "," + miVehiculo.velocidad + "," + miVehiculo.direccion);

                    myAgent.addBehaviour(new AchieveREInitiator(myAgent, aclMessage) {
                        //@override???
                        protected void handleInform(ACLMessage inform) {
                            //double t = Double.parseDouble(inform.getContent());
                            //System.out.println(VehiculoAgent.this.getName() + " == " + inform.getContent()); //

                        }
                    });


                    //VehiculoAgent.this.send(aclMessage);

                    //System.out.println("ha superado el send!");
                    //iter = 4;
                } catch (FIPAException e) {
                    e.printStackTrace();
                }*/
            }
            else {
                --dist_next_obstacle; // reducimos distancia al objeto mas cercano
            }

            

            // Establece posicion y recibe next obstacle en cada tick
            if (true) {
                final DFAgentDescription desc = new DFAgentDescription();
                final ServiceDescription sdesc = new ServiceDescription();
                sdesc.setType("Entorno");
                desc.addServices(sdesc);
                try {
                    final DFAgentDescription[] environments = DFService.search(VehiculoAgent.this, getDefaultDF(), desc, new SearchConstraints());
                    final AID environment = environments[0].getName();
                    final ACLMessage aclMessage = new ACLMessage(ACLMessage.QUERY_IF);

                    aclMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
                    aclMessage.setSender(VehiculoAgent.this.getAID());
                    aclMessage.addReceiver(environment);
                    aclMessage.setContent("setCar," + myID + "," + miVehiculo.pos_x + "," + miVehiculo.pos_y + "," + miVehiculo.velocidad);

                    myAgent.addBehaviour(new AchieveREInitiator(myAgent, aclMessage) {
                        //@override???
                        protected void handleInform(ACLMessage inform) {
                            System.out.println(inform.getContent());
                            //double t = Double.parseDouble(inform.getContent());
                            //System.out.println(VehiculoAgent.this.getName() + " == " + inform.getContent()); //
                            
                        }
                    });
                    
                    
                    //VehiculoAgent.this.send(aclMessage);
                    
                    //System.out.println("ha superado el send!");
                    //iter = 4;
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
            }
            //--iter;


            //velocidad += aceleracion;
            //System.out.println("Vehiculo: Posicion=("+pos_x+", "+pos_y+"), Velocidad="+velocidad+", Dirección="+direccion);


            //System.out.println("hola");
            /* if (true) { //request avanzar
                final DFAgentDescription desc = new DFAgentDescription();
                final ServiceDescription sdesc = new ServiceDescription();
                sdesc.setType("Entorno");
                desc.addServices(sdesc);
                try {
                    final DFAgentDescription[] environments = DFService.search(VehiculoAgent.this, getDefaultDF(), desc, new SearchConstraints());
                    final AID environment = environments[0].getName();
                    final ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
                    aclMessage.setSender(VehiculoAgent.this.getAID());
                    aclMessage.addReceiver(environment);
                    aclMessage.setContent(pos_x + "," + pos_y + "," + velocidad + "," + direccion);
                    VehiculoAgent.this.send(aclMessage);
                    
                    System.out.println("ha superado el send!");
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
            } */



        }

    }

    protected void setup() {

        Object[] args = getArguments();
        myID = (Integer) args[0];

        miVehiculo = (EntornoAgent.Vehiculo) args[1];
        calleActual = (EntornoAgent.Calle) args[2];

        miVehiculo.velocidad = 1;

        
        dist_next_obstacle = -1;



        // REGISTRO DF
        final DFAgentDescription desc = new DFAgentDescription();
        desc.setName(getAID());

        final ServiceDescription sdesc = new ServiceDescription();
        sdesc.setName("Vehiculo");
        sdesc.setType("Vehiculo");
        desc.addServices(sdesc);

        try {
            DFService.register(this, getDefaultDF(), desc);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        // FIN REGISTRO DF
        System.out.println("agente : " + this.getLocalName() + " registrado!!!");

        VehiculoTickerBehaviour b = new VehiculoTickerBehaviour(this, 3000);
        this.addBehaviour(b);
    }
}
