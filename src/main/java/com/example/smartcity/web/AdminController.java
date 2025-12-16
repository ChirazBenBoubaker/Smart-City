package com.example.smartcity.web;

import com.example.smartcity.dao.AgentMunicipalRepository;
import com.example.smartcity.dao.CitoyenRepository;
import com.example.smartcity.dao.UserRepository;
import com.example.smartcity.dto.CreateAgentRequest;
import com.example.smartcity.metier.service.EmailService;
import com.example.smartcity.model.entity.Admin;
import com.example.smartcity.model.entity.AgentMunicipal;
import com.example.smartcity.model.entity.Citoyen;
import com.example.smartcity.model.entity.User;
import com.example.smartcity.model.enums.Departement;
import com.example.smartcity.model.enums.RoleUtilisateur;
import com.example.smartcity.util.PasswordGenerator;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AgentMunicipalRepository agentMunicipalRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final CitoyenRepository citoyenRepository;

    // ✅ constructeur obligatoire
    public AdminController(AgentMunicipalRepository agentMunicipalRepository, PasswordEncoder passwordEncoder, EmailService emailService, UserRepository userRepository, CitoyenRepository citoyenRepository) {
        this.agentMunicipalRepository = agentMunicipalRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.citoyenRepository = citoyenRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
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


}
