package com.truckcompany.service;

import com.truckcompany.domain.enums.WaybillState;
import com.truckcompany.service.dto.RouteListDTO;
import com.truckcompany.service.dto.WaybillDTO;
import com.truckcompany.service.facade.RouteListFacade;
import com.truckcompany.service.facade.WaybillFacade;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Vlad Momotov on 24.11.2016.
 */

@Service
@Transactional
public class CompanyOwnerStatisticsService {
    private final Logger log = LoggerFactory.getLogger(CompanyOwnerStatisticsService.class);

    @Inject
    private RouteListFacade routeListFacade;

    @Inject
    private WaybillFacade waybillFacade;

    /*@Inject
    private Goods*/

    public List<List<Long>> getConsumptionStatistics(){
        List<RouteListDTO> routeListDTOs = routeListFacade.findRouteLists();

        Map<Long, Long> map = routeListDTOs.stream()
            .collect(Collectors.toMap(s-> s.getCreationDate().toInstant().toEpochMilli(),
                s-> s.getTruck().getConsumption()*s.getFuelCost()*s.getDistance(),
                (a,b) -> a+ b));

        List<List<Long>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : map.entrySet()){
            ArrayList<Long> list = new ArrayList<>();
            list.add(entry.getKey());
            list.add(entry.getValue());
            result.add(list);
        }

        Collections.sort(result, (o1, o2) -> o1.get(0).compareTo(o2.get(0)));
        return result;
    }

    public List<List<Long>> getConsumptionStatistics(ZonedDateTime fromDate, ZonedDateTime toDate){
        List<RouteListDTO> routeListDTOs = routeListFacade.findRouteLists(fromDate, toDate);

        Map<Long, Long> map = routeListDTOs.stream()
            .collect(Collectors.toMap(s-> s.getCreationDate().toInstant().toEpochMilli(),
                s-> s.getTruck().getConsumption()*s.getFuelCost()*s.getDistance(),
                (a,b) -> a+ b));

        List<List<Long>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : map.entrySet()){
            ArrayList<Long> list = new ArrayList<>();
            list.add(entry.getKey());
            list.add(entry.getValue());
            result.add(list);
        }

        Collections.sort(result, (o1, o2) -> o1.get(0).compareTo(o2.get(0)));
        return result;
    }

    public List<List<Long>> getLossStatistics(){
        List<WaybillDTO> waybills = waybillFacade.findWaybillsWithState(WaybillState.DELIVERED);

       /* waybills.stream()
            .map(WaybillDTO::getGo)*/
        //List<RouteListDTO> routeListDTOs = routeListFacade.findRouteLists();
/*
        Map<Long, Long> map = routeListDTOs.stream()
            .collect(Collectors.toMap(s-> s.getCreationDate().toInstant().toEpochMilli(),
                s-> s.getTruck().getConsumption()*s.getFuelCost()*s.getDistance(),
                (a,b) -> a+ b));

        List<List<Long>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : map.entrySet()){
            ArrayList<Long> list = new ArrayList<>();
            list.add(entry.getKey());
            list.add(entry.getValue());
            result.add(list);
        }

        Collections.sort(result, (o1, o2) -> o1.get(0).compareTo(o2.get(0)));
        return result;*/
        return null;
    }

    public List<List<Long>> getLossStatistics(ZonedDateTime fromDate, ZonedDateTime toDate){
        /*List<RouteListDTO> routeListDTOs = routeListFacade.findRouteLists(fromDate, toDate);

        Map<Long, Long> map = routeListDTOs.stream()
            .collect(Collectors.toMap(s-> s.getCreationDate().toInstant().toEpochMilli(),
                s-> s.getTruck().getConsumption()*s.getFuelCost()*s.getDistance(),
                (a,b) -> a+ b));

        List<List<Long>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : map.entrySet()){
            ArrayList<Long> list = new ArrayList<>();
            list.add(entry.getKey());
            list.add(entry.getValue());
            result.add(list);
        }

        Collections.sort(result, (o1, o2) -> o1.get(0).compareTo(o2.get(0)));
        return result;*/
        return null;
    }

    public HSSFWorkbook getConsumptionReport(){
        List<List<Long>> statistics = getConsumptionStatistics();
        return createConsumptionReport(statistics);

    }

    public HSSFWorkbook getConsumptionReport(ZonedDateTime fromDate, ZonedDateTime toDate){
        List<List<Long>> statistics = getConsumptionStatistics(fromDate, toDate);
        return createConsumptionReport(statistics);
    }



    public HSSFWorkbook getRouteListsReport(ZonedDateTime fromDate, ZonedDateTime toDate){
        List<WaybillDTO> waybills = waybillFacade.findWaybillsWithRouteListCreationDateBetween(fromDate, toDate);

        HSSFWorkbook book = new HSSFWorkbook() ;
        Sheet sheet = book.createSheet("report");

        DataFormat dataFormat = book.createDataFormat();
        CellStyle dateStyle = book.createCellStyle();
        dateStyle.setDataFormat(dataFormat.getFormat("dd.mm.yyyy"));

        createHeaderForRouteListReport(sheet);

        for (int i=0; i<waybills.size(); ++i) {
            Row row = sheet.createRow(i+1);
            fillRowForRouteListReport(row, dateStyle, waybills.get(i));
        }

        setAutoSizeForRouteListReport(sheet, new int[]{1,2, 3,4,5,6,7});

        return book;
    }

    public HSSFWorkbook getRouteListsReport(){
        List<WaybillDTO> waybills = waybillFacade.findWaybills();

        HSSFWorkbook book = new HSSFWorkbook() ;
        Sheet sheet = book.createSheet("routelist report");

        DataFormat dataFormat = book.createDataFormat();
        CellStyle dateStyle = book.createCellStyle();
        dateStyle.setDataFormat(dataFormat.getFormat("dd.mm.yyyy"));

        createHeaderForRouteListReport(sheet);

        for (int i=0; i<waybills.size(); ++i) {
            Row row = sheet.createRow(i+1);
            fillRowForRouteListReport(row, dateStyle, waybills.get(i));
        }

        setAutoSizeForRouteListReport(sheet, new int[]{1,2, 3,4,5,6,7});

        return book;
    }

    private void createHeaderForRouteListReport(Sheet sheet){
        Row header = sheet.createRow(0);

        Cell id = header.createCell(0);
        id.setCellValue("ID");

        Cell creationDate = header.createCell(1);
        creationDate.setCellValue("Creation date");


        Cell leavingDate = header.createCell(2);
        leavingDate.setCellValue("Leaving date");

        Cell arrivalDate = header.createCell(3);
        arrivalDate.setCellValue("Arrival date");

        Cell truckNumber = header.createCell(4);
        truckNumber.setCellValue("Truck number");

        Cell leavingStorage = header.createCell(5);
        leavingStorage.setCellValue("Leaving storage");

        Cell arrivalStorage = header.createCell(6);
        arrivalStorage.setCellValue("Arrival storage");

        Cell state = header.createCell(7);
        state.setCellValue("State");

        Cell fuelCost = header.createCell(8);
        fuelCost.setCellValue("Fuel cost");

        Cell distance = header.createCell(9);
        distance.setCellValue("Distance");

        Cell waybillID = header.createCell(10);
        waybillID.setCellValue("Waybill ID");

    }

    private void fillRowForRouteListReport(Row row, CellStyle dateStyle,WaybillDTO waybill){
        Cell id = row.createCell(0);
        id.setCellValue(waybill.getRouteList().getId());

        Cell creationDate = row.createCell(1);
        creationDate.setCellStyle(dateStyle);
        creationDate.setCellValue(GregorianCalendar.from(waybill.getRouteList().getCreationDate()));

        Cell leavingDate = row.createCell(2);
        leavingDate.setCellStyle(dateStyle);
        leavingDate.setCellValue(GregorianCalendar.from(waybill.getRouteList().getLeavingDate()));

        Cell arrivalDate = row.createCell(3);
        arrivalDate.setCellStyle(dateStyle);
        arrivalDate.setCellValue(GregorianCalendar.from(waybill.getRouteList().getArrivalDate()));

        Cell truckNumber = row.createCell(4);
        truckNumber.setCellValue(waybill.getRouteList().getTruck().getRegNumber());

        Cell leavingStorage = row.createCell(5);
        leavingStorage.setCellValue(waybill.getRouteList().getLeavingStorage().getName());

        Cell arrivalStorage = row.createCell(6);
        arrivalStorage.setCellValue(waybill.getRouteList().getArrivalStorage().getName());

        Cell state = row.createCell(7);
        state.setCellValue(waybill.getRouteList().getState());

        Cell fuelCost = row.createCell(8);
        fuelCost.setCellValue(waybill.getRouteList().getFuelCost());

        Cell distance = row.createCell(9);
        distance.setCellValue(waybill.getRouteList().getDistance());

        Cell waybillID = row.createCell(10);
        waybillID.setCellValue(waybill.getId());
    }

    private void setAutoSizeForRouteListReport(Sheet sheet, int[] columnNumbers){
        for (int i=0; i<columnNumbers.length; ++i){
            sheet.autoSizeColumn(columnNumbers[i]);
        }
    }

    private HSSFWorkbook createConsumptionReport(List<List<Long>> statistics){
        HSSFWorkbook book = new HSSFWorkbook();
        Sheet sheet = book.createSheet("consumption report");

        DataFormat dataFormat = book.createDataFormat();
        CellStyle dateStyle = book.createCellStyle();
        dateStyle.setDataFormat(dataFormat.getFormat("dd.mm.yyyy"));

        Row header = sheet.createRow(0);

        Cell date = header.createCell(0);
        date.setCellValue("Date");

        Cell value = header.createCell(1);
        value.setCellValue("Consumption amount");

        int index = 1;

        for (List<Long> record : statistics){
            Row row = sheet.createRow(index++);

            Cell dateCell = row.createCell(0);
            dateCell.setCellStyle(dateStyle);
            dateCell.setCellValue(GregorianCalendar.from(Instant.ofEpochMilli(record.get(0)).atZone(ZoneOffset.UTC)));

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(record.get(1));
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        return book;

    }

}
