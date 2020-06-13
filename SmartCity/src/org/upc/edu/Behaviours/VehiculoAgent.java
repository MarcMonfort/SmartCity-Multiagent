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


        public void onTick() {
            // mirar si se llega al final del carril
            boolean fin_calle = false;

            miVehiculo.pos_x += miVehiculo.velocidad * calleActual.dir_x;
            miVehiculo.pos_y += miVehiculo.velocidad * calleActual.dir_y;

            //llegado al destino
            if (miVehiculo.pos_x == miVehiculo.obj_x && miVehiculo.pos_y == miVehiculo.obj_y){
                miVehiculo.velocidad = 0;
                System.out.println(miVehiculo.nombre + "  DESTINO!!!");
            }

            //final calle
            if (miVehiculo.pos_x == calleActual.fin_x && miVehiculo.pos_y == calleActual.fin_y){
                System.out.println(miVehiculo.nombre + "  Ha llegado al final de la calle");
                calleActual = calleActual.siguiente;
            }

            // ha llegado a una intersección... decidir si cambiar o no...
            if (miVehiculo.pos_x == calleActual.inter.get(0).ini_x && miVehiculo.pos_y == calleActual.inter.get(0).fin_x){
                System.out.println(miVehiculo.nombre + "  Estoy en una interseccioin");

                Random rand = new Random();
                if (rand.nextBoolean()){
                    calleActual = calleActual.inter.get(0);
                }
            }

            // ha llegado a un semaforo
            if (miVehiculo.pos_x == calleActual.semaforos.get(0).pos_x && miVehiculo.pos_y == calleActual.semaforos.get(0).pos_y){
                //esta en rojo
                if (calleActual.nombre == calleActual.semaforos.get(0).calleCerrada) {
                    System.out.println(miVehiculo.nombre + "  Esperando en semaforo");
                    miVehiculo.velocidad = 0;
                    // solicitar ponerse en verde,,, (messageInitiator)
                    // esperar a que responda y se ponga en verde
                    // (puede que no funcione, y haya que mirar del entorno...)
                }
            }

            

            if (dist_next_obstacle == 0) { // tanto si es otro vehiculo o un semaforo rojo
                System.out.println(miVehiculo.nombre + "  va a chocar...");
                miVehiculo.velocidad = 0;
                // esperas o envias mensaje
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
                        protected void handleInform(ACLMessage inform) {
                            System.out.println(inform.getContent());
                            //double t = Double.parseDouble(inform.getContent());
                            //System.out.println(VehiculoAgent.this.getName() + " == " + inform.getContent()); //
                            
                        }
                    });
                    
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
            }

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
        sdesc.setName("Vehiculo"); //habria que poner nombre vehiculo
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
