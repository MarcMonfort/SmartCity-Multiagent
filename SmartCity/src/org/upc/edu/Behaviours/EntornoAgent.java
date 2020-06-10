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
public class EntornoAgent extends Agent {

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

        EntornoTickerBehaviour b = new EntornoTickerBehaviour(this, 3000);
        this.addBehaviour(b);
    }
}
