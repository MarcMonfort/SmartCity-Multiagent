/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

import java.util.Random;

/**
 * @author igomez
 */
public class TermometroAgent extends Agent {

    public class HelloWorldTickerBehaviour extends TickerBehaviour {

        ACLMessage msg;

        public HelloWorldTickerBehaviour(Agent a, long period) {
            super(a, period);
        }

        public void onStart() {
            msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("Termostato", AID.ISLOCALNAME));
            msg.setSender(getAID());
        }

        public int onEnd() {
            System.out.println("Bye..");
            return 0;
        }

        public void onTick() {
            Random rand = new Random();
            String n = Integer.toString(rand.nextInt(40) - 10);
            //System.out.println(n);

            msg.setContent(n);
            send(msg);
        }

    }

    protected void setup() {

        HelloWorldTickerBehaviour b = new HelloWorldTickerBehaviour(this, 10000);
        this.addBehaviour(b);
    }
}
