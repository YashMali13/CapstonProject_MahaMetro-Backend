package com.aurionpro.app.service;

import com.aurionpro.app.dto.StationDetailResponse;
import com.aurionpro.app.dto.StationRequest;
import com.aurionpro.app.dto.StationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface StationService {
    StationResponse createStation(StationRequest request);
    StationResponse getActiveStationById(Long id);
    Page<StationResponse> listActiveStations(Pageable pageable);
    StationResponse updateStation(Long id, StationRequest request);
    void softDeleteStation(Long id);
    List<StationResponse> listInactiveStations();
    StationResponse restoreStation(Long id);
    void hardDeleteStation(Long id);
    List<StationDetailResponse> listActiveStationsWithRoutes();
}