/**
 * DiffSplitter
 * Copyright (C) 2018 Red Hat, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.redhat.java.tools.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class DiffSplitter {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.err.println("Need patch!!");
            return;
        }
        
        List<String> patches = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        File patchFile = new File(args[0]);

        File destination = new File(patchFile.getParentFile().getCanonicalPath(), "split");
        if (!destination.exists()) {
            System.err.println("creating destination directory: " + destination);
            destination.mkdirs();
        } else {
            System.err.println("WARNING: destination directory exist " + destination);
            System.err.println("WARNING: patches will be overridden");
        }
        
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(patchFile))) {
            
            boolean firstDiff = true;
            StringBuffer patch = new StringBuffer();
            String line = null;
            
            // process each line
            System.err.println("processing file ... ");
            while ((line = bufferedReader.readLine()) != null) {
                
                if (line.startsWith("diff -r")) {
                    paths.add(line.split("\\s")[5]);
                    
                    if (!firstDiff) {
                        patches.add(patch.toString());
                        
                    } else {
                        firstDiff = false;
                    }
                    patch = new StringBuffer();
                }
                
                patch.append(line).append("\n");
            }
            
            // get last line in too
            if (!firstDiff) {
                patches.add(patch.toString());
            }
        }
        
        System.err.println("file processed, evaluating patches...");
        
        // for each section, find out the directory structure and put single
        // patches in the right place
        int i = 0;
        
        File patchList = new File(destination.getCanonicalPath() + File.separator + "patch-list.txt");
        for (String patch : patches) {
            
            String currentPath = paths.get(i);
            System.err.println(currentPath);

            Files.write(patchList.toPath(), (currentPath + "\n").getBytes("utf-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            
            File currentPatch = new File(destination.getCanonicalPath() + File.separator + currentPath + ".patch");
            File parent = currentPatch.getParentFile();
            parent.mkdirs();
            
            Files.write(currentPatch.toPath(), patch.getBytes("utf-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(new File(parent, parent.getName() + "-full.patch").toPath(), patch.getBytes("utf-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            
            i++;
        }
        
        System.err.println("done!");
    }
}
