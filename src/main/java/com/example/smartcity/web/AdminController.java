package com.example.smartcity.web;

import com.example.smartcity.dao.AgentMunicipalRepository;
import com.example.smartcity.metier.service.EmailService;
import com.example.smartcity.model.entity.AgentMunicipal;
import com.example.smartcity.model.enums.Departement;
import com.example.smartcity.model.enums.RoleUtilisateur;
import com.example.smartcity.util.PasswordGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AgentMunicipalRepository agentMunicipalRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ✅ constructeur obligatoire
    public AdminController(AgentMunicipalRepository agentMunicipalRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.agentMunicipalRepository = agentMunicipalRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/agents")
    public String agents(Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "3") int size) {

        Page<AgentMunicipal> agentsPage =
                agentMunicipalRepository.findAll(
                        PageRequest.of(page, size, Sort.by("id").descending())
                );

        // 🔹 données pour le tableau
        model.addAttribute("agents", agentsPage.getContent());

        // 🔹 pagination
        model.addAttribute("agentsPage", agentsPage);
        model.addAttribute("baseUrl", "/admin/agents");

        // 🔹 enums pour le modal
        model.addAttribute("departements", Departement.values());

        return "admin/agents";
    }


    @PostMapping("/agents")
    public String createAgent(@RequestParam String nom,
                              @RequestParam String prenom,
                              @RequestParam String email,
                              @RequestParam String telephone,
                              @RequestParam Departement departement) {

        // 1️⃣ mot de passe aléatoire
        String rawPassword = PasswordGenerator.generate(10);

        // 2️⃣ création agent
        AgentMunicipal agent = new AgentMunicipal();
        agent.setNom(nom);
        agent.setPrenom(prenom);
        agent.setEmail(email);
        agent.setTelephone(telephone);
        agent.setDepartement(departement);
        agent.setRole(RoleUtilisateur.AGENT_MUNICIPAL);
        agent.setEnabled(true);
        agent.setPassword(passwordEncoder.encode(rawPassword));

        agentMunicipalRepository.save(agent);

        // 3️⃣ email
        emailService.send(
                email,
                "Votre compte Agent Municipal - Smart City",
                """
                <h3>Bienvenue %s %s</h3>
                <p>Votre compte agent a été créé.</p>
                <p><b>Email :</b> %s</p>
                <p><b>Mot de passe :</b> %s</p>
                <p>Veuillez changer votre mot de passe après connexion.</p>
                """.formatted(prenom, nom, email, rawPassword)
        );

        return "redirect:/admin/agents";
    }
}
