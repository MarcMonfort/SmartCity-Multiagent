/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.upc.edu.Behaviours;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.tools.sniffer.Sniffer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author igomez
 */
public class EntornoAgent extends Agent {

    public static class Calle {
        public String nombre;
        public int longitud;
        public int ini_x;
        public int ini_y;
        public int fin_x;
        public int fin_y;
        public int dir_x;
        public int dir_y;
        
        public Calle siguiente;
        public ArrayList<Calle> inter; //null
        public ArrayList<Semaforo> semaforos; //arrasy list

        public HashMap<String, Vehiculo> vehiculos = new HashMap<>();
    }

    public static class Vehiculo {
        public int ID;
        public String nombre;
        public int pos_x;
        public int pos_y;
        public int obj_x;
        public int obj_y;
        public int velocidad;
        //public String calle_actual;

        public Calle calleActual;
    }

    public static class Semaforo {
        public String nombre;
        public int pos_x;
        public int pos_y;
        //public String calle1;
        //public String calle2;
        public String calleCerrada; //calle1 o calle2
        public int tiempoEstadoActual;

        public Calle calle1;
        public Calle calle2;

    }

    public static int[] getCoord(String c) {
        String[] nums = c.split(",");
        int[] coord = new int[2];
        coord[0] = Integer.parseInt(nums[0]);
        coord[1] = Integer.parseInt(nums[1]);
        return coord;
    }

    //private ArrayList<Vehiculo> info_vehiculos;

    private HashMap<String, Vehiculo> info_vehiculos = new HashMap<>();
    private Calle[] calles;
    private Vehiculo[] vehiculos;
    private Semaforo[] semaforos;
    
    private void inicializarEntorno() throws StaleProxyException {
        String JENA = "./";
        String File = "Ontologia.owl";
        String NamingContext = "http://www.semanticweb.org/sid/smartCity";

        System.out.println("----------------Starting program -------------");

        OntologyParser parser = new OntologyParser(JENA, File, NamingContext);

        parser.loadOntology();
        System.out.println("Ontology loaded");

        ContainerController cc = getContainerController();
        AgentController sniffer = cc.createNewAgent("Sniffer", Sniffer.class.getName(), null);
        sniffer.start();
        System.out.println("Sniffer started");

        System.out.println("INSTANCIAS DE CALLES:");
        calles = parser.getCalles();

        for (int i = 0; i < calles.length; i++) {
            System.out.println(calles[i].nombre + ":");
            System.out.println("  Longitud: " + calles[i].longitud);
            System.out.println("  Pos ini : " + calles[i].ini_x + ", " + calles[i].ini_y);
            System.out.println("  Pos fin : " + calles[i].fin_x + ", " + calles[i].fin_y);

        }

        

        System.out.println("INSTANCIAS DE SEMAFOROS:");
        semaforos = parser.getSemaforos();

        for (int i = 0; i < semaforos.length; i++) {
            System.out.println(semaforos[i].nombre + ":");
            System.out.println("  Calle1 : " + semaforos[i].calle1.nombre);
            System.out.println("  Calle2 : " + semaforos[i].calle2.nombre);

            System.out.println("  Pos : " + semaforos[i].pos_x + ", " + semaforos[i].pos_y);
            System.out.println("  CalleCerrada : " + semaforos[i].calleCerrada);
            Object[] args = new Object[5];

            String nombre_calle1 = semaforos[i].calle1.nombre;
            String nombre_calle2 = semaforos[i].calle2.nombre;

            int id_calle1 = Integer.parseInt(nombre_calle1.substring(nombre_calle1.length() - 1)) - 1;
            int id_calle2 = Integer.parseInt(nombre_calle2.substring(nombre_calle2.length() - 1)) - 1;

            semaforos[i].calle1 = calles[id_calle1];
            semaforos[i].calle2 = calles[id_calle2];

            args[0] = i;
            args[1] = semaforos[i];
            args[2] = calles[id_calle1];
            args[3] = calles[id_calle2];
            args[4] = semaforos[i].calleCerrada;
            AgentController ac = cc.createNewAgent(semaforos[i].nombre, "org.upc.edu.Behaviours.SemaforoAgent", args);
            ac.start();
        }

        {
        Object[] args = new Object[1];
        args[0] = semaforos;
        //args[1] = calles
        AgentController ac = cc.createNewAgent("CentroDeDatos", "org.upc.edu.Behaviours.CloudAgent", args);
        ac.start();
        }


        System.out.println("INSTANCIAS DE VEHICULOS:");
        vehiculos = parser.getVehiculos();

        // crea objetos calle siguiente i calle interseccion (actuan como punteros)
        for (Calle c : calles) { //relaciona calles con calles

            int id_calle = Integer.parseInt(c.siguiente.nombre.substring(c.siguiente.nombre.length() - 1)) - 1;
            c.siguiente = calles[id_calle];

            ArrayList<Calle> intersecciones = new ArrayList<>();
            for (Calle interseccion : c.inter){
                id_calle = Integer.parseInt(interseccion.nombre.substring(interseccion.nombre.length() - 1)) - 1;
                intersecciones.add(calles[id_calle]);
            }
            c.inter = intersecciones;

            ArrayList<Semaforo> lista_semaforos = new ArrayList<>();    //correcta apuntando bien
            for (Semaforo semaforo : c.semaforos){
                int id_semaforo = Integer.parseInt(semaforo.nombre.substring(semaforo.nombre.length() - 1)) - 1;
                lista_semaforos.add(semaforos[id_semaforo]);
            }
            c.semaforos = lista_semaforos;
        }

        for (int i = 0; i < vehiculos.length; i++) {
            System.out.println(vehiculos[i].nombre + ":");
            System.out.println("  Calle: " + vehiculos[i].calleActual.nombre);
            System.out.println("  Pos : " + vehiculos[i].pos_x + ", " + vehiculos[i].pos_y);
            System.out.println("  Obj : " + vehiculos[i].obj_x + ", " + vehiculos[i].obj_y);
            System.out.println("  Vel : " + vehiculos[i].velocidad);
            Object[] args = new Object[3];

            //String nombre_calle = vehiculos[i].calleActual.nombre;


            int id_calle = Integer.parseInt(vehiculos[i].calleActual.nombre.substring(vehiculos[i].calleActual.nombre.length() - 1)) - 1;
            vehiculos[i].calleActual = calles[id_calle];
            
            vehiculos[i].calleActual.vehiculos.put(vehiculos[i].nombre, vehiculos[i]);

            vehiculos[i].ID = i;

            args[0] = i; //id vehiculo... se podria obtener deel nombre...
            args[1] = vehiculos[i];
            //args[2] = calles[id_calle];

            AgentController ac = cc.createNewAgent(vehiculos[i].nombre, "org.upc.edu.Behaviours.VehiculoAgent", args);
            ac.start();
        }
    }

    public class EntornoTickerBehaviour extends TickerBehaviour {

        ACLMessage msg;

        public EntornoTickerBehaviour(Agent a, long period) {
            super(a, period);
        }

        public void onTick() {

            System.out.println(" "); // print vector de info 
            for (Vehiculo v : vehiculos){
                System.out.println("["+v.nombre + "] pos = (" + v.pos_x + "," + v.pos_y + ") velocidad=" + v.velocidad);
            }
            for (Semaforo s : semaforos){
                System.out.println("["+s.nombre + "] calleCerrada: " + s.calleCerrada);
            }

            for (int i = 10; i >= 0; i--) {
                for (int j = 0; j <= 10; j++) {
                    int v = buscaVehiculos(i,j);
                    int s = buscaSemaforo(i,j);
                    int o = buscaObjetivo(i,j);
                    if(v != -1) {
                        String color = String.format("\u001b[3%dm", v+3);
                        
                        System.out.print(color + (v+1) + "\u001b[0m" + " ");
                    }
                    else if (s != -1) {
                        if (s == 0) System.out.print("\u001b[31m"+"◉" + "\u001b[0m" + " ");
                        else if (s == 1) System.out.print("\u001b[32m" + "◉" + "\u001b[0m"+  " ");
                    }
                    else if (o != -1) {
                        String color = String.format("\u001b[3%dm", o+3);
                        System.out.print(color + "▴" + "\u001b[0m" + " ");
                    }
                    else if(i==0 || i==5 || i==10 || j==0 || j==5 || j==10) System.out.print("· ");
                    else System.out.print("  ");
                }
                System.out.print("\n");
            }

        }

    }
    
    
    public int buscaVehiculos(int i, int j) {
        int n = -1;
        boolean found = false;
        for (Vehiculo v: vehiculos) {
            if (v.pos_x == j && v.pos_y == i) {
                if (found) System.out.println("ERROOOOOOOOORRRR");
                else {
                    n = v.ID;
                    found = true;
                }
            }
        }
        return n;
    }

    public int buscaObjetivo(int i, int j) {
        int n = -1;
        for (Vehiculo v: vehiculos) {
            if (v.obj_x == j && v.obj_y == i) {
                n = v.ID;
            }
        }
        return n;
    }

    public int buscaSemaforo(int i, int j) {
        int n = -1;
        for (Semaforo s: semaforos) {
            int dir_x = s.calle1.dir_x;
            int dir_y = s.calle1.dir_y;

            int dir_x_2 = s.calle2.dir_x;
            int dir_y_2 = s.calle2.dir_y;

            if ( (j + dir_x)==s.pos_x && (i + dir_y)==s.pos_y ) {
                if (s.calleCerrada.equals(s.calle1.nombre)){
                    n = 0;
                }
                else n = 1;
            }
            else if ( (j + dir_x_2)==s.pos_x && (i + dir_y_2)==s.pos_y ) {
                if (s.calleCerrada.equals(s.calle2.nombre)){
                    n = 0;
                }
                else n = 1;
            }
        }
        return n;
    }
    

    protected void setup() {

        try {
            inicializarEntorno();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

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


        // ya no haria falta, todo con punteros
        /* MessageTemplate mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_QUERY);
        this.addBehaviour(new AchieveREResponder(this, mt) {
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {

                // 2 posibilities (accident, nextobstacle)
                String[] contentArray = request.getContent().split(",");
                int elID = Integer.parseInt(contentArray[1]);

                // al ser por referencia ya deberia modificarse
                vehiculos[elID].pos_x = Integer.parseInt(contentArray[2]);
                vehiculos[elID].pos_y = Integer.parseInt(contentArray[3]);
                vehiculos[elID].velocidad = Integer.parseInt(contentArray[4]);

                // buscar vehiculo mas cercano en la calle actual
                String calleActual = vehiculos[elID].calle_actual;
                int pos_x = vehiculos[elID].pos_x;
                int pos_y = vehiculos[elID].pos_y;

                for (Vehiculo v : vehiculos){
                    if (v.calle_actual.equal(calleActual) ){
                        int dist = (v.pos_x - pos_x)*
                    }
                }


                // devuelve veehiculo mas cercano, y luz del semaforo

                ACLMessage informDone  = request.createReply();
                informDone.setPerformative(ACLMessage.INFORM);
                informDone.setContent("mensaje para ..." + request.getSender().getName());
                return informDone;
            }
            protected ACLMessage handleRequest(ACLMessage request) {
                return null;
            }
        }); */

        EntornoTickerBehaviour b = new EntornoTickerBehaviour(this, 300);
        this.addBehaviour(b);

    }
}
