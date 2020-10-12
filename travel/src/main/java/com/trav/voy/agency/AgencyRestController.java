package com.trav.voy.agency;

import com.trav.voy.ville.Ville;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin("*")
@RestController
public class AgencyRestController {

    public static final Logger LOGGER = LoggerFactory.getLogger(AgencyRestController.class);

    @Autowired
    private AgencyServiceImpl agencyService;

    @GetMapping("/allAgencys")
    public ResponseEntity<List<AgencyDTO>> getAllAgencys() {
        List<Agency> agencys = agencyService.getAgencysList();
//        if (!CollectionUtils.isEmpty(agencys.toList())) {
        if (!agencys.isEmpty()) {
            List<AgencyDTO> custumerDTOs = agencys.stream().map(custumer -> {
                return mapAgencyToAgencyDTO(custumer);
            }).collect(Collectors.toList());
            return new ResponseEntity<List<AgencyDTO>>(custumerDTOs, HttpStatus.OK);
        }
        return new ResponseEntity<List<AgencyDTO>>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/allAgencyByVille")
    public ResponseEntity<List<AgencyDTO>> getAllAgencyByVille(@RequestParam("villeID") String villeID) {
        int idVille = 0;
        if (villeID != null) {
            idVille = Integer.parseInt(villeID);
        }
        List<Agency> agencys = (List<Agency>) agencyService.findAllAgencyByVilleId(idVille);
        if (!CollectionUtils.isEmpty(agencyService.findAllAgencyByVilleId(idVille))) {
            List<AgencyDTO> agencyDTOs = agencys.stream().map(agency -> {
                return mapAgencyToAgencyDTO(agency);
            }).collect(Collectors.toList());
            return new ResponseEntity<>(agencyDTOs, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Ajoute un nouveau client dans la base de donnée H2. Si le client existe
     * déjà, on retourne un code indiquant que la création n'a pas abouti.
     *
     * @param agencyDTORequest
     * @return
     */
    @PostMapping("/addAgency")
    public ResponseEntity<AgencyDTO> createNewAgency(@RequestBody AgencyDTO agencyDTORequest) {
        Agency existingAgency = agencyService.findAgencyByName(agencyDTORequest.getName());
        if (existingAgency != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Agency customerRequest = mapAgencyDTOToAgency(agencyDTORequest);
        if (!customerRequest.getPhoto().equals("") && customerRequest.getPhoto() != null) {
            String photoName[] = customerRequest.getPhoto().split("\\\\");
            customerRequest.setPhoto(photoName[photoName.length - 1]);
        }
        customerRequest.setCreationDate(LocalDate.now());
        Agency customerResponse = agencyService.saveAgency(customerRequest);
        if (customerResponse != null) {
            AgencyDTO customerDTO = mapAgencyToAgencyDTO(customerResponse);
            return new ResponseEntity<>(customerDTO, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);

    }

    /**
     * Met à jour les données d'un client dans la base de donnée H2. Si le
     * client n'est pas retrouvé, on retourne un code indiquant que la mise à
     * jour n'a pas abouti.
     *
     * @param customerDTORequest
     * @return
     */
    @PutMapping("/updateAgency")
    public ResponseEntity<AgencyDTO> updateAgency(@RequestBody AgencyDTO customerDTORequest) {
        //, UriComponentsBuilder uriComponentBuilder
        if (!agencyService.checkIfIdexists(customerDTORequest.getId())) {
            return new ResponseEntity<AgencyDTO>(HttpStatus.NOT_FOUND);
        }
        Agency customerRequest = mapAgencyDTOToAgency(customerDTORequest);
        Agency customerResponse = agencyService.updateAgency(customerRequest);
        if (customerResponse != null) {
            AgencyDTO customerDTO = mapAgencyToAgencyDTO(customerResponse);
            return new ResponseEntity<AgencyDTO>(customerDTO, HttpStatus.OK);
        }
        return new ResponseEntity<AgencyDTO>(HttpStatus.NOT_MODIFIED);
    }

//	/**
//	 * Supprime un client dans la base de donnée H2. Si le client n'est pas retrouvé, on retourne le Statut HTTP NO_CONTENT.
//	 * @param customerId
//	 * @return
//	 */
    @DeleteMapping("/deleteAgency/{customerId}")
//    @ApiOperation(value = "Delete a customer in the Library, if the customer does not exist, nothing is done", response = String.class)
//    @ApiResponse(code = 204, message = "No Content: customer sucessfully deleted")
    public ResponseEntity<String> deleteAgency(@PathVariable Integer customerId) {
        agencyService.deleteAgency(customerId);
        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }
//

    @GetMapping("/paginatedSearch")
    public ResponseEntity<List<AgencyDTO>> searchAgencys(@RequestParam("beginPage") int beginPage,
            @RequestParam("endPage") int endPage) {
        //, UriComponentsBuilder uriComponentBuilder
        Page<Agency> customers = agencyService.getPaginatedAgencysList(beginPage, endPage);
        if (customers != null) {
            List<AgencyDTO> customerDTOs = customers.stream().map(customer -> {
                return mapAgencyToAgencyDTO(customer);
            }).collect(Collectors.toList());
            return new ResponseEntity<List<AgencyDTO>>(customerDTOs, HttpStatus.OK);
        }
        return new ResponseEntity<List<AgencyDTO>>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/saveAgencyVilleLignes")
    public ResponseEntity<Ville> saveAgencyVilleLignes(@RequestBody ItineraireForm itineraireForm) {
//        System.out.println("HHHHHHHHHHHHH " + itineraireForm);
        if (itineraireForm != null) {
            try {
//                List<Agency>agencys=(List<Agency>) agencyService.findAgencyByName(itineraireForm.getAgency().getName());
                itineraireForm.getVilleNames().forEach(name -> {
                    agencyService.saveAgencyVilleLignes(new AgencyVilleLigne(null, name, itineraireForm.getAgency()));
                });
                return new ResponseEntity<>(HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);

    }

    @GetMapping("/itineraires")
    public ResponseEntity<List<AgencyVilleLigne>> itineraires(@RequestParam("agencyId") String agencyId) {
        System.out.println("MMMMMMMMMMMMMMMMMMMMM " + agencyId);
        int id = 0;
        if (agencyId != null) {
            id = Integer.parseInt(agencyId);
            List<AgencyVilleLigne> agencyVilleLignes = (List<AgencyVilleLigne>) agencyService.findItineraireAgence(id);
//            System.out.println("QQQQQQQQQQQQQQQQQQQQQ "+agencyVilleLignes);
            if (!CollectionUtils.isEmpty(agencyVilleLignes)) {
                agencyVilleLignes.removeAll(Collections.singleton(null));
                return new ResponseEntity<>(agencyVilleLignes, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private AgencyDTO mapAgencyToAgencyDTO(Agency customer) {
        ModelMapper mapper = new ModelMapper();
        AgencyDTO agencyDTO = mapper.map(customer, AgencyDTO.class);
        return agencyDTO;
    }

    /**
     * Transforme un POJO AgencyDTO en en entity Agency
     *
     * @param agencyDTO
     * @return
     */
    private Agency mapAgencyDTOToAgency(AgencyDTO agencyDTO) {
        ModelMapper mapper = new ModelMapper();
        Agency agency = mapper.map(agencyDTO, Agency.class);
        return agency;
    }

    @GetMapping(value = "/images/{name}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] image(@PathVariable(name = "name") String name) throws IOException {
//        Bus bus = busDao.findById(id).get();
//        String photoName = bus.getPhoto();
        File file = new File(System.getProperty("user.home") + "/travel/images/" + name);
        Path path = Paths.get(file.toURI());
        return Files.readAllBytes(path);
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ItineraireForm {

    private List<String> villeNames = new ArrayList<>();
    private Agency agency;
}
