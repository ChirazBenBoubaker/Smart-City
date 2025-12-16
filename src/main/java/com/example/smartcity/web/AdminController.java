package com.example.smartcity.web;

import com.example.smartcity.dao.*;
import com.example.smartcity.dto.CreateAgentRequest;
import com.example.smartcity.metier.service.EmailService;
import com.example.smartcity.metier.service.IncidentEmailService;
import com.example.smartcity.metier.service.IncidentService;
import com.example.smartcity.model.entity.*;
import com.example.smartcity.model.enums.Departement;
import com.example.smartcity.model.enums.PrioriteIncident;
import com.example.smartcity.model.enums.RoleUtilisateur;
import com.example.smartcity.model.enums.StatutIncident;
import com.example.smartcity.util.PasswordGenerator;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AgentMunicipalRepository agentMunicipalRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final CitoyenRepository citoyenRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentEmailService incidentEmailService;
    private final IncidentService incidentService;
    private final NotificationRepository notificationRepository;

    // ✅ constructeur obligatoire
    public AdminController(AgentMunicipalRepository agentMunicipalRepository, PasswordEncoder passwordEncoder, EmailService emailService, UserRepository userRepository, CitoyenRepository citoyenRepository, IncidentRepository incidentRepository, IncidentEmailService incidentEmailService, IncidentService incidentService, NotificationRepository notificationRepository) {
        this.agentMunicipalRepository = agentMunicipalRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.citoyenRepository = citoyenRepository;
        this.incidentRepository = incidentRepository;
        this.incidentEmailService = incidentEmailService;
        this.incidentService = incidentService;
        this.notificationRepository = notificationRepository;
    }


    @GetMapping("/agents")
    public String agents(Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "3") int size) {

        // ✅ récupération paginée des agents ACTIVÉS uniquement
        Page<AgentMunicipal> agentsPage =
                agentMunicipalRepository.findByEnabledTrue(
                        PageRequest.of(page, size, Sort.by("id").descending())
                );

        // ✅ données pour le tableau
        model.addAttribute("agents", agentsPage.getContent());

        // ✅ pagination
        model.addAttribute("agentsPage", agentsPage);
        model.addAttribute("baseUrl", "/admin/agents");

        // ✅ enums pour le select
        model.addAttribute("departements", Departement.values());

        // ✅ OBLIGATOIRE pour th:object dans le modal
        model.addAttribute("createAgentRequest", new CreateAgentRequest());

        return "admin/agents";
    }


    @PostMapping("/agents")
    public String createAgent(
            @Valid @ModelAttribute("createAgentRequest") CreateAgentRequest req,
            BindingResult br,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {

        // ✅ Vérifier email unique
        if (userRepository.existsByEmail(req.getEmail())) {
            br.rejectValue("email", "email.exists", "Cet email est déjà utilisé");
        }

        // ❌ S’il y a des erreurs → retour page + modal ouvert
        if (br.hasErrors()) {

            Page<AgentMunicipal> agentsPage =
                    agentMunicipalRepository.findByEnabledTrue(
                            PageRequest.of(page, size, Sort.by("id").descending())
                    );

            model.addAttribute("agents", agentsPage.getContent());
            model.addAttribute("agentsPage", agentsPage);
            model.addAttribute("baseUrl", "/admin/agents");
            model.addAttribute("departements", Departement.values());
            model.addAttribute("createAgentRequest", req);

            // 🔥 pour rouvrir le modal
            model.addAttribute("showAgentModal", true);

            return "admin/agents";
        }

        // ✅ Création OK
        String rawPassword = PasswordGenerator.generate(10);

        AgentMunicipal agent = new AgentMunicipal();
        agent.setNom(req.getNom());
        agent.setPrenom(req.getPrenom());
        agent.setEmail(req.getEmail());
        agent.setTelephone(req.getTelephone());
        agent.setDepartement(req.getDepartement());
        agent.setRole(RoleUtilisateur.AGENT_MUNICIPAL);
        agent.setEnabled(true);
        agent.setPassword(passwordEncoder.encode(rawPassword));

        agentMunicipalRepository.save(agent);

        emailService.send(
                req.getEmail(),
                "Votre compte Agent Municipal - Smart City",
                ("<h3>Bienvenue %s %s</h3>"
                        + "<p>Votre compte agent a été créé.</p>"
                        + "<p><b>Email :</b> %s</p>"
                        + "<p><b>Mot de passe :</b> %s</p>")
                        .formatted(req.getPrenom(), req.getNom(), req.getEmail(), rawPassword)
        );

        return "redirect:/admin/agents";
    }
    // AdminController
    @GetMapping("/users/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return userRepository.existsByEmail(email);
    }


    @PostMapping("/agents/{id}/disable")
    public String disableAgent(@PathVariable Long id) {
        AgentMunicipal agent = agentMunicipalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agent introuvable"));

        agent.setEnabled(false); // ✅ soft delete
        agentMunicipalRepository.save(agent);

        return "redirect:/admin/agents";
    }
    @GetMapping("/profile")
    public String adminProfile(Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {

        // récupérer l’admin depuis la base
        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        model.addAttribute("admin", admin);

        return "admin/profile";
    }

    @GetMapping("/citoyen")
    public String citoyens(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "5") int size) {

        Page<Citoyen> citoyensPage = citoyenRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        model.addAttribute("citoyens", citoyensPage.getContent());
        model.addAttribute("citoyensPage", citoyensPage);
        model.addAttribute("baseUrl", "/admin/citoyen");

        return "admin/citoyens";
    }

    @GetMapping("/incidents")
    public String incidents(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {

        Page<Incident> incidentsPage = incidentRepository.findAll(
                PageRequest.of(page, size, Sort.by("dateSignalement").descending())
        );

        model.addAttribute("incidents", incidentsPage.getContent());
        model.addAttribute("incidentsPage", incidentsPage);
        model.addAttribute("baseUrl", "/admin/incidents");
        model.addAttribute("categories", Departement.values());
        model.addAttribute("statuts", StatutIncident.values());
        model.addAttribute("priorites", PrioriteIncident.values());
        return "admin/incidents";
    }

    @GetMapping("/incidents/{id}")
    public String incidentDetails(@PathVariable Long id, Model model) {

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Incident introuvable"));

        // 🔹 Agents du même département + actifs + en service
        List<AgentMunicipal> agentsDisponibles =
                agentMunicipalRepository
                        .findByDepartementAndEnabledTrueAndEnServiceTrue(
                                incident.getCategorie()
                        );

        model.addAttribute("incident", incident);
        model.addAttribute("agentsDisponibles", agentsDisponibles);

        return "citoyen/detail-incident";
    }



    @PostMapping("/incidents/{id}/assign")
    public String assignIncident(
            @PathVariable Long id,
            @RequestParam Long agentId
    ) {

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Incident introuvable"));

        AgentMunicipal agent = agentMunicipalRepository.findById(agentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Agent introuvable"));

        // 🔒 Sécurité métier (optionnelle)
        if (!agent.getDepartement().equals(incident.getCategorie())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Agent non autorisé pour ce département"
            );
        }

        // ✅ Affectation
        incident.setAgentResponsable(agent);
        incident.setStatut(StatutIncident.PRIS_EN_CHARGE);
        incident.setDatePriseEnCharge(LocalDateTime.now());

        incidentRepository.save(incident);

        emailService.send(
                agent.getEmail(),
                "Nouvel incident affecté – Smart City",
                incidentEmailService.buildAssignIncidentEmail(agent, incident)
        );

        return "redirect:/admin/incidents/" + id;
    }

    @PostMapping("/incidents/{id}/supprimer")
    public String supprimerIncidentAdmin(@PathVariable Long id) {

        incidentService.supprimerParAdmin(id);

        return "redirect:/admin/incidents";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // ====== CARTES ======
        model.addAttribute("totalAgents", agentMunicipalRepository.count());
        model.addAttribute("activeAgents", agentMunicipalRepository.countByEnabledTrue());

        model.addAttribute("totalCitoyens", citoyenRepository.count());
        model.addAttribute("activeCitoyens", citoyenRepository.countByEnabledTrue());

        model.addAttribute("totalIncidents", incidentRepository.count());
        model.addAttribute(
                "activeIncidents",
                incidentRepository.countByStatutNot(StatutIncident.CLOTURE)
        );

        // ====== ÉVOLUTION INCIDENTS (7 jours) ======
        LocalDateTime startDate = LocalDateTime.now().minusDays(6);

        List<Object[]> results = incidentRepository.countIncidentsByDay(startDate);

        List<String> days = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (Object[] row : results) {
            days.add(((java.sql.Date) row[0]).toLocalDate().format(formatter));
            counts.add((Long) row[1]);
        }

        model.addAttribute("incidentDays", days);
        model.addAttribute("incidentCounts", counts);
        List<Object[]> depResults = incidentRepository.countIncidentsByDepartementThisMonth();

        List<String> departements = new ArrayList<>();
        List<Long> depCounts = new ArrayList<>();

        for (Object[] row : depResults) {
            departements.add(((Departement) row[0]).name());
            depCounts.add((Long) row[1]);
        }

        model.addAttribute("departementLabels", departements);
        model.addAttribute("departementCounts", depCounts);
        // 🆕 Incidents récents
        model.addAttribute(
                "recentIncidents",
                incidentRepository.findTop5ByOrderByDateSignalementDesc()
        );

        return "admin/dashboard";
    }


}
