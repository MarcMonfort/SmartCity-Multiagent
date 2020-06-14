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
import jade.lang.acl.MessageTemplate;
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

    boolean semaforoRojoConsultado; // sirve para no consultar otra vez mientras espera a que se ponga verde


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
                System.out.println(miVehiculo.nombre + ": DESTINO!!!");
                //hacer un pause(3 segundos)...
                //miVehiculo.obj_x = -1; //poner randoms...
                //miVehiculo.obj_y = -1; 
                Random rand = new Random();
                miVehiculo.obj_x = rand.nextInt(3) * 2;
                miVehiculo.obj_y = rand.nextInt(3) * 2;

                System.out.println(miVehiculo.nombre + ": Nuevo Objetivo (" + miVehiculo.obj_x +","+miVehiculo.obj_y+")");



                miVehiculo.velocidad = 1;


            }

            //final calle
            if (miVehiculo.pos_x == calleActual.fin_x && miVehiculo.pos_y == calleActual.fin_y){
                System.out.println(miVehiculo.nombre + "  Ha llegado al final de la calle");
                calleActual = calleActual.siguiente;
            }

            // ha llegado a una intersección... decidir si cambiar o no...
            for (EntornoAgent.Calle calle_inter : calleActual.inter) {
                if (((miVehiculo.pos_x - calle_inter.ini_x) * calleActual.dir_x + (miVehiculo.pos_y - calle_inter.ini_y) * calleActual.dir_y)==0) {
                    System.out.println(miVehiculo.nombre + "  Estoy en una interseccioin");

                    Random rand = new Random();
                    boolean cambiar = rand.nextBoolean();
                    if (true) {
                        calleActual = calle_inter;
                    }
                }
            }

            // ha llegado a un semaforo
            for (EntornoAgent.Semaforo semaforo : calleActual.semaforos) {
                if ((miVehiculo.pos_x+calleActual.dir_x) == semaforo.pos_x && (miVehiculo.pos_y+calleActual.dir_y) == semaforo.pos_y){
                    //esta en rojo
                    System.out.println("semaforo.calleCerrada = " + semaforo.calleCerrada);
                    System.out.println("calleActual.nombre = " + calleActual.nombre);

                    if (calleActual.nombre.equals(semaforo.calleCerrada) && !semaforoRojoConsultado) {
                        System.out.println(miVehiculo.nombre + "  Esperando en : " + semaforo.nombre);
                        miVehiculo.velocidad = 0;
                        // solicitar ponerse en verde,,, (messageInitiator)
                        // esperar a que responda y se ponga en verde
                        // (puede que no funcione, y haya que mirar del entorno...)

                        DFAgentDescription search_template;
                        ServiceDescription sd = new ServiceDescription();
                        sd.setName(semaforo.nombre);
                        search_template = new DFAgentDescription();
                        search_template.addServices(sd);

                        DFAgentDescription[] search_results = new DFAgentDescription[0];
                        AID semaforoAID = null;
                        try {
                            search_results = DFService.search(myAgent, search_template);
                        } catch (FIPAException ex) {
                            System.out.println("Agente " + myAgent.getLocalName() + ": Error al buscar al semaforo");
                        }
                        if (search_results.length > 0) {
                            semaforoAID = search_results[0].getName();
                            System.out.println(myAgent.getLocalName() + " : " + semaforoAID.getLocalName() + " encontrado");
                        }


                        ACLMessage requestSemaforo = new ACLMessage(ACLMessage.REQUEST);
                        requestSemaforo.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                        requestSemaforo.addReceiver(semaforoAID);
                        requestSemaforo.setSender(myAgent.getAID());

                        myAgent.addBehaviour(new AchieveREInitiator(myAgent, requestSemaforo) {
                            protected void handleInform(ACLMessage inform) {
                                String respuesta = inform.getContent();
                                System.out.println("alo???");
                            }
                        });
                        semaforoRojoConsultado = true;
                        System.out.println("ESPERANDO RESPUESTA DEL SEMAFORO");

                    } else {
                        miVehiculo.velocidad = 1;
                        semaforoRojoConsultado = false;
                        System.out.println("Vehiculo ACTIVADOOOO");
                    }

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
                            //System.out.println(inform.getContent());
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
        sdesc.setName(miVehiculo.nombre);
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
