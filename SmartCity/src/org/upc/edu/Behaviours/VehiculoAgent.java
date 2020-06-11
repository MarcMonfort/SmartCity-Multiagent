/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Random;

/**
 * @author igomez
 */
public class VehiculoAgent extends Agent {

    int pos_x = 0;
    int pos_y = 0;

    int direccion = 0;

    int velocidad = 0;
    int aceleracion = 1;

    int carril_fin = 50; //fin de carril
    int[] carril_inter = {20,30};



    public class VehiculoTickerBehaviour extends TickerBehaviour {

        ACLMessage msg;

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
            switch(direccion) {
                case 0:
                    pos_x += velocidad; break;
                case 1:
                    pos_x -= velocidad; break;
                case 2:
                    pos_y += velocidad; break;
                case 3:
                    pos_y -= velocidad; break;
            }
            velocidad += aceleracion;
            System.out.println("Vehiculo: Posicion=("+pos_x+", "+pos_y+"), Velocidad="+velocidad+", Dirección="+direccion);
            //System.out.println("hola");
        }

    }

    protected void setup() {
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

        VehiculoTickerBehaviour b = new VehiculoTickerBehaviour(this, 3000);
        this.addBehaviour(b);
    }
}
