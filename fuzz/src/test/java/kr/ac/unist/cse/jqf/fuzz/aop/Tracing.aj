package kr.ac.unist.cse.jqf.fuzz.aop;
import com.thoughtworks.xstream.XStream;
import edu.berkeley.cs.jqf.fuzz.util.TargetCoverage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public aspect Tracing {
    private pointcut mainMethod () :
        execution(* *(..));

    after () : mainMethod() {

        if(TargetCoverage.getTargetCoverage().getCoveredTargets().size()>0) {
            XStream stream = new XStream();
            if(thisJoinPoint.toString().contains("inverseCumulativeProbability")) {
                String xml = stream.toXML(thisJoinPoint);
                //                String logDir = System.getProperty("jqf.ei.logDir");
//                String inputID = System.getProperty("jqf.ei.inputID");
//                Path dir;
//                if (inputID == null)
//                    dir = Paths.get(logDir);
//                else
//                    dir = Paths.get(logDir, inputID);
//                try {
//                    Files.createDirectories(dir);
//                } catch (IOException e) {
//                    System.err.println("Failed to create directory " + dir);
//                    e.printStackTrace();
//                }
//                List<Path> files = new ArrayList<>();
//                files.add(Paths.get(dir.toString(), "dump.xml"));
//
//                for (Path file : files) {
//                    try {
//                        Files.deleteIfExists(file);
//                    } catch (IOException e) {
//                        System.err.println("Failed to delete " + file);
//                    }
//
//                    try {
//                        Files.createFile(file);
//                    } catch (IOException e) {
//                        System.err.println("Failed to create " + file);
//                    }
//                }
//                Path outFile;
//                if (inputID == null) {
//                    outFile = Paths.get(logDir, "dump.xml");
//                } else {
//                    outFile = Paths.get(logDir, inputID, "dump.xml");
//                }
//                try {
//                    Files.write(outFile, xml.getBytes(),
//                            StandardOpenOption.APPEND);
//                } catch (IOException e) {
//                    System.err.println("Failed to write output due to IOException");
//                }


               System.out.println(xml);
            }
            //System.out.println("< After mainMethod()" + thisJoinPoint);
        }
    }
}