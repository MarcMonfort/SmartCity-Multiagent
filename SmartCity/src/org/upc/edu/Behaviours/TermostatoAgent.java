/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * @author igomez
 */
public class TermostatoAgent extends Agent {

    public class HelloWorldCyclicBehaviour extends CyclicBehaviour {

        MessageTemplate tmpl;

        public HelloWorldCyclicBehaviour() {
        }

        public void onStart() {
            tmpl = MessageTemplate.MatchSender((new AID("Termometro", AID.ISLOCALNAME)));
        }

        public int onEnd() {
            System.out.println("Bye..");
            return 0;
        }

        public void action() {
            ACLMessage msg = receive(tmpl);
            if (msg != null) {
                int n = Integer.parseInt(msg.getContent());

                if (n < 15) System.out.println("calefacción encendida (" + n + "°C)");
                else if (n > 25) System.out.println("refrigeración encendida (" + n + "°C)");
                else System.out.println("temperatura adecuada (" + n + "°C)");
            }
            else {
                block();
            }
        }

    }

    protected void setup() {

        HelloWorldCyclicBehaviour b = new HelloWorldCyclicBehaviour();
        this.addBehaviour(b);
    }
}
