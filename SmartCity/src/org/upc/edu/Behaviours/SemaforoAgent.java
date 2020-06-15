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
import org.apache.jena.base.Sys;

/**
 * @author igomez
 */
public class SemaforoAgent extends Agent {

    EntornoAgent.Semaforo miSemaforo;

    int myID;

    AID cloudAID;

    public int getTiempoEspera() {


        EntornoAgent.Calle cC, cA;
        if (miSemaforo.calleCerrada.equals(miSemaforo.calle1.nombre))  {
            cC = miSemaforo.calle1;
            cA = miSemaforo.calle2;
        }
        else {
            cC = miSemaforo.calle2;
            cA = miSemaforo.calle1;
        }

        // Rango de calle donde mirar coches (entre inicio de la calle y semaforo)
        int ini_x;
        int ini_y;
        int fin_x;
        int fin_y;
        
        if (cA.dir_x == 1){
            fin_x = miSemaforo.pos_x;
            fin_y = miSemaforo.pos_y;
            ini_x = fin_x - cA.longitud/2;
            ini_y = cA.ini_y;
        }
        else if (cA.dir_y == 1) {
            fin_x = miSemaforo.pos_x;
            fin_y = miSemaforo.pos_y;
            ini_x = cA.ini_x;
            ini_y = fin_y - cA.longitud/2;
        }
        else if (cA.dir_x == -1){
            ini_x = miSemaforo.pos_x;
            ini_y = miSemaforo.pos_y;
            fin_x = ini_x + cA.longitud/2;
            fin_y = cA.ini_y;
        }
        else { // (cA.dir_y == -1)
            ini_x = miSemaforo.pos_x;
            ini_y = miSemaforo.pos_y;
            fin_x = cA.ini_x;
            fin_y = ini_y + cA.longitud/2;
        }


        int proposedWaitingTime;

        if (miSemaforo.tiempoEstadoActual >= 3) {
            proposedWaitingTime = 0;
        }
        else {
            proposedWaitingTime = 3 - miSemaforo.tiempoEstadoActual;
        }


        int n_cA = 0;

        //System.out.println(miSemaforo.nombre + " cA = " + cA.nombre);
        for (EntornoAgent.Vehiculo v : cA.vehiculos.values()){
            if (v.pos_x >= ini_x && v.pos_y >= ini_y &&  v.pos_x <= fin_x && v.pos_y <= fin_y) {
                n_cA++;
                //System.out.println(miSemaforo.nombre + " cA   " + "    +1 => " + v.nombre);
            }
        }

        if (n_cA == 0) return 0; //Si no hay coches en la calle abierta, al semaforo le da igual cambiar ya
        else proposedWaitingTime += n_cA;


        if (cC.dir_x == 1){
            fin_x = miSemaforo.pos_x;
            fin_y = miSemaforo.pos_y;
            ini_x = fin_x - cC.longitud/2;
            ini_y = cC.ini_y;
        }
        else if (cC.dir_y == 1) {
            fin_x = miSemaforo.pos_x;
            fin_y = miSemaforo.pos_y;
            ini_x = cC.ini_x;
            ini_y = fin_y - cC.longitud/2;
        }
        else if (cC.dir_x == -1){
            ini_x = miSemaforo.pos_x;
            ini_y = miSemaforo.pos_y;
            fin_x = ini_x + cC.longitud/2;
            fin_y = cC.ini_y;
        }
        else { // (cC.dir_y == -1)
            ini_x = miSemaforo.pos_x;
            ini_y = miSemaforo.pos_y;
            fin_x = cC.ini_x;
            fin_y = ini_y + cC.longitud/2;
        }

        //System.out.println(miSemaforo.nombre + " cC = " + cC.nombre);
        for (EntornoAgent.Vehiculo v : cC.vehiculos.values()){
            if (v.pos_x >= ini_x && v.pos_y >= ini_y &&  v.pos_x <= fin_x && v.pos_y <= fin_y) {
                proposedWaitingTime--;
                //System.out.println(miSemaforo.nombre + " cC   " + "    -1 => " + v.nombre);
            }
        }

        if (proposedWaitingTime < 0) proposedWaitingTime = 0;
        return proposedWaitingTime;
    }


    private class ContractNetResponderBehaviour extends ContractNetResponder {


        public ContractNetResponderBehaviour(Agent a, MessageTemplate mt) {
            super(a, mt);

        }

        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            //System.out.println("Agent '" + getLocalName() + "' receives a CFP from Agent '" + cfp.getSender().getLocalName() + "' to perform action '" + cfp.getContent() + "'");

            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            // We provide a proposal
            int proposedWaitingTime = getTiempoEspera();
            
            //System.out.println("Agent '" + getLocalName() + "' proposes  '" + proposedWaitingTime + "'");
            propose.setContent(String.valueOf(proposedWaitingTime));
            return propose;

        }

        // aqui invocar al waker (si tiempo > 0, sino cambia directamente de calle)
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            int countdown = Integer.parseInt(accept.getContent());
            //System.out.println("Agent '" + getLocalName() + "' accepts proposal and is about to perform an action");
            SemaforoWakerBehaviour b = new SemaforoWakerBehaviour(myAgent, countdown*1000, false);
            myAgent.addBehaviour(b);
            //System.out.println("Agent '" + getLocalName() + "' : invocado waker, cuenta atras: '" + countdown + "'");
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            inform.setContent(String.valueOf(countdown));
            return inform;
        }

        protected void handleRejectProposal(ACLMessage reject) {
            System.out.println("Agent '" + getLocalName() + "' rejects proposal");
        }
    }

    public class SemaforoWakerBehaviour extends WakerBehaviour {

        boolean iniciador;

        public SemaforoWakerBehaviour(Agent a, long timeout, boolean ini) {
            super(a, timeout);
            iniciador = ini;
        }

        public void onStart() {
            //System.out.println("Agent " + myAgent.getLocalName() + " with SemaforoWakerBehaviour in action!!");
        }

        public int onEnd() {
            //System.out.println("Agent " + myAgent.getLocalName() + " actualizado:\n"
            //        + "   CalleCerrada : " + miSemaforo.calleCerrada + "\n"
            //        + "   tiempoActual : " + miSemaforo.tiempoEstadoActual);


            return 1; //indiferente
        }

        public void onWake() {
            if (miSemaforo.calleCerrada.equals(miSemaforo.calle1.nombre)) miSemaforo.calleCerrada = miSemaforo.calle2.nombre;
            else miSemaforo.calleCerrada = miSemaforo.calle1.nombre;
            miSemaforo.tiempoEstadoActual = 0;
            if(iniciador) {
                ACLMessage proposeCloud = new ACLMessage(ACLMessage.PROPOSE);
                proposeCloud.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
                proposeCloud.addReceiver(cloudAID);
                proposeCloud.setSender(myAgent.getAID());
                proposeCloud.setContent("negociacion-finalizada");

                myAgent.addBehaviour(new AchieveREInitiator(myAgent, proposeCloud) {});
            }
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
            miSemaforo.tiempoEstadoActual += 1;
        }

    }




    protected void setup() {

        Object[] args = getArguments();
        myID = (Integer) args[0];

        miSemaforo = (EntornoAgent.Semaforo) args[1];
        //calle1 = (EntornoAgent.Calle) args[2];
        //calle2 = (EntornoAgent.Calle) args[3];
        //calleCerrada = (String) args[4];
        //if (calleCerrada.equals(calle1.nombre)) calleAbierta = calle2.nombre;
        //else calleAbierta = calle1.nombre;
        //


        System.out.println("Agent " + this.getLocalName() + " inicial:\n"
                + "   CalleCerrada : " + miSemaforo.calleCerrada);

        // REGISTRO DF
        final DFAgentDescription desc = new DFAgentDescription();
        desc.setName(getAID());

        final ServiceDescription sdesc = new ServiceDescription();
        sdesc.setName(miSemaforo.nombre);
        sdesc.setType("Semaforo");
        desc.addServices(sdesc);

        try {
            DFService.register(this, getDefaultDF(), desc);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        // FIN REGISTRO DF
        System.out.println("agente : " + this.getLocalName() + " registrado!!!");


        //Busca cloudAID

        DFAgentDescription search_template;
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "Cloud" );
        search_template = new DFAgentDescription();
        search_template.addServices(sd);

        DFAgentDescription[] search_results;
        try {
            search_results = DFService.search(this, search_template);
            if (search_results.length > 0) {
                cloudAID = search_results[0].getName();
                System.out.println(this.getLocalName() + " : CloudAgent encontrado");
            }
        } catch (FIPAException ex) {
            System.out.println("Agente " + this.getLocalName() + ": Error al buscar al cloud");
        }

        // Recibe respuesta de cloud, con el tiempo de espera => invoca waker
       // MessageTemplate aux = MessageTemplate.not(MessageTemplate.MatchContent("negociacion-finalizada"));
        MessageTemplate mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        //mt = MessageTemplate.and(mt, aux);
        this.addBehaviour(new AchieveREResponder(this, mt) {
            protected ACLMessage prepareResultNotification(ACLMessage propose, ACLMessage response) {
                return null;
            }

            protected ACLMessage handleRequest(ACLMessage propose) {
                //System.out.println(myAgent.getLocalName() + ": countdown = " + propose.getContent());
                int countdown = Integer.parseInt(propose.getContent());
                //System.out.println("Agent '" + getLocalName() + "' recibió countdown del cloud... va a invocar a waker");
                SemaforoWakerBehaviour b = new SemaforoWakerBehaviour(myAgent, countdown*1000, true);
                myAgent.addBehaviour(b);
                //System.out.println("Agent '" + getLocalName() + "' : invocado waker, cuenta atras: '" + countdown + "' [SENDER]");

                ACLMessage informDone  = propose.createReply();
                informDone.setPerformative(ACLMessage.AGREE);
                return informDone;
            }
        });


        //Responder a la peticion del coche de ponerse verde
        mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
        this.addBehaviour(new AchieveREResponder(this, mt) {
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                return null;
            }
            protected ACLMessage handleRequest(ACLMessage request) {
                ACLMessage requestCloud = new ACLMessage(ACLMessage.REQUEST);
                requestCloud.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                requestCloud.addReceiver(cloudAID);
                requestCloud.setSender(myAgent.getAID());
                int miTiempo = getTiempoEspera();
                requestCloud.setContent(String.valueOf(miTiempo));
                //System.out.println("Agent " + myAgent.getLocalName() + "va a llamar a Cloud con tiempo: " + miTiempo);
                //System.out.println("Agent '" + getLocalName() + "' proposes  '" + miTiempo + "' [SENDER]");

                // Pregunta al cloud para que negocie con los otros semaforos
                myAgent.addBehaviour(new AchieveREInitiator(myAgent, requestCloud) {
                    protected void handleInform(ACLMessage inform) {
                        //System.out.println(myAgent.getLocalName() + ": recibido => " + inform.getContent());

                    }
                });
                // Espera a que el cloud le responda
                ACLMessage informDone  = request.createReply();
                informDone.setPerformative(ACLMessage.INFORM);
                return informDone;
            }
        });

        mt = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        ContractNetResponderBehaviour crb = new ContractNetResponderBehaviour(this, mt);
        this.addBehaviour(crb);

        SemaforoTickerBehaviour b = new SemaforoTickerBehaviour(this, 1000);
        this.addBehaviour(b);
    }
}
