/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import java.util.Arrays;
import java.util.Optional;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 *
 * @author erik
 */
public enum GraphMaker {
    IN_MEMORY("In Memory") {
        @Override
        public Graph makeGraph() {
            Graph g = TinkerGraph.open();
            return g;
        }
    },
    JANUS("Janus (local file)") {
        @Override
        public Graph makeGraph() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    },
    CASSANDRA("Cassandra (network clustered)") {
        @Override
        public Graph makeGraph() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

    private final String english;

    private GraphMaker(String string) {
        this.english = string;
    }
    
    public String inEnglish(){
        return english;
    }
    
    public static GraphMaker fromText(String text){
        return Arrays.stream(values())
                .filter(v -> v.inEnglish().equals(text))
                .findAny()
                .orElse(null);
        
    }
    
    public abstract Graph makeGraph();
}
