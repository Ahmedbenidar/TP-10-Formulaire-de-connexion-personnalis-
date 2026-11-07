package ma.usmba.mediatheque.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ma.usmba.mediatheque.modele.AnnuaireLecteurs;
import ma.usmba.mediatheque.modele.Catalogue;
import ma.usmba.mediatheque.modele.CompteLecteur;

@Controller
public class EspaceController {

	private final AnnuaireLecteurs annuaire;
	private final Catalogue catalogue;

	public EspaceController(AnnuaireLecteurs annuaire, Catalogue catalogue) {
		this.annuaire = annuaire;
		this.catalogue = catalogue;
	}

	@GetMapping("/espace/lecteur/tableau-de-bord")
	public String espaceLecteur(Authentication authentification, Model modele) {
		CompteLecteur compte = annuaire.parIdentifiant(authentification.getName());
		String nomComplet = compte == null ? authentification.getName() : compte.nomComplet();

		modele.addAttribute("nomComplet", nomComplet);
		modele.addAttribute("fonction", compte == null ? "Lecteur" : compte.fonction());
		modele.addAttribute("prets", catalogue.getPrets().stream()
				.filter(p -> p.emprunteur().equals(nomComplet))
				.toList());
		modele.addAttribute("ouvrages", catalogue.getOuvrages());
		return "espace_lecteur";
	}

	@GetMapping("/espace/bibliothecaire/tableau-de-bord")
	public String espaceBibliothecaire(Model modele) {
		modele.addAttribute("prets", catalogue.getPrets());
		modele.addAttribute("enRetard", catalogue.getPretsEnRetard());
		modele.addAttribute("nombreOuvrages", catalogue.getNombreOuvrages());
		return "espace_bibliothecaire";
	}

	@GetMapping("/espace/conservation/tableau-de-bord")
	public String espaceConservateur(Model modele) {
		modele.addAttribute("ouvrages", catalogue.getOuvrages());
		modele.addAttribute("nombreDisponibles", catalogue.getNombreDisponibles());
		return "espace_conservation";
	}

	@GetMapping("/espace/administration/tableau-de-bord")
	public String espaceAdministration(Model modele) {
		modele.addAttribute("comptes", annuaire.getComptes());
		modele.addAttribute("nombreOuvrages", catalogue.getNombreOuvrages());
		return "espace_administration";
	}
}
