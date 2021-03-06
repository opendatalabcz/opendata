package eu.profinit.opendata;

import eu.profinit.opendata.control.ExtractionService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by dm on 2/4/16.
 */
public class Main {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");

        ExtractionService extractionService = context.getBean(ExtractionService.class);
        extractionService.runExtraction();
        context.close();
    }
}
