package kr.ac.unist.cse.jqf.fuzz.aop;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Ignore
public class AOPTest {

    @Test
    public void ajcHelpTest() throws IOException {
        String[] args = new String[] { "--help" };
        org.aspectj.tools.ajc.Main.main(args);
    }

    @Test
    public void ajcTest() throws IOException {
        String[] args = new String[] {"-outjar", "../aspect/tracing.jar",
                System.getProperty("user.dir") + "/src/test/java/kr/ac/unist/cse/jqf/fuzz/aop/" + "Tracing.aj",
                "-outxmlfile", "../aspect/aop.xml"
        };
        org.aspectj.tools.ajc.Main.main(args);
    }

}
