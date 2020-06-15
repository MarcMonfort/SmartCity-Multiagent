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

        public boolean pausa = false;

    }

    public static int[] getCoord(String c) {
        String[] nums = c.split(",");
        int[] coord = new int[2];
        coord[0] = Integer.parseInt(nums[0]);
        coord[1] = Integer.parseInt(nums[1]);
        return coord;
    }

    //private ArrayList<Vehiculo> info_vehiculos;

    //private HashMap<String, Vehiculo> info_vehiculos = new HashMap<>();
    /* private Calle[] calles;
    private Vehiculo[] vehiculos;
    private Semaforo[] semaforos; */

    private HashMap<String, Calle> calles;
    private HashMap<String, Vehiculo> vehiculos;
    private HashMap<String, Semaforo> semaforos;
    
    private void inicializarEntorno() throws StaleProxyException {
        String JENA = "./";
        String File = "Ontologia.owl";
        String NamingContext = "http://www.semanticweb.org/sid/smartCity";

        System.out.println("----------------Starting program -------------");

        OntologyParser parser = new OntologyParser(JENA, File, NamingContext);

        parser.loadOntology();
        System.out.println("Ontology loaded");

        ContainerController cc = getContainerController();
        //AgentController sniffer = cc.createNewAgent("Sniffer", Sniffer.class.getName(), null);
        //sniffer.start();
        //System.out.println("Sniffer started");

        System.out.println("INSTANCIAS DE CALLES:");
        calles = parser.getCalles();

        for (Calle c : calles.values()) {
            System.out.println(c.nombre + ":");
            System.out.println("  Longitud: " + c.longitud);
            System.out.println("  Pos ini : " + c.ini_x + ", " + c.ini_y);
            System.out.println("  Pos fin : " + c.fin_x + ", " + c.fin_y);

        }

        

        System.out.println("INSTANCIAS DE SEMAFOROS:");
        semaforos = parser.getSemaforos();

        for (Semaforo s : semaforos.values()) {
            System.out.println(s.nombre + ":");
            System.out.println("  Calle1 : " + s.calle1.nombre);
            System.out.println("  Calle2 : " + s.calle2.nombre);

            System.out.println("  Pos : " + s.pos_x + ", " + s.pos_y);
            System.out.println("  CalleCerrada : " + s.calleCerrada);
            Object[] args = new Object[1];

            //String nombre_calle1 = s.calle1.nombre;
            //String nombre_calle2 = s.calle2.nombre;

            //int id_calle1 = Integer.parseInt(nombre_calle1.substring(nombre_calle1.length() - 1)) - 1;
            //int id_calle2 = Integer.parseInt(nombre_calle2.substring(nombre_calle2.length() - 1)) - 1;


            s.calle1 = calles.get(s.calle1.nombre);
            s.calle2 = calles.get(s.calle2.nombre);

            args[0] = s;

            AgentController ac = cc.createNewAgent(s.nombre, "org.upc.edu.Behaviours.SemaforoAgent", args);
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
        for (Calle c : calles.values()) { //relaciona calles con calles

            //int id_calle = Integer.parseInt(c.siguiente.nombre.substring(c.siguiente.nombre.length() - 1)) - 1;
            //c.siguiente = calles[id_calle];
            c.siguiente = calles.get(c.siguiente.nombre);

            ArrayList<Calle> intersecciones = new ArrayList<>();
            for (Calle interseccion : c.inter){
                //id_calle = Integer.parseInt(interseccion.nombre.substring(interseccion.nombre.length() - 1)) - 1;
                //intersecciones.add(calles[id_calle]);
                intersecciones.add(calles.get(interseccion.nombre));
            }
            c.inter = intersecciones;

            ArrayList<Semaforo> lista_semaforos = new ArrayList<>();    //correcta apuntando bien
            for (Semaforo semaforo : c.semaforos){
                //int id_semaforo = Integer.parseInt(semaforo.nombre.substring(semaforo.nombre.length() - 1)) - 1;
                //lista_semaforos.add(semaforos[id_semaforo]);
                lista_semaforos.add(semaforos.get(semaforo.nombre));
            }
            c.semaforos = lista_semaforos;
        }

        for (Vehiculo v : vehiculos.values()) {
            System.out.println(v.nombre + ":");
            System.out.println("  Calle: " + v.calleActual.nombre);
            System.out.println("  Pos : " + v.pos_x + ", " + v.pos_y);
            System.out.println("  Obj : " + v.obj_x + ", " + v.obj_y);
            System.out.println("  Vel : " + v.velocidad);
            Object[] args = new Object[1];

            //String nombre_calle = vehiculos[i].calleActual.nombre;


            //int id_calle = Integer.parseInt(vehiculos[i].calleActual.nombre.substring(vehiculos[i].calleActual.nombre.length() - 1)) - 1;
            //vehiculos[i].calleActual = calles[id_calle];
            v.calleActual = calles.get(v.calleActual.nombre);
            
            //vehiculos[i].calleActual.vehiculos.put(vehiculos[i].nombre, vehiculos[i]);
            v.calleActual.vehiculos.put(v.nombre, v);

            //vehiculos[i].ID = i;

            //args[0] = i; //id vehiculo... se podria obtener deel nombre...
            args[0] = v;
            //args[2] = calles[id_calle];

            AgentController ac = cc.createNewAgent(v.nombre, "org.upc.edu.Behaviours.VehiculoAgent", args);
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
            for (Vehiculo v : vehiculos.values()){
                System.out.println("["+v.nombre + "] pos = (" + v.pos_x + "," + v.pos_y + ") velocidad=" + v.velocidad);
            }
            for (Semaforo s : semaforos.values()){
                System.out.println("["+s.nombre + "] calleCerrada: " + s.calleCerrada);
            }

            for (int i = 10; i >= 0; i--) {
                for (int j = 0; j <= 10; j++) {
                    int v = buscaVehiculos(i,j);
                    int s = buscaSemaforo(i,j);

                    String reset = "\u001b[0m";
                    String green = "\u001b[32m";
                    String red = "\u001b[31m";
                    String yellow = "\u001b[33m";

                    if (v != -1) {
                        if (v == 1) {

                            if (s == 0){
                                System.out.print(red + "■" + reset + " ");
                            }
                            else if (s == 1) {
                                System.out.print(green + "■" + reset + " ");
                            }
                            else if (s == 2) {
                                System.out.print(yellow + "■" + reset + " ");
                            }
                            else System.out.print("■" + reset + " ");

                        }
                    
                        else if (v == 2) {
                            String color = String.format("\u001b[3%dm", 5);
                            System.out.print(color + "☒" + reset + " ");
                        }
                    }

                    else if (s != -1) {
                        if (s == 0) System.out.print(red +"◉" + reset + " ");
                        else if (s == 1) System.out.print(green + "◉" + reset +  " ");
                        else if (s == 2) System.out.print(yellow + "◉" + reset +  " ");
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
        for (Vehiculo v: vehiculos.values()) {
            if (v.pos_x == j && v.pos_y == i) {
                if (found) {
                    n = 2;
                }
                else {
                    n = 1;
                    found = true;
                }
            }
        }
        return n;
    }

    public int buscaObjetivo(int i, int j) {
        int n = -1;
        for (Vehiculo v: vehiculos.values()) {
            if (v.obj_x == j && v.obj_y == i) {
                n = v.ID;
            }
        }
        return n;
    }

    public int buscaSemaforo(int i, int j) {
        int n = -1;
        for (Semaforo s: semaforos.values()) {
            int dir_x = s.calle1.dir_x;
            int dir_y = s.calle1.dir_y;

            int dir_x_2 = s.calle2.dir_x;
            int dir_y_2 = s.calle2.dir_y;

            if ( (j + dir_x)==s.pos_x && (i + dir_y)==s.pos_y ) {
                if (s.calleCerrada.equals(s.calle1.nombre)){
                    n = 0;
                }
                else {
                    if (s.pausa) n = 2;
                    else n = 1;
                }
            }
            else if ( (j + dir_x_2)==s.pos_x && (i + dir_y_2)==s.pos_y ) {
                if (s.calleCerrada.equals(s.calle2.nombre)){
                    n = 0;
                }
                else {
                    if (s.pausa) n = 2;
                    else n = 1;
                }
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


        EntornoTickerBehaviour b = new EntornoTickerBehaviour(this, 300);
        this.addBehaviour(b);

    }
}
