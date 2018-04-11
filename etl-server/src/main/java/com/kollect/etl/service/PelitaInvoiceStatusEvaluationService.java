package com.kollect.etl.service;

import com.kollect.etl.config.CrudProcessHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Service
public class PelitaInvoiceStatusEvaluationService {


    private IReadWriteServiceProvider rwProvider;
    private String dataSource;
    private BatchHistoryService batchHistoryService;
    private IAsyncExecutorService executorService;
    private boolean lock;

    @Autowired
    public PelitaInvoiceStatusEvaluationService(IReadWriteServiceProvider rwProvider, @Value("${app.datasource_pelita_test}") String dataSource, BatchHistoryService batchHistoryService, @Qualifier("simple") IAsyncExecutorService executorService){
        this.rwProvider = rwProvider;
        this.dataSource = dataSource;
        this.batchHistoryService = batchHistoryService;
        this.executorService = executorService;
    }

    public List<Object> getInvoiceStatusIdById(Object object) {
        return this.rwProvider.executeQuery(dataSource, "getInvoiceStatusById", object);
        // TODO Auto-generated method stub

    }


    public int combinePelitaInvoiceStatusEvaluation(@RequestParam(required = false) Integer tenant_id, @RequestParam Integer batch_id) {
        int numberOfRows = -1;
        if (!lock) {
            long startTime = System.nanoTime();
            lock = true;
            List<Object> statusIdList = this.getInvoiceStatusIdById(tenant_id);
            Map<String, CrudProcessHolder> map = new TreeMap<>();
            map.put("STATUS_ID", new CrudProcessHolder("NONE", 10, 100, new ArrayList<>(Arrays.asList("updateInvoiceStatusEvaluation"))));
            executorService.processEntries(map, statusIdList);
            int numberOfRecords = statusIdList.size();
            lock = false;
            numberOfRows = numberOfRecords;
            long endTime = System.nanoTime();
            long timeTaken = (endTime - startTime) / 1000000;
            this.batchHistoryService.runBatchHistory(batch_id, numberOfRows, timeTaken);
        }
        return numberOfRows;
    }

}
