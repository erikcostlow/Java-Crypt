/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.back;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author ecostlow
 */
public interface ArchiveWalkerRecipient {
    public void accept(List<String> archiveNames, ZipEntry currentEntry, ZipInputStream zin);
}
