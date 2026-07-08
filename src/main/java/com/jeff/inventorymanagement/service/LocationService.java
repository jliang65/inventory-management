package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.LocationDto;
import com.jeff.inventorymanagement.entity.Location;
import com.jeff.inventorymanagement.repository.LocationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LocationService {
    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public Location findById(Long id) {
        return locationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }

    public Location save(Location location) {
        if (locationRepository.existsByName(location.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Location name already exists");
        }
        return locationRepository.save(location);
    }

    public Location update(Long id, Location updatedLocation) {
        Location target = findById(id);

        if (!target.getName().equals(updatedLocation.getName())
                && locationRepository.existsByName(updatedLocation.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Location name already exists");
        }

        target.setName(updatedLocation.getName());
        target.setType(updatedLocation.getType());
        target.setAddress(updatedLocation.getAddress());
        target.setCity(updatedLocation.getCity());
        target.setState(updatedLocation.getState());
        target.setActive(updatedLocation.getActive());
        return locationRepository.save(target);
    }

    public void delete(Long id) {
        Location location = findById(id);
        locationRepository.delete(location);
    }

    public LocationDto toDto(Location location) {
        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setType(location.getType());
        dto.setAddress(location.getAddress());
        dto.setCity(location.getCity());
        dto.setState(location.getState());
        dto.setActive(location.getActive());
        return dto;
    }

    public Location toEntity(LocationDto dto) {
        Location location = new Location();
        location.setName(dto.getName());
        location.setType(dto.getType());
        location.setAddress(dto.getAddress());
        location.setCity(dto.getCity());
        location.setState(dto.getState());
        location.setActive(dto.getActive() != null ? dto.getActive() : true);
        return location;
    }

    public LocationDto findByIdAsDto(Long id) {
        return toDto(findById(id));
    }

    public LocationDto saveFromDto(LocationDto dto) {
        Location location = toEntity(dto);
        return toDto(save(location));
    }

    public List<LocationDto> findAllAsDto() {
        return findAll().stream()
            .map(this::toDto)
            .toList();
    }

    public LocationDto updateFromDto(Long id, LocationDto dto) {
        Location target = findById(id);

        if (!target.getName().equals(dto.getName())
                && locationRepository.existsByName(dto.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Location name already exists");
        }

        target.setName(dto.getName());
        target.setType(dto.getType());
        target.setAddress(dto.getAddress());
        target.setCity(dto.getCity());
        target.setState(dto.getState());
        if (dto.getActive() != null) {
            target.setActive(dto.getActive());
        }
        return toDto(locationRepository.save(target));
    }
}
