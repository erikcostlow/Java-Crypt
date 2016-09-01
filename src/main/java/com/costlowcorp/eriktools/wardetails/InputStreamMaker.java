/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author ecostlow
 */
public interface InputStreamMaker {
    public InputStream make() throws IOException;
}
