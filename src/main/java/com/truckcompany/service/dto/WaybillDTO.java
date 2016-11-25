package com.truckcompany.service.dto;

import com.truckcompany.domain.Waybill;
import com.truckcompany.domain.enums.WaybillState;

import java.time.ZonedDateTime;


/**
 * A DTO representing a Waybill.
 * Created by Viktor Dobroselsky.
 */
public class WaybillDTO {
    private Long id;

    private ZonedDateTime date;

    private RouteListDTO routeList;

    private UserDTO driver;

    private WaybillState state;

    private UserDTO dispatcher;

    private UserDTO manager;

    private ZonedDateTime dateChecked;

    private OfferDTO offer;

    public WaybillDTO(Long id, ZonedDateTime date, UserDTO driver, WaybillState state, UserDTO dispatcher, UserDTO manager, ZonedDateTime dateChecked) {
        this.id = id;
        this.date = date;
        this.driver = driver;
        this.state = state;
        this.dispatcher = dispatcher;
        this.manager = manager;
        this.dateChecked = dateChecked;
    }

    public WaybillDTO(Long id, ZonedDateTime date, UserDTO driver, WaybillState state, UserDTO dispatcher) {
        this.id = id;
        this.date = date;
        this.driver = driver;
        this.state = state;
        this.dispatcher = dispatcher;
    }

    public WaybillDTO() {
    }

    public WaybillDTO(Long id, ZonedDateTime date, WaybillState state) {
        this.id = id;
        this.date = date;
        this.state = state;
    }


    public WaybillDTO(Waybill waybill, RouteListDTO routeList) {
        this(waybill);
        this.routeList = routeList;
    }


    public WaybillDTO(Waybill waybill) {
        this(waybill.getId(), waybill.getDate(), waybill.getState());
        this.dispatcher = waybill.getDispatcher() != null ? new UserDTO(waybill.getDispatcher()) : null;
        this.driver = waybill.getDriver() != null ? new UserDTO(waybill.getDriver()) : null;
        this.routeList = new RouteListDTO(waybill.getRouteList());
        this.manager = waybill.getManager() == null ? null : new UserDTO(waybill.getManager());
        this.dateChecked = waybill.getDateChecked();

        //this.offer = new OfferDTO(waybill.getOffer);
    }

    public WaybillDTO(WaybillDTO waybill) {
        this(waybill.getId(), waybill.getDate(), waybill.getState());
        this.dispatcher = waybill.getDispatcher();
        this.driver = waybill.getDriver();
        this.routeList = waybill.getRouteList();
        this.offer = waybill.getOffer();
    }

    public RouteListDTO getRouteList() {
        return routeList;
    }

    public void setRouteList(RouteListDTO routeList) {
        this.routeList = routeList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public UserDTO getDriver() {
        return driver;
    }

    public void setDriver(UserDTO driver) {
        this.driver = driver;
    }

    public WaybillState getState() {
        return state;
    }

    public void setState(WaybillState state) {
        this.state = state;
    }

    public UserDTO getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(UserDTO dispatcher) {
        this.dispatcher = dispatcher;
    }

    public UserDTO getManager() {
        return manager;
    }

    public void setManager(UserDTO manager) {
        this.manager = manager;
    }

    public ZonedDateTime getDateChecked() {
        return dateChecked;
    }

    public void setDateChecked(ZonedDateTime dateChecked) {
        this.dateChecked = dateChecked;
    }

    public OfferDTO getOffer() {
        return offer;
    }

    public void setOffer(OfferDTO offer) {
        this.offer = offer;
    }



    @Override
    public String toString() {
        return "WaybillDTO{" +
            "id=" + id +
            ", date=" + date +
            ", routeList=" + routeList +
            ", driver=" + driver +
            ", state=" + state +
            ", dispatcher=" + dispatcher +
            ", offer=" + offer +
            '}';
    }
}
