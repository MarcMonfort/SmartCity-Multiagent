/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Agentes;

import jade.core.AID;
import jade.core.Agent;
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

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;



public class CloudAgent extends Agent {

    private HashMap<String, EntornoAgent.Semaforo> semaforos;
    AID semaforoSender;
    boolean respuestaPendiente;
    boolean negociacionEnCurso = false;
    int minTiempoRequested;

    private class ContractNetInitiatorBehaviour extends ContractNetInitiator {
        public ContractNetInitiatorBehaviour(Agent a, ACLMessage mt) {
            super(a, mt);
        }

        protected void handlePropose(ACLMessage propose, Vector v) {
            //System.out.println("[Cloud] Agent '" + propose.getSender().getLocalName() + "' proposed '" + propose.getContent() + "'");
        }


        protected void handleAllResponses(Vector responses, Vector acceptances) {
            int nResponders = semaforos.size() - 1;
            int maxTime = minTiempoRequested;

            if (responses.size() < nResponders) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
            }
            // Evaluate proposals.
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                int proposal = Integer.parseInt(msg.getContent());
                if (proposal > maxTime) {
                    maxTime = proposal;
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
            //System.out.println("[Cloud] Agent '" + inform.getSender().getLocalName() + "' successfully performed the requested action");
            if (respuestaPendiente) {
               // System.out.println("[Cloud] Agent : preparando respuesta por parte de '" + inform.getSender().getLocalName() + "'");
                respuestaPendiente = false;

                ACLMessage proposeCloud = new ACLMessage(ACLMessage.PROPOSE);
                proposeCloud.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
                proposeCloud.addReceiver(semaforoSender);
                proposeCloud.setSender(myAgent.getAID());
                proposeCloud.setContent(inform.getContent());

                myAgent.addBehaviour(new AchieveREInitiator(myAgent, proposeCloud) {
                    protected void handleAgree(ACLMessage agree) {
                        //System.out.println("[Cloud] Ya le ha llegado la respuesta al " + agree.getSender().getLocalName());
                    }
                });
                
            }

        }


    }





    protected void setup() {

        Object[] args = getArguments();
        semaforos = (HashMap<String, EntornoAgent.Semaforo>) args[0];


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
                return null;
            }
            protected ACLMessage handleRequest(ACLMessage request) {
                ACLMessage informDone  = request.createReply();
                if (!negociacionEnCurso) {
                    negociacionEnCurso = true;

                    semaforoSender = request.getSender();
                    // El tiempo del semaforoSender
                    minTiempoRequested = Integer.parseInt(request.getContent());

                    // Create the CFP message
                    ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                    for (EntornoAgent.Semaforo s : semaforos.values()) {
                        if (!s.nombre.equals(semaforoSender.getLocalName())) {
                            msg.addReceiver(new AID(s.nombre, AID.ISLOCALNAME));
                        }
                    }

                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    // We want to receive a reply in 1 sec
                    msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));


                    respuestaPendiente = true;
                    informDone.setContent("SI");
                    ContractNetInitiatorBehaviour cib = new ContractNetInitiatorBehaviour(myAgent, msg);
                    addBehaviour(cib);
                }
                else informDone.setContent("NO");

                informDone.setPerformative(ACLMessage.INFORM);

                return informDone;
            }
        });

        // Semaforo sender informa de que ya se ha hecho el cambio de calles en los semaforos
        mt = MessageTemplate.MatchContent("negociacion-finalizada");
        this.addBehaviour(new AchieveREResponder(this, mt) {
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                negociacionEnCurso = false;
                return null;
            }
            protected ACLMessage handleRequest(ACLMessage request) {
               return null;
            }
        });
    }
}
