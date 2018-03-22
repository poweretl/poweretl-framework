package com.kollect.etl.service;

import com.kollect.etl.dataaccess.AgeInvoiceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgeInvoiceService {
    @Autowired
    private AgeInvoiceDao ageInvoiceDao;
    private boolean lock;

    public List<Object> getAgeInvoiceById(Object object) {
        return this.ageInvoiceDao.getAgeInvoiceById(object);
        // TODO Auto-generated method stub

    }

    public int updateAgeInvoice(Object object) {
        return this.ageInvoiceDao.updateAgeInvoice(object);
    }

    public int combinedAgeInvoiceService(@RequestParam(required = false) Integer tenant_id){
        int numberOfRows = -1;
        if (!lock) {
            lock = true;
            List<Object> ageInvoiceList = this.getAgeInvoiceById(tenant_id);
            int numberOfRecords = ageInvoiceList.size();
            for (int i = 0; i < numberOfRecords; i++) {
                Map<Object, Object> map = (Map<Object, Object>) ageInvoiceList.get(i);
                Map<Object, Object> args = new HashMap<>();
                args.put("invoice_due_date", map.get("invoice_due_date"));
                args.put("id", map.get("id"));
                args.put("tenant_id", tenant_id);
                this.updateAgeInvoice(args);
            }
            lock = false;
            numberOfRows = numberOfRecords;
        }
        System.out.println("AgeInvoice - Number of rows updated: " + numberOfRows);
        return numberOfRows;
    }

}
