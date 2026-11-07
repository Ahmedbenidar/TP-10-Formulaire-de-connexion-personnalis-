package ma.usmba.mediatheque.modele;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AnnuaireLecteurs {

	private final List<CompteLecteur> comptes = List.of(
			new CompteLecteur("lecteur", "4321", "Yasmine Berrada", "Étudiante en master informatique", "LECTEUR"),
			new CompteLecteur("lecteur2", "9753", "Salma Idrissi", "Étudiante en licence biologie", "LECTEUR"),
			new CompteLecteur("biblio", "8765", "Rachid Ouhadi", "Bibliothécaire - service prêt", "BIBLIOTHECAIRE"),
			new CompteLecteur("conservateur", "2468", "Latifa Aït Ali", "Conservatrice du fonds ancien", "CONSERVATEUR"),
			new CompteLecteur("admin", "1357", "Mounir Sbai", "Administrateur du système", "ADMIN"));

	public List<CompteLecteur> getComptes() {
		return comptes;
	}

	public CompteLecteur parIdentifiant(String identifiant) {
		return comptes.stream()
				.filter(c -> c.identifiant().equals(identifiant))
				.findFirst()
				.orElse(null);
	}
}
