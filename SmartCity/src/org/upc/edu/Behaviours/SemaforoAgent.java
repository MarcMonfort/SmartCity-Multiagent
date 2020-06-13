/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
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
    String calleAbierta;
    String calleCerrada;
    int tiempoEstadoActual = 0;

    int myID;

    private boolean calleAbiertaVacia() {
        return true;
    }

    private class ContractNetResponderBehaviour extends ContractNetResponder {

        public ContractNetResponderBehaviour(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            System.out.println("Agent '" + getLocalName() + "' receives a CFP from Agent '" + cfp.getSender().getName() + "' to perform action '" + cfp.getContent() + "'");
            
            int proposedWaitingTime;
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            // We provide a proposal
            if (tiempoEstadoActual >= 10 || calleAbiertaVacia()) {
                proposedWaitingTime = 0;
            }
            else {
                proposedWaitingTime = 10 - tiempoEstadoActual;
            }
            System.out.println("Agent '" + getLocalName() + "' proposes  '" + proposedWaitingTime + "'");
            propose.setContent(String.valueOf(proposedWaitingTime));
            return propose;

        }

        // aqui invocar al waker (si tiempo > 0, sino cambia directamente de calle)
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            int countdown = Integer.parseInt(accept.getContent());
            System.out.println("Agent '" + getLocalName() + "' accepts proposal and is about to perform an action");
            SemaforoWakerBehaviour b = new SemaforoWakerBehaviour(myAgent, countdown*1000);
            myAgent.addBehaviour(b);
            System.out.println("Agent '" + getLocalName() + "' : invocado waker, con tiempo '" + countdown + "'");
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }

        protected void handleRejectProposal(ACLMessage reject) {
            System.out.println("Agent '" + getLocalName() + "' rejects proposal");
        }
    }

    public class SemaforoWakerBehaviour extends WakerBehaviour {

        public SemaforoWakerBehaviour(Agent a, long timeout) {
            super(a, timeout);
        }

        public void onStart() {
            System.out.println("Agent " + myAgent + " with SemaforoWakerBehaviour in action!!");
        }

        public int onEnd() {
            System.out.println("Agent " + myAgent + "actualizado:");
            System.out.println("   CalleCerrada : " + calleCerrada);
            System.out.println("   CalleAbierta : " + calleAbierta);

            return 1; //indiferente
        }

        public void onWake() {
            String aux   = calleAbierta;
            calleAbierta = calleCerrada;
            calleCerrada = aux;
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

                // poner verde en el entorno

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
