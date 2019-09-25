package sample.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import io.aergo.openkeychain.model.Challenge;
import io.aergo.openkeychain.model.Entry;
import io.aergo.openkeychain.model.Metadata;
import io.aergo.openkeychain.model.Response;
import sample.service.OpenkeychainService;

@RestController
public class AppController {
	
	@Autowired
	private OpenkeychainService openkeychain;
	
	
	@GetMapping("/")
	public String index() {
		return "openkeychain";
	}
	
	@GetMapping("/challenge")
	public Challenge challenge() {
		return openkeychain.createChallenge();
	}
	
	@GetMapping("/registration/record")
	public String recordRegistration(
			@ModelAttribute Response response, @ModelAttribute Metadata data) throws IOException {
		return openkeychain.recordRegistration(response, data);
	}
	
	@GetMapping("/registration/revoke")
	public String revokeRegistration(
			@ModelAttribute Response response) throws IOException {
		return openkeychain.revokeRegistration(response);
	}

	@GetMapping("/registration/check")
	public boolean checkRegistration(
			@ModelAttribute Response response) throws IOException {
		return openkeychain.checkRegistration(response);
	}

	@GetMapping("/registration")
	public Entry fetchRegistration(
			@ModelAttribute Response response) throws IOException {
		return openkeychain.fetchRegistration(response);
	}
}
