package plogo.plogoserver.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import plogo.plogoserver.service.BatchService;

@Component
@RequiredArgsConstructor
public class BatchInitializer implements CommandLineRunner {

    private final BatchService batchService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Batch Job Started...");
        batchService.runSaveAll();
        System.out.println("✅ Batch Job Completed!");
    }
}

