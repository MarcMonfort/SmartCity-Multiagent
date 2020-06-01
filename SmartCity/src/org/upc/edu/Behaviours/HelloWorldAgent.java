/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.upc.edu.Behaviours;


/**
 * @author igomez
 */

import jade.core.Agent;

public class HelloWorldAgent extends Agent {

    protected void setup() {

        String arguments = "";

        Object[] args = getArguments();
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                arguments = arguments + " - " + args[i];
            }
        }


        System.out.println("Hallo World! my name is " + getAID().getName() + " and my arguments are " + arguments);

        doDelete(); //same as kill in GUI
    }

    protected void takeDown() {
        System.out.println("Bye...");
    }

}

