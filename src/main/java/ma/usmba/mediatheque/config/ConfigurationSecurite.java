package ma.usmba.mediatheque.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import ma.usmba.mediatheque.modele.AnnuaireLecteurs;
import ma.usmba.mediatheque.modele.CompteLecteur;

@Configuration
@EnableWebSecurity
public class ConfigurationSecurite {

	private static final List<String> HIERARCHIE = List.of("LECTEUR", "BIBLIOTHECAIRE", "CONSERVATEUR", "ADMIN");

	@Bean
	public PasswordEncoder encodeur() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService serviceLecteurs(AnnuaireLecteurs annuaire, PasswordEncoder encodeur) {
		List<UserDetails> comptes = new ArrayList<>();

		for (CompteLecteur compte : annuaire.getComptes()) {
			comptes.add(User.withUsername(compte.identifiant())
					.password(encodeur.encode(compte.secretClair()))
					.roles(rolesCumules(compte.rolePrincipal()).toArray(String[]::new))
					.build());
		}

		return new InMemoryUserDetailsManager(comptes);
	}

	private List<String> rolesCumules(String rolePrincipal) {
		int rang = HIERARCHIE.indexOf(rolePrincipal);
		return HIERARCHIE.subList(0, rang + 1);
	}

	@Bean
	public SecurityFilterChain chaineDeFiltres(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(acces -> acces
						.requestMatchers("/", "/connexion", "/acces-refuse", "/css/**", "/error").permitAll()
						.requestMatchers("/espace/administration/**").hasRole("ADMIN")
						.requestMatchers("/espace/conservation/**").hasRole("CONSERVATEUR")
						.requestMatchers("/espace/bibliothecaire/**").hasRole("BIBLIOTHECAIRE")
						.requestMatchers("/espace/lecteur/**").hasRole("LECTEUR")
						.anyRequest().authenticated())
				.formLogin(connexion -> connexion
						.loginPage("/connexion")
						.loginProcessingUrl("/authentification")
						.usernameParameter("identifiant")
						.passwordParameter("motDePasse")
						.successHandler(new RedirectionParRole())
						.failureUrl("/connexion?erreur=true")
						.permitAll())
				.logout(deconnexion -> deconnexion
						.logoutUrl("/deconnexion")
						.logoutSuccessUrl("/connexion?deconnexion=true")
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.permitAll())
				.exceptionHandling(refus -> refus.accessDeniedPage("/acces-refuse"));

		return http.build();
	}
}
