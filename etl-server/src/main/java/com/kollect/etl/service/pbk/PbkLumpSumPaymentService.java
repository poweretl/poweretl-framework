package com.kollect.etl.service.pbk;

import com.kollect.etl.service.IReadWriteServiceProvider;
import com.kollect.etl.service.app.BatchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PbkLumpSumPaymentService {
    private IReadWriteServiceProvider rwProvider;
    private List<String> dataSource;
    private boolean lock;
    private BatchHistoryService batchHistoryService;

    @Autowired
    public PbkLumpSumPaymentService(IReadWriteServiceProvider rwProvider,
                                    @Value("#{'${app.datasource_all}'.split(',')}") List<String> dataSource, BatchHistoryService batchHistoryService) {
        this.rwProvider = rwProvider;
        this.dataSource = dataSource;
        this.batchHistoryService = batchHistoryService;
    }

    public int combinedLumpSumPaymentService(@RequestParam Integer batch_id){
        int numberOfRows = -1;
        for (String aDataSource:dataSource){
            if (!lock) {
                long startTime = System.nanoTime();
                lock = true;
                List<Object> selectLumSumPaymentList = this.rwProvider.executeQuery(aDataSource, "getSumAmount", null);
                this.rwProvider.updateQuery(aDataSource, "truncateNetLumpSum", null);
                int numberOfRecords = selectLumSumPaymentList.size();
                for (Object aSelectLumSumPaymentList : selectLumSumPaymentList) {

                    Map<Object, Object> map = (Map<Object, Object>) aSelectLumSumPaymentList;
                    Map<Object, Object> args = new HashMap<>();
                    args.put("account_id", map.get("account_id"));
                    args.put("net_lump_sum_amount", map.get("net_lump_sum_amount"));
                    int updateCount = this.rwProvider.updateQuery(aDataSource, "updateGetSumAmount", args);
                    if (updateCount == 0) {
                        this.rwProvider.insertQuery(aDataSource, "insertGetSumAmount", args);
                    }
                }
                lock = false;
                numberOfRows = numberOfRecords;
                long endTime = System.nanoTime();
                long timeTaken = (endTime - startTime) / 1000000;
                this.batchHistoryService.runBatchHistory(batch_id, numberOfRows, timeTaken);
            }
        }
        System.out.println("LumpSumPayment - Number of rows updated: " + numberOfRows);
        return numberOfRows;
    }
}
