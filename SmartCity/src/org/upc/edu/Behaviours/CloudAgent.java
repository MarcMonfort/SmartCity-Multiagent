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
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;
import org.apache.jena.base.Sys;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

//import static jade.lang.acl.MessageTemplate.MatchPerformative;

/**
 * @author igomez
 */
public class CloudAgent extends Agent {

    EntornoAgent.Semaforo[] semaforos;
    AID semaforoSender;
    boolean respuestaPendiente;
    //EntornoAgent.Calle[] calles;

    private class ContractNetInitiatorBehaviour extends ContractNetInitiator {
        public ContractNetInitiatorBehaviour(Agent a, ACLMessage mt) {
            super(a, mt);
        }

        protected void handlePropose(ACLMessage propose, Vector v) {
            System.out.println("[Cloud] Agent '" + propose.getSender().getLocalName() + "' proposed '" + propose.getContent() + "'");
        }

        /*protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Agent '" + refuse.getSender().getName() + "' refused");
        }*/

        /*protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                // FAILURE notification from the JADE runtime: the receiver
                // does not exist
                System.out.println("Responder does not exist");
            } else {
                System.out.println("Agent '" + failure.getSender().getName() + "' failed");
            }
            // Immediate failure --> we will not receive a response from this agent
            nResponders--;
        }*/

        protected void handleAllResponses(Vector responses, Vector acceptances) {
            int nResponders = semaforos.length - 1;
            int maxTime = 0;
            AID bestProposer = null;

            if (responses.size() < nResponders) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
            }
            // Evaluate proposals.
            ACLMessage accept = null;
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                int proposal = Integer.parseInt(msg.getContent());
                if (proposal > maxTime) {
                    maxTime = proposal;
                    bestProposer = msg.getSender();
                }

            }
            e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                if (msg.getPerformative() == ACLMessage.PROPOSE) {

                    ACLMessage reply = msg.createReply();
                    reply.setContent(String.valueOf(maxTime));
                    acceptances.addElement(reply);
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                }
            }
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println("[Cloud] Agent '" + inform.getSender().getLocalName() + "' successfully performed the requested action");
            if (respuestaPendiente) {
                System.out.println("[Cloud] Agent : preparando respuesta por parte de '" + inform.getSender().getLocalName() + "'");
                respuestaPendiente = false;

                ACLMessage proposeCloud = new ACLMessage(ACLMessage.PROPOSE);
                proposeCloud.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
                proposeCloud.addReceiver(semaforoSender);
                proposeCloud.setSender(myAgent.getAID());
                proposeCloud.setContent(inform.getContent());
                System.out.println("[Cloud] responde al semaforoSender (" + semaforoSender.getLocalName() + ") con el tiempo de espera");

                myAgent.addBehaviour(new AchieveREInitiator(myAgent, proposeCloud) {
                    protected void handleAgree(ACLMessage agree) {
                        System.out.println("[Cloud] Ya le ha llegado la respuesta al " + agree.getSender().getLocalName() + " !!!!");
                    }
                });
            }

        }


    }





    protected void setup() {

        Object[] args = getArguments();
        semaforos = (EntornoAgent.Semaforo[]) args[0];


        // REGISTRO DF
        final DFAgentDescription desc = new DFAgentDescription();
        desc.setName(getAID());

        final ServiceDescription sdesc = new ServiceDescription();
        sdesc.setName("CentroDeDatos");
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

                semaforoSender = request.getSender();
                // Create the CFP message
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                for (int i = 0; i < semaforos.length; ++i) {
                    if (!semaforos[i].nombre.equals(semaforoSender.getLocalName())) {
                        msg.addReceiver(new AID(semaforos[i].nombre, AID.ISLOCALNAME));
                    }
                }
                //msg.removeReceiver(semaforoSender); //se envia a todos menos al sender

                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                // We want to receive a reply in 10 secs
                // HACER CON MENOS TIEMPO
                msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
                msg.setContent("solicitud-cambio-semaforos");

                respuestaPendiente = true;
                System.out.println("Cloud empieza ContractNetInitiator!!!");
                ContractNetInitiatorBehaviour cib = new ContractNetInitiatorBehaviour(myAgent, msg);
                addBehaviour(cib);

                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                //ACLMessage resultado = myAgent.blockingReceive(mt);
                //while(!cib.done());

                // Responde al semaforo inicial con el resultado de contract-net
                ACLMessage informDone  = request.createReply();
                informDone.setPerformative(ACLMessage.INFORM);
                informDone.setContent("wait");

                return informDone;
            }
            protected ACLMessage handleRequest(ACLMessage request) {
                return null;
            }
        });

    }
}
