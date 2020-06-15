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

    int distVehiculoCarcano;

    EntornoAgent.Vehiculo miVehiculo;
    //EntornoAgent.Calle miVehiculo.calleActual; // voy a suponer que sabe en que calle esta
    int myID;

    boolean semaforoRojoConsultado; // sirve para no consultar otra vez mientras espera a que se ponga verde


    public class VehiculoTickerBehaviour extends TickerBehaviour {

        //ACLMessage msg;

        public VehiculoTickerBehaviour(Agent a, long period) {
            super(a, period);
        }


        public void onTick() {

            /* miVehiculo.pos_x += miVehiculo.velocidad * miVehiculo.calleActual.dir_x;
            miVehiculo.pos_y += miVehiculo.velocidad * miVehiculo.calleActual.dir_y; */

            //llegado al destino
            if (miVehiculo.pos_x == miVehiculo.obj_x && miVehiculo.pos_y == miVehiculo.obj_y){
                //miVehiculo.velocidad = 0;
                //System.out.println(miVehiculo.nombre + ": DESTINO!!!");
                //hacer un pause(3 segundos)...
                // nuevo objetivo
                Random rand = new Random();
                miVehiculo.obj_x = rand.nextInt(3) * 5;
                miVehiculo.obj_y = rand.nextInt(3) * 5;

                //System.out.println(miVehiculo.nombre + ": Nuevo Objetivo (" + miVehiculo.obj_x +","+miVehiculo.obj_y+")");

                //miVehiculo.velocidad = 1;
            }


            


            //final calle
            if (miVehiculo.pos_x == miVehiculo.calleActual.fin_x && miVehiculo.pos_y == miVehiculo.calleActual.fin_y){
                //System.out.println(miVehiculo.nombre + "  Ha llegado al final de la calle");

                miVehiculo.calleActual.vehiculos.remove(miVehiculo.nombre);
                miVehiculo.calleActual = miVehiculo.calleActual.siguiente;
                miVehiculo.calleActual.vehiculos.put(miVehiculo.nombre, miVehiculo);

            }

            // ha llegado a una intersección... decidir si cambiar o no... si su destino esta en la nueva calle, cambia a esa calle!
            for (EntornoAgent.Calle calle_inter : miVehiculo.calleActual.inter) {
                if (((miVehiculo.pos_x - calle_inter.ini_x) * miVehiculo.calleActual.dir_x + (miVehiculo.pos_y - calle_inter.ini_y) * miVehiculo.calleActual.dir_y)==0) {
                    //System.out.println(miVehiculo.nombre + "  Estoy en una interseccioin");


                    boolean cambiar = (calle_inter.ini_x - miVehiculo.obj_x) * calle_inter.dir_y + (calle_inter.ini_y - miVehiculo.obj_y) * calle_inter.dir_x == 0;
                    
                    if (!cambiar){
                        Random rand = new Random();
                        //cambiar = rand.nextBoolean();
                        //cambiar = rand.nextInt(3);
                        int r = rand.nextInt(3);
                        if (r == 0) {
                            cambiar = true;
                        }
                    }

                    if (cambiar) {
                        miVehiculo.calleActual.vehiculos.remove(miVehiculo.nombre);
                        miVehiculo.calleActual = calle_inter;
                        miVehiculo.calleActual.vehiculos.put(miVehiculo.nombre, miVehiculo);
                    }
                }
            }


            // busca vehiculo mas cercano
            distVehiculoCarcano = miVehiculo.calleActual.longitud;
            try {
                for (EntornoAgent.Vehiculo v : miVehiculo.calleActual.vehiculos.values()) {
                    if (v != miVehiculo) {
                        //siempre misma calle
                        int dist = (v.pos_x - miVehiculo.pos_x) + (v.pos_y - miVehiculo.pos_y);
                        dist = dist * (miVehiculo.calleActual.dir_x + miVehiculo.calleActual.dir_y);
                        if ( dist >= 0 && dist < distVehiculoCarcano ){
                            distVehiculoCarcano = dist;
                        }
                    }
                }
                for (EntornoAgent.Vehiculo v : miVehiculo.calleActual.siguiente.vehiculos.values()) {
                    if (v.pos_x == miVehiculo.calleActual.fin_x && v.pos_y == miVehiculo.calleActual.fin_y){
                        int dist = (v.pos_x - miVehiculo.pos_x) + (v.pos_y - miVehiculo.pos_y);
                        dist = dist * (miVehiculo.calleActual.dir_x + miVehiculo.calleActual.dir_y);
                        //System.out.println(miVehiculo.nombre + "         *****    " + dist);
                        //distVehiculoCarcano = dist;
                        if ( dist >= 0 && dist < distVehiculoCarcano ){
                            distVehiculoCarcano = dist;
                        } 
                    }
                }

                
                for (EntornoAgent.Calle calle_inter : miVehiculo.calleActual.inter) {
                    for (EntornoAgent.Vehiculo v : calle_inter.vehiculos.values()) {

                        if ( v.pos_x == miVehiculo.pos_x+miVehiculo.calleActual.dir_x && v.pos_y == miVehiculo.pos_y+miVehiculo.calleActual.dir_y  ){
                            distVehiculoCarcano = 1;
                        }

                    }
                }               

            }
            catch(Exception e){
                distVehiculoCarcano = 1;
                //System.out.println(" 0000000000000000000000111111111111111111    2222222222222222222222222222222222");
            }
            if (distVehiculoCarcano == 1) { // tanto si es otro vehiculo
                //System.out.println(miVehiculo.nombre + "  va a chocar...");
                miVehiculo.velocidad = 0;
                // esperas o envias mensaje
            }
            else if (distVehiculoCarcano == 0) {
                System.out.println(miVehiculo.nombre + "  ACCIDENTE!!!");
                miVehiculo.velocidad = 0;
                Random rand = new Random();
                int reparar = rand.nextInt(7);
                /* if (reparar == 0) {
                    miVehiculo.velocidad = 1;
                } */
            }
            else {
                miVehiculo.velocidad = 1;
            }


            // ha llegado a un semaforo
            for (EntornoAgent.Semaforo semaforo : miVehiculo.calleActual.semaforos) {
                if ((miVehiculo.pos_x+miVehiculo.calleActual.dir_x) == semaforo.pos_x && (miVehiculo.pos_y+miVehiculo.calleActual.dir_y) == semaforo.pos_y){
                    //ambar
                    if(semaforo.pausa) {
                        miVehiculo.velocidad = 0;
                    }
                    //rojo
                    else if (miVehiculo.calleActual.nombre.equals(semaforo.calleCerrada)) {
                        miVehiculo.velocidad = 0;
                        if (!semaforoRojoConsultado) {
                            //System.out.println(miVehiculo.nombre + "  Esperando en : " + semaforo.nombre);

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
                                //System.out.println(myAgent.getLocalName() + " : " + semaforoAID.getLocalName() + " encontrado");
                            }


                            ACLMessage requestSemaforo = new ACLMessage(ACLMessage.REQUEST);
                            requestSemaforo.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                            requestSemaforo.addReceiver(semaforoAID);
                            requestSemaforo.setSender(myAgent.getAID());

                            myAgent.addBehaviour(new AchieveREInitiator(myAgent, requestSemaforo) {
                                protected void handleInform(ACLMessage inform) {
                                    String respuesta = inform.getContent();
                                }
                            });
                            semaforoRojoConsultado = true;
                            //System.out.println(myAgent.getLocalName() + ": ESPERANDO RESPUESTA DEL SEMAFORO");
                        }

                    } 
                    // verde 
                    else if (!miVehiculo.calleActual.nombre.equals(semaforo.calleCerrada)) {
                        semaforoRojoConsultado = false;
                        if (miVehiculo.velocidad == 0 && distVehiculoCarcano > 1){
                            miVehiculo.velocidad = 1;
                            //System.out.println(myAgent.getLocalName() +" ACTIVADO");
                        }
                    }
                }
            }


            miVehiculo.pos_x += miVehiculo.velocidad * miVehiculo.calleActual.dir_x;
            miVehiculo.pos_y += miVehiculo.velocidad * miVehiculo.calleActual.dir_y;


        }

    }

    protected void setup() {

        Object[] args = getArguments();
        //myID = (Integer) args[0];

        miVehiculo = (EntornoAgent.Vehiculo) args[0];

        miVehiculo.velocidad = 1;
        
        //buscamos distancia al vehiculo mas cercano de la misma calle
        distVehiculoCarcano = miVehiculo.calleActual.longitud;
        for (EntornoAgent.Vehiculo v : miVehiculo.calleActual.vehiculos.values()) {
            if (v != miVehiculo) {
                //siempre misma calle
                int dist = (v.pos_x - miVehiculo.pos_x) + (v.pos_y - miVehiculo.pos_y);
                dist = dist * (miVehiculo.calleActual.dir_x + miVehiculo.calleActual.dir_y);
                if ( dist >= 0 && dist < distVehiculoCarcano ){
                    distVehiculoCarcano = dist;
                }
            }
        }



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

        VehiculoTickerBehaviour b = new VehiculoTickerBehaviour(this, 300);
        this.addBehaviour(b);
    }
}
