package com.truckcompany.service.facade;

import com.truckcompany.domain.User;
import com.truckcompany.domain.Waybill;
import com.truckcompany.domain.enums.WaybillState;
import com.truckcompany.security.SecurityUtils;
import com.truckcompany.service.UserService;
import com.truckcompany.service.WaybillService;
import com.truckcompany.service.dto.WaybillDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.truckcompany.security.SecurityUtils.isCurrentUserInRole;
import static java.util.Collections.emptyList;

@Component
@Transactional
public class DefaultWaybillFacade implements WaybillFacade {
    private final Logger log = LoggerFactory.getLogger(DefaultWaybillFacade.class);

    @Inject
    private UserService userService;

    @Inject
    private WaybillService waybillService;


    @Override
    public List<WaybillDTO> findWaybills() {

        Optional<User> optionalUser = userService.getUserByLogin(SecurityUtils.getCurrentUserLogin());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            log.debug("Get all waybills for user \'{}\'", user.getLogin());
            List<WaybillDTO> waybills = emptyList();
            if (isCurrentUserInRole("ROLE_DRIVER")) {
                waybills = waybillService.getWaybillByDriver(user)
                    .stream()
                    .map(WaybillDTO::new)
                    .collect(Collectors.toList());
                for (int index = 0; index < waybills.size(); index++) {
                    if(waybills.get(index).getState().equals(WaybillState.CHECKED) ||
                        (waybills.get(index).getState().equals(WaybillState.DELIVERED) &&
                        waybills.get(index).getRouteList().getState().equals("TRANSPORTATION"))) {
                        while (index < waybills.size()-1) {
                            if( waybills.get(index + 1).getState().equals(WaybillState.CHECKED)) {
                                waybills.remove(index + 1);
                            }else {
                                index++;
                            }
                        }
                    }
                }

            } else if (isCurrentUserInRole("ROLE_COMPANYOWNER") || isCurrentUserInRole("ROLE_MANAGER") || isCurrentUserInRole("ROLE_DISPATCHER")) {
                waybills = waybillService.getWaybillByCompany(user.getCompany())
                    .stream()
                    .map(WaybillDTO::new)
                    .collect(Collectors.toList());
            }
            return waybills;
        } else {
            return emptyList();
        }
    }

    @Override
    public List<WaybillDTO> findWaybillsWithRouteListCreationDateBetween(ZonedDateTime fromDate, ZonedDateTime toDate) {
        Optional<User> optionalUser = userService.getUserByLogin(SecurityUtils.getCurrentUserLogin());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            log.debug("Get all waybills for user \'{}\'", user.getLogin());
            List<WaybillDTO> waybills = emptyList();
            if (isCurrentUserInRole("ROLE_COMPANYOWNER")) {
                waybills = waybillService.getWaybillByCompanyAndRouteListCreationDateBetween(user.getCompany(), fromDate, toDate)
                    .stream()
                    .map(WaybillDTO::new)
                    .collect(Collectors.toList());
            }

            return waybills;
        } else {
            return emptyList();
        }
    }

    @Override
    public Page<WaybillDTO> findWaybillsWithRouteListCreationDateBetween(Pageable pageable, ZonedDateTime fromDate, ZonedDateTime toDate) {
        Page<Waybill> pageWaybills = new PageImpl<>(emptyList());

        Optional<User> optionalUser = userService.getUserByLogin(SecurityUtils.getCurrentUserLogin());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            log.debug("Get all waybill for user \'{}\'", user.getLogin());

            if (isCurrentUserInRole("ROLE_COMPANYOWNER")) {
                pageWaybills = waybillService
                    .getPageWaybillsByCompanyAndRouteListCreationDateBetween(pageable, user.getCompany(), fromDate, toDate);
            }
        }
        return new PageImpl<>(pageWaybills.getContent().stream()
            .map(WaybillDTO::new)
            .collect(Collectors.toList()), pageable, pageWaybills.getTotalElements());
    }

    @Override
    public List<WaybillDTO> findWaybillsWithState(WaybillState state) {
        Optional<User> optionalUser = userService.getUserByLogin(SecurityUtils.getCurrentUserLogin());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            log.debug("Get waybills with state " + state.toString() + " for user \'{}\'", user.getLogin());
            List<WaybillDTO> waybills = emptyList();
            if (isCurrentUserInRole("ROLE_COMPANYOWNER")) {
                waybills = waybillService.getWaybillByCompanyAndState(user.getCompany(), state)
                    .stream()
                    .map(WaybillDTO::new)
                    .collect(Collectors.toList());
            }

            return waybills;
        } else {
            return emptyList();
        }
    }

    @Override
    public List<WaybillDTO> findWaybillsWithStateAndDateBetween(WaybillState state, ZonedDateTime fromDate, ZonedDateTime toDate) {
        Optional<User> optionalUser = userService.getUserByLogin(SecurityUtils.getCurrentUserLogin());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            log.debug("Get waybills with state {}, date between {} and {} for user \'{}\'", state.toString(), fromDate,
                toDate, user.getLogin());
            List<WaybillDTO> waybills = emptyList();
            if (isCurrentUserInRole("ROLE_COMPANYOWNER")) {
                waybills = waybillService.getWaybillByCompanyAndStateAndDateBetween(user.getCompany(), state,
                    fromDate, toDate)
                    .stream()
                    .map(WaybillDTO::new)
                    .collect(Collectors.toList());
            }

            return waybills;
        } else {
            return emptyList();
        }
    }

    @Override
    public Page<WaybillDTO> findWaybillWithStolenGoods(Pageable page) {
        Page<Waybill> pageWaybills = new PageImpl<>(emptyList());

        Optional<User> optionalUser = userService.getUserByLogin(SecurityUtils.getCurrentUserLogin());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            log.debug("Get all waybills with stolen goods for user \'{}\'", user.getLogin());
            if (isCurrentUserInRole("ROLE_COMPANYOWNER")) {
                pageWaybills = waybillService.getPageWaybillByCompanyAndWithStolenGoods(page, user.getCompany());
            }

        }
        return new PageImpl<>(pageWaybills.getContent()
            .stream()
            .map(WaybillDTO::new)
            .collect(Collectors.toList()), page, pageWaybills.getTotalElements());
    }


    @Override
    public Page<WaybillDTO> findWaybills(Pageable pageable) {
        Page<Waybill> pageWaybills = new PageImpl<>(emptyList());

        Optional<User> optionalUser = userService.getUserByLogin(SecurityUtils.getCurrentUserLogin());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            log.debug("Get all waybills for user \'{}\'", user.getLogin());
            if (isCurrentUserInRole("ROLE_COMPANYOWNER")) {
                pageWaybills = waybillService.getPageWaybillByCompany(pageable, user.getCompany());
            } else if (isCurrentUserInRole("ROLE_DISPATCHER")) {
                pageWaybills = waybillService.getPageWaybillByDispatcher(pageable, user);
            }
        }

        return new PageImpl<>(pageWaybills.getContent()
            .stream()
            .map(WaybillDTO::new)
            .collect(Collectors.toList()), pageable, pageWaybills.getTotalElements());
    }
}
