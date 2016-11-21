package com.truckcompany.service;

import com.truckcompany.domain.*;
import com.truckcompany.domain.enums.CompanyStatus;
import com.truckcompany.repository.CheckpointRepository;
import com.truckcompany.repository.CompanyRepository;
import com.truckcompany.repository.UserRepository;
import com.truckcompany.security.SecurityUtils;
import com.truckcompany.web.rest.vm.ManagedCheckPointVM;
import com.truckcompany.web.rest.vm.ManagedRouteListVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CheckpointService {
    private final Logger log = LoggerFactory.getLogger(CheckpointService.class);

    @Inject
    private CheckpointRepository checkpointRepository;

    @Inject
    private UserRepository userRepository;

    @Inject WaybillService waybillService;

    public List<Checkpoint> getCheckpoints(){
        List<Waybill> waybill = waybillService.getWaybillForDriver();

        return checkpointRepository.findByRouteList(waybill.get(0).getRouteList());
    }

    public Checkpoint createCheckpoint(ManagedCheckPointVM checkPointVM){
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setName(checkPointVM.getName());
        checkpoint.setRouteList(checkPointVM.getRouteList());
        checkpointRepository.save(checkpoint);
        log.debug("Created Information for Checkpoint: {}", checkpoint);
        return checkpoint;
    }

    public void  updateCheckpointByManager(Long id,String name){
        checkpointRepository.findOneById(id).ifPresent(checkpoint -> {
            checkpoint.setName(name);
            log.debug("Changed Information for Checkpoint with id: {} by manager", checkpoint.getId());
        });
    }


    public void deleteCheckPoint(Long id){
        checkpointRepository.findOneById(id).ifPresent(checkpoint -> {
            checkpointRepository.delete(checkpoint);
            log.debug("Deleted Checkpoint: {}", checkpoint);
        });
    }


    public void deleteAllCheckpoint(RouteList routeList){
        checkpointRepository.deleteAllInBatchByRouteList(routeList);
        log.debug("Deleted all checkpoint routeList id: {}", routeList.getId());

    }

    public void markDate(Long id) {
        Checkpoint checkpoint = checkpointRepository.getOne(id);

        checkpoint.setCheckDate(new Timestamp(new Date().getTime()));

        checkpointRepository.save(checkpoint);

    }

    public List<Checkpoint> getCheckpointsByRouteListId(Long id) {
        List<Checkpoint> checkpoints = checkpointRepository.findByRouteListId(id);
        return checkpoints;
    }
}
