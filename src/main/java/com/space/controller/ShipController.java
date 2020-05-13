package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipCrudRepository;
import com.space.service.ShipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;

@RestController
@RequestMapping("/rest")
public class ShipController {
    private final ShipCrudRepository shipCrudRepository;
    private final EntityManager em;
    private final ShipService shipService;

    public ShipController(ShipCrudRepository shipCrudRepository, EntityManager em, ShipService shipService) {
        this.shipCrudRepository = shipCrudRepository;
        this.em = em;
        this.shipService = shipService;
    }

    @PostMapping("ships")
    public Ship createShip(@RequestBody CreateShipRequest request) {
        if (!request.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
        }

        Ship ship = new Ship();

        ship.setName(request.getName());
        ship.setPlanet(request.getPlanet());
        ship.setShipType(request.getShipType());
        ship.setProdDate(new Date(request.getProdDate()));
        ship.setSpeed(Math.round(request.getSpeed() * 100) / 100.0);
        ship.setCrewSize(request.getCrewSize());
        ship.setUsed(request.getUsed() != null && request.getUsed());
        ship.setRating(shipService.getRating(ship));
        shipCrudRepository.save(ship);
        return ship;
    }

    @PostMapping("ships/{id}")
    public Ship updateShip(@RequestBody CreateShipRequest request, @PathVariable String id) {
        Optional<Ship> shipOptional = shipCrudRepository.findById(getIdOrFail(id));

        if (!shipOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ship Not Found");
        }

        Ship ship = shipOptional.get();

        if (request.getName() != null) {
            if (!request.isNameValid()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
            }

            ship.setName(request.getName());
        }

        if (request.getPlanet() != null) {
            if (!request.isPlanetValid()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
            }

            ship.setPlanet(request.getPlanet());
        }

        if (request.getShipType() != null) {
            if (ship.getShipType() != request.getShipType()) {
                ship.setShipType(request.getShipType());
            }
        }

        if (request.getProdDate() != null) {
            if (!request.isDateValid()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
            }

            ship.setProdDate(new Date(request.getProdDate()));
        }

        if (request.getSpeed() != null) {
            if (!request.isSpeedValid()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
            }

            ship.setSpeed(Math.round(request.getSpeed() * 100) / 100.0);
        }

        if (request.getCrewSize() != null) {
            if (!request.isCrewValid()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
            }

            ship.setCrewSize(request.getCrewSize());
        }

        if (request.getUsed() != null) {
            ship.setUsed(request.getUsed());
        }

        ship.setRating(shipService.getRating(ship));
        shipCrudRepository.save(ship);
        return ship;
    }

    @GetMapping("ships/{id}")
    public Ship getShip(@PathVariable String id) {
        Optional<Ship> ship = shipCrudRepository.findById(getIdOrFail(id));

        if (ship.isPresent()) {
            return ship.get();
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ship Not Found");
    }

    @DeleteMapping("ships/{id}")
    public ResponseEntity<?> deleteShip(@PathVariable String id) {
        Optional<Ship> ship = shipCrudRepository.findById(getIdOrFail(id));

        if (ship.isPresent()) {
            shipCrudRepository.delete(ship.get());
            return ResponseEntity.ok().build();
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ship Not Found");
    }

    @GetMapping("ships")
    public List<Ship> getShips(@RequestParam Map<String, String> params) {
        ShipOrder order = ShipOrder.valueOf(params.getOrDefault("order", "ID"));
        int pageNumber = Integer.parseInt(params.getOrDefault("pageNumber", "0"));
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "3"));
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Ship> criteriaQuery = criteriaBuilder.createQuery(Ship.class);
        Root<Ship> root = criteriaQuery.from(Ship.class);
        CriteriaQuery<Ship> select = criteriaQuery.select(root);

        select.where(getPredicates(params, criteriaBuilder, root))
                .orderBy(criteriaBuilder.asc(root.get(order.getFieldName())));
        TypedQuery<Ship> typedQuery = em.createQuery(select);

        typedQuery.setFirstResult(pageNumber * pageSize);
        typedQuery.setMaxResults(pageSize);
        return typedQuery.getResultList();
    }

    @GetMapping("ships/count")
    public Long getShipsCount(@RequestParam Map<String, String> params) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Ship> root = countQuery.from(Ship.class);

        countQuery.select(criteriaBuilder.count(root));
        countQuery.where(getPredicates(params, criteriaBuilder, root));
        return em.createQuery(countQuery).getSingleResult();
    }

    protected Predicate[] getPredicates(Map<String, String> params, CriteriaBuilder criteriaBuilder, Root<Ship> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (params.containsKey("isUsed")) {
            Boolean isUsed = Boolean.parseBoolean(params.get("isUsed"));
            predicates.add(criteriaBuilder.equal(root.get("isUsed"), isUsed));
        }

        if (params.containsKey("shipType")) {
            ShipType shipType = ShipType.valueOf(params.get("shipType"));
            predicates.add(criteriaBuilder.equal(root.get("shipType"), shipType));
        }

        if (params.containsKey("name")) {
            String name = params.get("name");
            predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
        }

        if (params.containsKey("planet")) {
            String planet = params.get("planet");
            predicates.add(criteriaBuilder.like(root.get("planet"), "%" + planet + "%"));
        }

        if (params.containsKey("after")) {
            Calendar calendar = Calendar.getInstance();

            calendar.setTimeInMillis(Long.parseLong(params.get("after")));
            calendar.set(Calendar.DAY_OF_YEAR, 1);
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), calendar.getTime()));
        }

        if (params.containsKey("before")) {
            Calendar calendar = Calendar.getInstance();

            calendar.setTimeInMillis(Long.parseLong(params.get("before")));
            calendar.set(Calendar.DAY_OF_YEAR, 1);
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), calendar.getTime()));
        }

        if (params.containsKey("minCrewSize")) {
            Integer minCrew = Integer.parseInt(params.get("minCrewSize"));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("crewSize"), minCrew));
        }

        if (params.containsKey("maxCrewSize")) {
            Integer maxCrew = Integer.parseInt(params.get("maxCrewSize"));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("crewSize"), maxCrew));
        }

        if (params.containsKey("minSpeed")) {
            Double minSpeed = Double.parseDouble(params.get("minSpeed"));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("speed"), minSpeed));
        }

        if (params.containsKey("maxSpeed")) {
            Double maxSpeed = Double.parseDouble(params.get("maxSpeed"));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("speed"), maxSpeed));
        }

        if (params.containsKey("minRating")) {
            Double minRating = Double.parseDouble(params.get("minRating"));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating));
        }

        if (params.containsKey("maxRating")) {
            Double maxRating = Double.parseDouble(params.get("maxRating"));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("rating"), maxRating));
        }

        Predicate[] predicate = new Predicate[predicates.size()];
        return predicates.toArray(predicate);
    }

    private Long getIdOrFail(String id) {
        if (id.matches("\\d+")) {
            long _id = Long.parseLong(id);

            if (_id > 0) {
                return _id;
            }
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
    }
}
