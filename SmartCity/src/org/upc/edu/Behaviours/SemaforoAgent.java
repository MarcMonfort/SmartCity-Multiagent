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

/**
 * @author igomez
 */
public class SemaforoAgent extends Agent {

    EntornoAgent.Semaforo miSemaforo;
    EntornoAgent.Calle calle1;
    EntornoAgent.Calle calle2;
    String calleCerrada;

    int myID;



    public class SemaforoTickerBehaviour extends TickerBehaviour {

        //ACLMessage msg;

        public SemaforoTickerBehaviour(Agent a, long period) {
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

        }

    }

    protected void setup() {

        Object[] args = getArguments();
        myID = (Integer) args[0];

        miSemaforo = (EntornoAgent.Semaforo) args[1];
        calle1 = (EntornoAgent.Calle) args[2];
        calle2 = (EntornoAgent.Calle) args[3];


        // REGISTRO DF
        final DFAgentDescription desc = new DFAgentDescription();
        desc.setName(getAID());

        final ServiceDescription sdesc = new ServiceDescription();
        sdesc.setName("Semaforo");
        sdesc.setType("Semaforo");
        desc.addServices(sdesc);

        try {
            DFService.register(this, getDefaultDF(), desc);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        // FIN REGISTRO DF
        System.out.println("agente : " + this.getLocalName() + " registrado!!!");

        SemaforoTickerBehaviour b = new SemaforoTickerBehaviour(this, 3000);
        this.addBehaviour(b);
    }
}
