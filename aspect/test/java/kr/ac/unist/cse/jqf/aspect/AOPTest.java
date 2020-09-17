package kr.ac.unist.cse.jqf.aspect;

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
        String[] args = new String[] { "-outjar", "tracing.jar",
                System.getProperty("user.dir") + "/src/main/aspectj/kr/ac/unist/cse/jqf/aspect/" + "Tracing.aj",
                "-outxmlfile", "aop.xml"
        };
        org.aspectj.tools.ajc.Main.main(args);
    }

}
