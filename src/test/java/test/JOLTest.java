/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;
import org.openjdk.jol.vm.VirtualMachine;

/**
 *
 * @author ecostlow
 */
public class JOLTest {
    
    public static void main(String[] args){
        
        final String details = VM.current().details();
        final VirtualMachine vm = VM.current();
        System.out.println(details);
    }
}
