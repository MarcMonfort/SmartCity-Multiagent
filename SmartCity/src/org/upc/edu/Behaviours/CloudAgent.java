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
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author igomez
 */
public class CloudAgent extends Agent {

    EntornoAgent.Semaforo[] semaforos;
    //EntornoAgent.Calle[] calles;

    private class ContractNetInitiatorBehaviour extends ContractNetInitiator {
        public ContractNetInitiatorBehaviour(Agent a, ACLMessage mt) {
            super(a, mt);
        }

        protected void handlePropose(ACLMessage propose, Vector v) {
            System.out.println("Agent '" + propose.getSender().getName() + "' proposed '" + propose.getContent() + "'");
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Agent '" + refuse.getSender().getName() + "' refused");
        }

        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                // FAILURE notification from the JADE runtime: the receiver
                // does not exist
                System.out.println("Responder does not exist");
            } else {
                System.out.println("Agent '" + failure.getSender().getName() + "' failed");
            }
            // Immediate failure --> we will not receive a response from this agent
            nResponders--;
        }

        protected void handleAllResponses(Vector responses, Vector acceptances) {
            if (responses.size() < nResponders) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
            }
            // Evaluate proposals.
            int bestProposal = -1;
            AID bestProposer = null;
            ACLMessage accept = null;
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                    int proposal = Integer.parseInt(msg.getContent());
                    if (proposal > bestProposal) {
                        bestProposal = proposal;
                        bestProposer = msg.getSender();
                        accept = reply;
                    }
                }
            }
            // Accept the proposal of the best proposer
            if (accept != null) {
                System.out.println("Accepting proposal '" + bestProposal + "' from responder '" + bestProposer.getName() + "'");
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println("Agent '" + inform.getSender().getName() + "' successfully performed the requested action");
        }


    }





    protected void setup() {

        Object[] args = getArguments();
        semaforos = (EntornoAgent.Semaforo[]) args[0];


        // REGISTRO DF
        final DFAgentDescription desc = new DFAgentDescription();
        desc.setName(getAID());

        final ServiceDescription sdesc = new ServiceDescription();
        sdesc.setName("Cloud");
        sdesc.setType("Cloud");
        desc.addServices(sdesc);

        try {
            DFService.register(this, getDefaultDF(), desc);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        // FIN REGISTRO DF
        System.out.println("agente : " + this.getLocalName() + " registrado!!!");



        // Escucha las peticiones de semaforos que piden cambiar de color
        MessageTemplate mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
        this.addBehaviour(new AchieveREResponder(this, mt) {
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {

                // Create the CFP message
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                for (int i = 0; i < args.length; ++i) {
                    msg.addReceiver(new AID((String) args[i], AID.ISLOCALNAME));
                }
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                // We want to receive a reply in 10 secs
                msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
                msg.setContent("perform-AIA-exam");
                ContractNetInitiatorBehaviour cib = new ContractNetInitiatorBehaviour(myAgent, msg);
                addBehaviour(cib);

                ACLMessage informDone  = request.createReply();
                informDone.setPerformative(ACLMessage.INFORM);
                informDone.setContent(String.valueOf(t));
                return informDone;
            }
            protected ACLMessage handleRequest(ACLMessage request) {
                return null;
            }
        });

    }
}
