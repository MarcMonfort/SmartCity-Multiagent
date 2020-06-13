/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;

/**
 * @author igomez
 */
public class SemaforoAgent extends Agent {

    EntornoAgent.Semaforo miSemaforo;
    EntornoAgent.Calle calle1;
    EntornoAgent.Calle calle2;
    String calleCerrada;
    int tiempoEstadoActual = 0;

    int myID;

    private class ContractNetResponderBehaviour extends ContractNetResponder {

        private boolean performAction() {
            // Simulate action execution by generating a random number
            return (Math.random() > 0.2);
        }

        public ContractNetResponderBehaviour(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            System.out.println("Agent '" + getLocalName() + "' receives a CFP from Agent '" + cfp.getSender().getName() + "' to perform action '" + cfp.getContent() + "'");
            if (performs.equalsIgnoreCase("YES")) {
                // We provide a proposal
                System.out.println("Agent '" + getLocalName() + "' proposes  '" + evaluation + "'");
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(String.valueOf(evaluation));
                return propose;
            } else {
                // We refuse to provide a proposal
                System.out.println("Agent '" + getLocalName() + "' is not interested in the proposal");
                throw new RefuseException("evaluation-failed");
            }
        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            System.out.println("Agent '" + getLocalName() + "' accepts proposal and is about to perform an action");
            if (performAction()) {
                System.out.println("Agent '" + getLocalName() + "' succesfully performs action");
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            } else {
                System.out.println("Agent '" + getLocalName() + "' failed to perform action");
                throw new FailureException("unexpected-error");
            }
        }

        protected void handleRejectProposal(ACLMessage reject) {
            System.out.println("Agent '" + getLocalName() + "' rejects proposal");
        }
    }



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
            tiempoEstadoActual += 1;
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

        SemaforoTickerBehaviour b = new SemaforoTickerBehaviour(this, 1000);
        this.addBehaviour(b);

        //Responder a la peticion del coche de ponerse verde
        MessageTemplate mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
        this.addBehaviour(new AchieveREResponder(this, mt) {
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {

                // Pregunta al cloud para que negocie con los otros semaforos
                myAgent.addBehaviour(new AchieveREInitiator(myAgent, request) {
                    protected void handleInform(ACLMessage inform) {
                        String respuesta = inform.getContent();
                    }
                });
                // Espera a que el cloud le responda
                ACLMessage r = blockingReceive();
                String respuesta_cloud = r.getContent();

                // Informa al vehiculo con la respuesta del cloud (sigue rojo / se pone verde)
                ACLMessage informDone  = request.createReply();
                informDone.setPerformative(ACLMessage.INFORM);
                informDone.setContent(respuesta_cloud);
                return informDone;
            }
            protected ACLMessage handleRequest(ACLMessage request) {
                return null;
            }
        });
    }
}
