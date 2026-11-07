package ma.usmba.mediatheque.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ma.usmba.mediatheque.modele.AnnuaireLecteurs;
import ma.usmba.mediatheque.modele.Catalogue;

@Controller
public class ConnexionController {

	private final AnnuaireLecteurs annuaire;
	private final Catalogue catalogue;

	public ConnexionController(AnnuaireLecteurs annuaire, Catalogue catalogue) {
		this.annuaire = annuaire;
		this.catalogue = catalogue;
	}

	@GetMapping("/")
	public String accueil(Model modele) {
		modele.addAttribute("comptes", annuaire.getComptes());
		modele.addAttribute("nombreOuvrages", catalogue.getNombreOuvrages());
		modele.addAttribute("nombreDisponibles", catalogue.getNombreDisponibles());
		return "accueil";
	}

	@GetMapping("/connexion")
	public String connexion() {
		return "connexion";
	}

	@GetMapping("/acces-refuse")
	public String accesRefuse() {
		return "acces_refuse";
	}
}
